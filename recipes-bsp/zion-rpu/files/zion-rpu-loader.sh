#!/bin/bash
# Loads an R5 (RPU) firmware image via the Linux remoteproc sysfs interface,
# if one has been dropped into /lib/firmware. No-ops cleanly when absent —
# Zion ships no RPU firmware today (see README-RPU.md).

set -euo pipefail

FW_NAME="zion-rpu0.elf"
FW_PATH="/lib/firmware/${FW_NAME}"
REMOTEPROC="/sys/class/remoteproc/remoteproc0"

if [ ! -e "${FW_PATH}" ]; then
    echo "zion-rpu-loader: no ${FW_PATH} present, skipping RPU firmware load"
    exit 0
fi

if [ ! -d "${REMOTEPROC}" ]; then
    echo "zion-rpu-loader: ${REMOTEPROC} not found (remoteproc driver not bound), skipping" >&2
    exit 0
fi

state="$(cat "${REMOTEPROC}/state" 2>/dev/null || echo unknown)"
if [ "${state}" = "running" ]; then
    echo "zion-rpu-loader: remoteproc0 already running, leaving it alone"
    exit 0
fi

echo "zion-rpu-loader: loading ${FW_NAME} onto remoteproc0"
echo "${FW_NAME}" > "${REMOTEPROC}/firmware"
echo start > "${REMOTEPROC}/state"
