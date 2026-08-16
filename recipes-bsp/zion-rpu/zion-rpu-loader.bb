SUMMARY = "Zion RPU (Cortex-R5F) remoteproc firmware loader"
DESCRIPTION = "Systemd oneshot that loads /lib/firmware/zion-rpu0.elf onto \
remoteproc0 if present, and no-ops otherwise. Ships the loading mechanism \
only — no RPU firmware image is included (see README-RPU.md)."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://zion-rpu-loader.sh \
    file://zion-rpu-loader.service \
    file://README-RPU.md \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "zion-rpu-loader.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash"

do_install() {
    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/zion-rpu-loader.sh ${D}${sbindir}/zion-rpu-loader.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/zion-rpu-loader.service \
                    ${D}${systemd_system_unitdir}/zion-rpu-loader.service

    install -d ${D}${docdir}/${PN}
    install -m 0644 ${WORKDIR}/README-RPU.md ${D}${docdir}/${PN}/README-RPU.md
}

FILES:${PN} = " \
    ${sbindir}/zion-rpu-loader.sh \
    ${systemd_system_unitdir}/zion-rpu-loader.service \
    ${docdir}/${PN}/README-RPU.md \
"
