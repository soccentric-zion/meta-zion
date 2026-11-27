#!/bin/bash
# swupdate-start.sh — SWUpdate startup wrapper.
#
# Conditionally enables:
#   * RSA signature verification (-k)   — only when
#     /etc/swupdate/swupdate-public.pem exists AND is a parseable public
#     key (an unparseable key file would make swupdate exit at startup).
#     Generate a real keypair with scripts/generate-swu-keys.sh.
#   * Web UI authentication (--auth_domain/--htpasswd_file) — only when
#     /etc/swupdate/webui.passwd exists.

set -euo pipefail

CFG=/etc/swupdate/swupdate.cfg
KEY=/etc/swupdate/swupdate-public.pem
PASSWD=/etc/swupdate/webui.passwd

KEY_ARGS=""
AUTH_ARGS=""

if [[ -f "${KEY}" ]] && openssl pkey -pubin -in "${KEY}" -noout >/dev/null 2>&1; then
    KEY_ARGS="-k ${KEY}"
elif [[ -f "${KEY}" ]]; then
    logger -t swupdate-start "swupdate-public.pem is not a valid public key — signature verification disabled (run scripts/generate-swu-keys.sh)"
fi

if [[ -f "${PASSWD}" ]]; then
    AUTH_ARGS=" --auth_domain=zion --htpasswd_file=${PASSWD}"
fi

exec /usr/bin/swupdate \
    -f "${CFG}" \
    ${KEY_ARGS} \
    -w "--document-root /usr/share/swupdate/www --port 8080${AUTH_ARGS}"