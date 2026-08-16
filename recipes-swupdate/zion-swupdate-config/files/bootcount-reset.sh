#!/bin/bash
# bootcount-reset.sh — U-Boot env OTA rollback confirmation.
#
# Runs once after a successful boot as a systemd oneshot service.
# Sets upgrade_available=0 and bootcount=0 in the U-Boot environment to
# signal that the current slot is healthy and rollback is not needed.

set -euo pipefail

FW_SETENV=$(command -v fw_setenv  || true)
FW_PRINTENV=$(command -v fw_printenv || true)

if [[ -z "${FW_SETENV}" || ! -x "${FW_SETENV}" ]]; then
    logger -t bootcount-reset "fw_setenv not found in PATH — skipping"
    exit 0
fi

CURRENT=$(${FW_PRINTENV:-/bin/true} -n upgrade_available 2>/dev/null || echo "0")
case "${CURRENT}" in
    0|"")
        logger -t bootcount-reset "upgrade_available already '${CURRENT}' — nothing to do"
        exit 0
        ;;
    1)
        : # expected post-OTA state — proceed to confirm
        ;;
    *)
        logger -t bootcount-reset \
            "WARNING: upgrade_available='${CURRENT}' is unexpected — clearing anyway"
        ;;
esac

if ! ${FW_SETENV} upgrade_available 0; then
    logger -t bootcount-reset "ERROR: fw_setenv upgrade_available 0 failed"
    exit 1
fi
if ! ${FW_SETENV} bootcount 0; then
    logger -t bootcount-reset "ERROR: fw_setenv bootcount 0 failed"
    exit 1
fi

logger -t bootcount-reset \
    "Upgrade confirmed — upgrade_available cleared, bootcount reset to 0"
