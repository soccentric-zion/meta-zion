# linux-xlnx_%.bbappend — Zion kernel configuration
#
# Applies the Zion base kernel fragment (recipes-kernel/linux/files/
# zion-base.cfg) on top of the Xilinx BSP defconfig for every supported
# Kria machine. The fragment enables the ZynqMP features the BSP leaves
# off (spidev, system monitor, TRNG entropy, NVMe, SPI CAN controllers) —
# see zion-base.cfg for the rationale behind each CONFIG_ line.

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " file://zion-base.cfg"