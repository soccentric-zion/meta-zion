#!/bin/bash
# swupdate-bootcheck.sh — health check that confirms OTA or triggers rollback.
#
# Runs after local-fs.target (before network.target) to ensure the health
# check completes BEFORE network services start. If the health check fails,
# the system rolls back before any network exposure.
#
# On success it confirms the slot by writing /boot/boot_ok — the U-Boot
# script consumes that marker on the next boot and clears the rollback
# boot counter.

set -euo pipefail

BOOT_STATE=/boot/boot_state
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

sleep 5

HEALTH_OK=true

# Check zion-hal / zion services are running (representative health check)
if ! systemctl is-active --quiet zion-services.service 2>/dev/null; then
    HEALTH_OK=false
    logger -t swupdate-bootcheck "zion-services.service not active"
fi

# Verify kernel/DTB files are intact
if [[ "${HEALTH_OK}" == "true" ]]; then
    for f in ${BOOT_FILES}; do
        if [[ ! -f "/boot/${f}" ]]; then
            HEALTH_OK=false
            logger -t swupdate-bootcheck "${f} missing after boot"
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
            HEALTH_OK=false
            logger -t swupdate-bootcheck \
                "${f} size ${SIZE} bytes outside [${MIN}, ${MAX}]"
            break
        fi
    done
fi

if [[ "${HEALTH_OK}" == "true" ]]; then
    mount -o remount,rw /boot
    sed -i 's/confirmed=false/confirmed=true/' "${BOOT_STATE}"
    # Confirm for the U-Boot rollback counter: boot.cmd.zion consumes this
    # marker on the next boot and clears upgrade_pending + bootcount.
    touch /boot/boot_ok
    mount -o remount,ro /boot
    logger -t swupdate-bootcheck "Slot ${slot} confirmed healthy"
else
    logger -t swupdate-bootcheck "Slot ${slot} unhealthy — rolling back"
    mount -o remount,rw /boot
    for f in ${BOOT_FILES}; do
        [[ -f "/boot/${f}.prev" ]] && cp "/boot/${f}.prev" "/boot/${f}" || true
        rm -f "/boot/${f}.prev"
    done
    rm -f /boot/slot_b /boot/upgrade_pending "${BOOT_STATE}"
    mount -o remount,ro /boot
    reboot
fi