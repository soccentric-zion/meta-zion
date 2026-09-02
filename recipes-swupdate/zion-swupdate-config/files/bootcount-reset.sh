#!/bin/bash
# bootcount-reset.sh — OTA confirmation cleanup after a healthy boot.
#
# swupdate-bootcheck.sh already drops /boot/boot_ok for the U-Boot script
# (which consumes it and resets its rollback counter). This service is the
# belt-and-braces confirmation path: it also clears the U-Boot environment
# via fw_setenv when /etc/fw_env.config matches the actual U-Boot env
# storage (failures here are non-fatal — slot state lives in the boot
# partition markers, not in the env).

set -euo pipefail

FW_SETENV=$(command -v fw_setenv  || true)
FW_PRINTENV=$(command -v fw_printenv || true)

# Confirm for U-Boot (idempotent with swupdate-bootcheck)
mount -o remount,rw /boot 2>/dev/null || true
touch /boot/boot_ok 2>/dev/null || true
mount -o remount,ro /boot 2>/dev/null || true

if [[ -z "${FW_SETENV}" || ! -x "${FW_SETENV}" || ! -f /etc/fw_env.config ]]; then
    logger -t bootcount-reset "fw_setenv or /etc/fw_env.config unavailable — boot_ok marker is authoritative"
    exit 0
fi

CURRENT=$(${FW_PRINTENV:-/bin/true} -n upgrade_available 2>/dev/null || echo "0")
if [[ "${CURRENT}" == "0" || -z "${CURRENT}" ]]; then
    logger -t bootcount-reset "upgrade_available already '${CURRENT}' — nothing to do"
    exit 0
fi

if ! ${FW_SETENV} upgrade_available 0; then
    logger -t bootcount-reset "ERROR: fw_setenv upgrade_available 0 failed"
    exit 0
fi
if ! ${FW_SETENV} bootcount 0; then
    logger -t bootcount-reset "ERROR: fw_setenv bootcount 0 failed"
    exit 0
fi

logger -t bootcount-reset \
    "Upgrade confirmed — upgrade_available cleared, bootcount reset to 0"