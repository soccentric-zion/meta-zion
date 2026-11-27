#!/bin/bash
# SWUpdate post-install script — included in every .swu package
#
# Activates the newly written slot for the next boot:
#   - sets the /boot/slot_b marker (boot.cmd.zion selects the slot from it)
#   - drops /boot/upgrade_pending so the U-Boot rollback boot counter arms
#   - writes /boot/boot_state for the userspace watchdog/bootcheck
#   - backs up nothing extra: pre-install already saved Image/system.dtb .prev

set -euo pipefail

TARGET_SLOT=$(cat /run/swupdate-target-slot)
TARGET_DEV=$(cat /run/swupdate-target-dev)

echo "[swupdate post-install] activating slot ${TARGET_SLOT} (${TARGET_DEV})"

mount -o remount,rw /boot

# Slot markers: slot_b present -> boot p3 (rootfs-b); absent -> p2 (rootfs-a)
if [[ "${TARGET_SLOT}" == "b" ]]; then
    touch /boot/slot_b
else
    rm -f /boot/slot_b
fi

# Arm the U-Boot rollback counter (consumed by boot.cmd.zion)
touch /boot/upgrade_pending

# State for the userspace watchdog — 3 tries before automatic rollback
printf 'slot=%s\ntries_remaining=3\nconfirmed=false\n' "${TARGET_SLOT}" \
    > /boot/boot_state

# Best-effort U-Boot env update (needs /etc/fw_env.config matching the
# U-Boot env storage; slot switching itself does NOT depend on it)
if command -v fw_setenv &>/dev/null && [[ -f /etc/fw_env.config ]]; then
    fw_setenv upgrade_available 1 \
        || logger -t swupdate-post-install "fw_setenv upgrade_available failed (non-fatal)"
    fw_setenv bootcount 0 \
        || logger -t swupdate-post-install "fw_setenv bootcount failed (non-fatal)"
    echo "[swupdate post-install] U-Boot env updated (upgrade_available=1 bootcount=0)"
else
    echo "[swupdate post-install] fw_setenv unavailable — slot state kept in boot partition markers"
fi

mount -o remount,ro /boot

echo "[swupdate post-install] slot=${TARGET_SLOT} armed (upgrade_pending + boot_state written)"

# Clean up temp files and symlink
rm -f /dev/swupdate-inactive \
      /run/swupdate-target-slot \
      /run/swupdate-target-dev