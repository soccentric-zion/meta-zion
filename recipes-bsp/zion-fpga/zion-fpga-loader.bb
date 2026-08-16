SUMMARY = "Zion PL/FPGA static bitstream loader"
DESCRIPTION = "Systemd oneshot that loads /lib/firmware/zion.bit.bin (+ \
optional zion.dtbo overlay) via fpga_manager/fpgautil if present, and \
no-ops otherwise. Static/full reconfiguration only — partial \
reconfiguration (DFX) is not implemented, see README-FPGA.md. Not \
installed by default; opt-in per deployment."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://zion-fpga-loader.sh \
    file://zion-fpga-loader.service \
    file://README-FPGA.md \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "zion-fpga-loader.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash fpga-manager-script"

do_install() {
    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/zion-fpga-loader.sh ${D}${sbindir}/zion-fpga-loader.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/zion-fpga-loader.service \
                    ${D}${systemd_system_unitdir}/zion-fpga-loader.service

    install -d ${D}${docdir}/${PN}
    install -m 0644 ${WORKDIR}/README-FPGA.md ${D}${docdir}/${PN}/README-FPGA.md
}

FILES:${PN} = " \
    ${sbindir}/zion-fpga-loader.sh \
    ${systemd_system_unitdir}/zion-fpga-loader.service \
    ${docdir}/${PN}/README-FPGA.md \
"
