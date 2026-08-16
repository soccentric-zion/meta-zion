#!/bin/bash
# Loads a static PL bitstream (+ optional device-tree overlay) via the
# kernel fpga_manager, if one has been dropped into /lib/firmware. No-ops
# cleanly when absent — Zion ships no bitstream (see README-FPGA.md).
#
# Only full/static bitstream loading is supported here. Partial
# reconfiguration (DFX) is a placeholder — see README-FPGA.md.

set -euo pipefail

BIT_PATH="/lib/firmware/zion.bit.bin"
DTBO_PATH="/lib/firmware/zion.dtbo"

if [ ! -e "${BIT_PATH}" ]; then
    echo "zion-fpga-loader: no ${BIT_PATH} present, skipping PL bitstream load"
    exit 0
fi

if [ ! -d /sys/class/fpga_manager ]; then
    echo "zion-fpga-loader: /sys/class/fpga_manager not found (fpga_manager driver not bound), skipping" >&2
    exit 0
fi

if [ -e "${DTBO_PATH}" ]; then
    echo "zion-fpga-loader: loading ${BIT_PATH} with overlay ${DTBO_PATH}"
    exec fpgautil -b "${BIT_PATH}" -o "${DTBO_PATH}"
else
    echo "zion-fpga-loader: loading ${BIT_PATH} via sysfs (no overlay found)"
    exec fpgautil -b "${BIT_PATH}"
fi
