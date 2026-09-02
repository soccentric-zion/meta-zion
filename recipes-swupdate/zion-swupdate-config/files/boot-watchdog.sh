#!/bin/bash
# boot-watchdog.sh — early boot rollback watchdog.
#
# Reads /boot/boot_state and:
#   - If confirmed=true  → nothing to do, slot is healthy.
#   - If confirmed=false → verify boot health (kernel/DTB integrity), then
#     decrement tries_remaining. If tries_remaining reaches 0 → restore the
#     previous kernel/DTB, clear the slot marker and reboot.
#
# Slot selection itself is done by boot.cmd.zion (slot_b marker +
# U-Boot boot counter for kernels that never reach userspace); this
# watchdog covers "the kernel boots but the system is broken" cases.

set -euo pipefail

BOOT_STATE=/boot/boot_state
# Kernel/DTB shared by both slots; restored from .prev on rollback
BOOT_FILES="Image system.dtb"

if [[ ! -f "${BOOT_STATE}" ]]; then
    exit 0
fi

slot=""
tries_remaining=""
confirmed=""
while IFS='=' read -r key value; do
    case "${key}" in
        slot)            slot="${value}" ;;
        tries_remaining) tries_remaining="${value}" ;;
        confirmed)       confirmed="${value}" ;;
        \#*|"")          : ;;
    esac
done < "${BOOT_STATE}"

if [[ "${confirmed:-false}" == "true" ]]; then
    exit 0
fi

rollback() {
    echo "[boot-watchdog] $* — rolling back" | tee /dev/console
    logger -t boot-watchdog "$* — rolling back"
    mount -o remount,rw /boot
    for f in ${BOOT_FILES}; do
        [[ -f "/boot/${f}.prev" ]] && cp "/boot/${f}.prev" "/boot/${f}" || true
        rm -f "/boot/${f}.prev"
    done
    # Boot slot A again: clear the slot marker and the pending flag
    rm -f /boot/slot_b /boot/upgrade_pending "${BOOT_STATE}"
    mount -o remount,ro /boot
    reboot
}

# Health check 1: kernel + DTB integrity
INTEGRITY_OK=true
for f in ${BOOT_FILES}; do
    if [[ ! -f "/boot/${f}" ]]; then
        INTEGRITY_OK=false
        logger -t boot-watchdog "${f} missing after boot"
        break
    fi
    SIZE=$(stat -c%s "/boot/${f}" 2>/dev/null || echo 0)
    case "${f}" in
        Image)
            MIN=$((1024 * 1024))
            MAX=$((50 * 1024 * 1024))
            ;;
        *.dtb)
            MIN=$((10 * 1024))
            MAX=$((5 * 1024 * 1024))
            ;;
    esac
    if [[ "${SIZE}" -lt "${MIN}" ]] || [[ "${SIZE}" -gt "${MAX}" ]]; then
        INTEGRITY_OK=false
        logger -t boot-watchdog "${f} size ${SIZE} bytes outside [${MIN}, ${MAX}]"
        break
    fi
done

mount -o remount,rw /boot

if [[ "${INTEGRITY_OK}" != "true" ]]; then
    sed -i "s/tries_remaining=.*/tries_remaining=0/" "${BOOT_STATE}"
    mount -o remount,ro /boot
    rollback "kernel/DTB integrity check failed"
fi

TRIES=$(( ${tries_remaining:-0} - 1 ))

if [[ "${TRIES}" -le 0 ]]; then
    mount -o remount,ro /boot
    rollback "tries_remaining exhausted"
else
    sed -i "s/tries_remaining=.*/tries_remaining=${TRIES}/" "${BOOT_STATE}"
    mount -o remount,ro /boot
fi