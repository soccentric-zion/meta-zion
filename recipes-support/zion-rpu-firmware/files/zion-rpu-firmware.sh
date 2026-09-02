#!/bin/bash
# zion-rpu-firmware — RPU (Cortex-R5) firmware lifecycle manager.
#
# Manages the lifecycle of firmware running on the Zynq UltraScale+ RPU
# (dual-core Cortex-R5) via the Linux remoteproc framework. Firmware ELF
# files are placed in /lib/firmware (they are NOT baked into the board);
# this tool loads, starts, stops, and reports on them at runtime.
#
# Usage:
#   zion-rpu-firmware start   [<firmware.elf>]   Load + start R5 firmware
#   zion-rpu-firmware stop                       Stop the R5
#   zion-rpu-firmware status                     Show R5 / remoteproc state
#   zion-rpu-firmware load   <firmware.elf>      Stage a firmware file
#   zion-rpu-firmware list                       List available firmware
#   zion-rpu-firmware autostart <on|off>         Enable/disable boot autostart
#
# The remoteproc device is auto-detected (first rproc that is not the PMU).

set -euo pipefail

FW_DIR="/lib/firmware"
AUTOSTART_FLAG="/var/lib/zion-rpu-firmware/autostart"
RPROC_SYS="/sys/class/remoteproc"

log() { echo "zion-rpu-firmware: $*"; }
die() { echo "zion-rpu-firmware: ERROR: $*" >&2; exit 1; }

# Find the first usable remoteproc device (exclude the PMU microblaze rproc).
find_rproc() {
    for d in "${RPROC_SYS}"/remoteproc*; do
        [ -e "$d" ] || continue
        local name
        name=$(cat "$d/name" 2>/dev/null || echo "")
        case "$name" in
            *pmu*|*microblaze*) continue ;;
        esac
        echo "${d##*/}"
        return 0
    done
    return 1
}

rproc_state() {
    local rp=$1
    cat "${RPROC_SYS}/${rp}/state" 2>/dev/null || echo "unknown"
}

start_rpu() {
    local rp fw
    rp=$(find_rproc) || die "no usable remoteproc device found (is the R5 enabled in the device tree?)"
    fw="${1:-}"
    if [ -n "$fw" ]; then
        # Stage a specific firmware file into the remoteproc firmware slot.
        [ -f "${FW_DIR}/${fw}" ] || die "firmware '${fw}' not found in ${FW_DIR}"
        echo "$fw" > "${RPROC_SYS}/${rp}/firmware"
        log "staged firmware '${fw}' on ${rp}"
    fi
    if [ "$(rproc_state "$rp")" = "running" ]; then
        log "${rp} already running."
        return 0
    fi
    echo start > "${RPROC_SYS}/${rp}/state"
    log "${rp} started."
}

stop_rpu() {
    local rp
    rp=$(find_rproc) || die "no usable remoteproc device found"
    if [ "$(rproc_state "$rp")" = "offline" ]; then
        log "${rp} already offline."
        return 0
    fi
    echo stop > "${RPROC_SYS}/${rp}/state"
    log "${rp} stopped."
}

status_rpu() {
    local rp
    rp=$(find_rproc) || { echo "no usable remoteproc device found"; return 1; }
    echo "remoteproc device : ${rp}"
    echo "name               : $(cat ${RPROC_SYS}/${rp}/name 2>/dev/null)"
    echo "state              : $(rproc_state "$rp")"
    echo "firmware           : $(cat ${RPROC_SYS}/${rp}/firmware 2>/dev/null)"
    echo "autostart          : $([ -f "$AUTOSTART_FLAG" ] && echo on || echo off)"
}

load_fw() {
    local fw=$1
    [ -f "$fw" ] || die "file '$fw' not found"
    install -Dm 0644 "$fw" "${FW_DIR}/$(basename "$fw")"
    log "staged '$(basename "$fw")' into ${FW_DIR}"
}

list_fw() {
    echo "Firmware available in ${FW_DIR}:"
    ls -1 "${FW_DIR}"/*.elf 2>/dev/null || echo "  (none)"
}

set_autostart() {
    case "$1" in
        on)  mkdir -p "$(dirname "$AUTOSTART_FLAG")"; touch "$AUTOSTART_FLAG"; log "autostart enabled." ;;
        off) rm -f "$AUTOSTART_FLAG"; log "autostart disabled." ;;
        *)   die "usage: autostart <on|off>" ;;
    esac
}

case "${1:-}" in
    start)     start_rpu "${2:-}" ;;
    stop)      stop_rpu ;;
    status)    status_rpu ;;
    load)      [ $# -ge 2 ] || die "usage: load <firmware.elf>"; load_fw "$2" ;;
    list)      list_fw ;;
    autostart) [ $# -ge 2 ] || die "usage: autostart <on|off>"; set_autostart "$2" ;;
    *)         echo "usage: $0 {start|stop|status|load|list|autostart}"; exit 1 ;;
esac
