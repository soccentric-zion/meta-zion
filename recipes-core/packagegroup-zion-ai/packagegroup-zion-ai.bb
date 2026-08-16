SUMMARY = "Zion AI/DPU acceleration — placeholder"
DESCRIPTION = "Reserved package group for DPU (Deep-learning Processing \
Unit) inference support via AMD/Xilinx Vitis-AI. Currently empty and not \
referenced by any image: no meta-vitis-ai (or equivalent XRT/VART) layer \
is included in this project's kas config, and adding one is a kas-level \
change outside meta-zion's scope. \
\
To make this real, a future kas change would need to add meta-vitis-ai \
(and its XRT dependency) alongside the existing meta-xilinx/meta-kria \
layers, a DPU-integrated PL bitstream would need to come from the \
hardware/PL team (loadable via zion-fpga-loader, see \
recipes-bsp/zion-fpga/files/README-FPGA.md), and VART/xmodel packages \
would need to be added to RDEPENDS below."
HOMEPAGE = "https://github.com/soccentric-zion/Zion-OS"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit packagegroup

PACKAGES = "${PN}"
ALLOW_EMPTY:${PN} = "1"

RDEPENDS:${PN} = ""
