#!/bin/bash
# SWUpdate post-install script — included in every .swu package
#
# Updates /boot/extlinux/extlinux.conf so that U-Boot boots the newly
# written slot on the next reboot, writes boot_state for the watchdog,
# and backs up extlinux.conf for rollback recovery.

set -euo pipefail

TARGET_SLOT=$(cat /run/swupdate-target-slot)
TARGET_DEV=$(cat  /run/swupdate-target-dev)

echo "[swupdate post-install] activating slot ${TARGET_SLOT} (${TARGET_DEV})"

# Resolve the PARTUUID of the target partition
TARGET_PARTUUID=$(blkid -s PARTUUID -o value "${TARGET_DEV}")
if [[ -z "${TARGET_PARTUUID}" ]]; then
    echo "[swupdate post-install] ERROR: cannot read PARTUUID from ${TARGET_DEV}" >&2
    exit 1
fi

mount -o remount,rw /boot

# Backup current extlinux.conf for rollback
cp /boot/extlinux/extlinux.conf /boot/extlinux/extlinux.conf.prev

# Switch root= to the new slot in the extlinux APPEND line
sed -i "s|root=PARTUUID=[a-fA-F0-9-]*|root=PARTUUID=${TARGET_PARTUUID}|" \
    /boot/extlinux/extlinux.conf

# Write boot_state for the watchdog — 3 tries before automatic rollback
printf 'slot=%s\ntries_remaining=3\nconfirmed=false\n' "${TARGET_SLOT}" \
    > /boot/boot_state

mount -o remount,ro /boot

echo "[swupdate post-install] extlinux.conf → root=PARTUUID=${TARGET_PARTUUID}"
echo "[swupdate post-install] boot_state written (slot=${TARGET_SLOT} tries=3)"

# Set U-Boot environment variables for firmware-level rollback.
if command -v fw_setenv &>/dev/null; then
    fw_setenv upgrade_available 1 \
        || logger -t swupdate-post-install \
              "ERROR: fw_setenv upgrade_available 1 failed — U-Boot env may be read-only or corrupt"
    fw_setenv bootcount 0 \
        || logger -t swupdate-post-install \
              "ERROR: fw_setenv bootcount 0 failed — U-Boot env may be read-only or corrupt"
    fw_setenv bootlimit 3 \
        || logger -t swupdate-post-install \
              "ERROR: fw_setenv bootlimit 3 failed — U-Boot env may be read-only or corrupt"
    echo "[swupdate post-install] U-Boot env: upgrade_available=1 bootcount=0 bootlimit=3"
fi

# Clean up temp files and symlink
rm -f /dev/swupdate-inactive \
      /run/swupdate-target-slot \
      /run/swupdate-target-dev
