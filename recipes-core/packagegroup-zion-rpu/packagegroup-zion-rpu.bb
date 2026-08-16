SUMMARY = "Zion RPU (Cortex-R5F) userspace support — placeholder"
DESCRIPTION = "Reserved package group for Linux-userspace RPU/OpenAMP \
support (rpmsg character-device tooling, libmetal/open-amp RPC libraries). \
Currently empty: no libmetal/open-amp package is available from any layer \
already pulled by this project's kas config (the Xilinx forks live behind \
a dynamic-layers/openamp-layer gate that needs a dedicated meta-openamp \
layer we don't have, and meta-openembedded has no generic equivalent). \
The kernel-side remoteproc/rpmsg plumbing and the firmware loader \
(zion-rpu-loader) are already active — see \
recipes-bsp/zion-rpu/files/README-RPU.md for what's needed to make this \
package group non-empty."
HOMEPAGE = "https://github.com/soccentric-zion/Zion-OS"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit packagegroup

PACKAGES = "${PN}"
ALLOW_EMPTY:${PN} = "1"

RDEPENDS:${PN} = ""
