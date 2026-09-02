#!/bin/bash
# expand-rootfs.sh — Grow root partition and filesystem to fill the SD card.
# Runs once on first boot via systemd.  A flag file prevents repeated runs.

set -euo pipefail

FLAG="/var/lib/expand-rootfs-done"

if [ -f "$FLAG" ]; then
    echo "expand-rootfs: already done, skipping."
    exit 0
fi

ROOT_DEV=$(findmnt -n -o SOURCE /)
ROOT_DEV_NAME="${ROOT_DEV#/dev/}"
DISK_NAME=$(lsblk -ndo PKNAME "/dev/${ROOT_DEV_NAME}" 2>/dev/null || echo "")
if [[ -z "${DISK_NAME}" ]]; then
    echo "expand-rootfs: cannot determine parent disk for '$ROOT_DEV'." >&2
    exit 1
fi
PART_NUM=$(cat "/sys/class/block/${ROOT_DEV_NAME}/partition" 2>/dev/null || echo "")
if [[ -z "${PART_NUM}" ]]; then
    echo "expand-rootfs: cannot determine partition number for '$ROOT_DEV'." >&2
    exit 1
fi

DISK_DEV="/dev/$DISK_NAME"

echo "expand-rootfs: resizing partition $PART_NUM on $DISK_DEV ..."
parted -s "$DISK_DEV" resizepart "$PART_NUM" 100%

partprobe "$DISK_DEV" 2>/dev/null || true
udevadm settle

echo "expand-rootfs: growing filesystem on $ROOT_DEV ..."
resize2fs "$ROOT_DEV"

mkdir -p "$(dirname "$FLAG")"
touch "$FLAG"

NEW_SIZE=$(df -h / | awk 'NR==2{print $2}')
echo "expand-rootfs: done — root filesystem is now $NEW_SIZE."
