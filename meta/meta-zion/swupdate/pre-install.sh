#!/bin/bash
# SWUpdate pre-install script — included in every .swu package
#
# Identifies the inactive rootfs slot and creates the /dev/swupdate-inactive
# symlink. Also backs up kernel and DTB for rollback recovery.
#
# The WKS partitions are labelled rootfs-a and rootfs-b regardless of the
# underlying device, so we use the LABEL to find the inactive slot — this
# works on mmcblk, NVMe, and USB block devices.

set -euo pipefail

# Files shared between both slots in the boot partition. They are backed up
# before the OTA overwrites them and restored on rollback by the boot
# watchdog / bootcheck scripts.
BOOT_FILES="Image system.dtb"

ACTIVE_DEV=$(findmnt -n -o SOURCE /)
echo "[swupdate pre-install] active root: ${ACTIVE_DEV}"

ACTIVE_LABEL=$(lsblk -n -o LABEL "${ACTIVE_DEV}" 2>/dev/null | head -1 | tr -d ' ')

case "${ACTIVE_LABEL}" in
    rootfs-a)
        INACTIVE_LABEL=rootfs-b
        INACTIVE_SLOT=b
        ;;
    rootfs-b)
        INACTIVE_LABEL=rootfs-a
        INACTIVE_SLOT=a
        ;;
    *)
        echo "[swupdate pre-install] ERROR: active label '${ACTIVE_LABEL:-<empty>}' is not rootfs-a/b" >&2
        exit 1
        ;;
esac

INACTIVE_DEV=$(lsblk -n -o PATH,LABEL | awk -v want="${INACTIVE_LABEL}" '$2 == want {print $1; exit}')

if [[ -z "${INACTIVE_DEV}" ]]; then
    echo "[swupdate pre-install] ERROR: no device with label '${INACTIVE_LABEL}'" >&2
    exit 1
fi

echo "[swupdate pre-install] target slot: ${INACTIVE_SLOT} (${INACTIVE_DEV})"

# Backup kernel and DTB so boot-watchdog / bootcheck can restore them on
# rollback (both slots share the kernel/DTB files in the boot partition).
mount -o remount,rw /boot
for f in ${BOOT_FILES}; do
    if [[ -f "/boot/${f}" ]]; then
        cp "/boot/${f}" "/boot/${f}.prev"
        echo "[swupdate pre-install] backed up /boot/${f}"
    fi
done
mount -o remount,ro /boot

# Create the stable symlink the image handler will write to
ln -sf "${INACTIVE_DEV}" /dev/swupdate-inactive

# Persist slot info for post-install
echo "${INACTIVE_SLOT}" > /run/swupdate-target-slot
echo "${INACTIVE_DEV}"  > /run/swupdate-target-dev