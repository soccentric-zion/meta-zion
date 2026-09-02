#!/bin/sh
# disk-usage-check.sh — warn when any filesystem exceeds 80% usage.
THRESHOLD=80
df -P | awk 'NR>1 {print $5, $6}' | while read -r usage mount; do
    pct="${usage%%%}"
    if [ "${pct}" -ge "${THRESHOLD}" ]; then
        msg="DISK WARNING: ${mount} is at ${usage} (threshold: ${THRESHOLD}%)"
        echo "${msg}" >&2
        logger -t disk-usage-check -p user.warning "${msg}" || true
    fi
done
exit 0
