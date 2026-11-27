#!/bin/bash
# swupdate-start.sh — wrapper that conditionally enables RSA signing.
# Called by swupdate.service.d/webui.conf drop-in instead of /usr/bin/swupdate directly.
#
# T-P14 (F12): the Mongoose web UI is GONE from production (plain-HTTP :8080
# with a printed per-boot password was a duplicate OTA path — OS-006..008).
# Only the signed-install daemon runs; OTA paths are WebConfig (K06) and HQ
# (K09). The file name keeps the drop-in contract stable.

set -euo pipefail

CFG=/etc/swupdate/swupdate.cfg
KEY=/etc/swupdate/swupdate-public.pem

KEY_ARGS=""

# RSA signed-image verification (C2)
if [[ -f "${KEY}" ]]; then
    KEY_ARGS="-k ${KEY}"
fi

exec /usr/bin/swupdate \
    -f "${CFG}" \
    ${KEY_ARGS}
