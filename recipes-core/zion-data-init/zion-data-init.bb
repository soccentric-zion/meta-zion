SUMMARY = "Zion persistent home directory initialisation"
DESCRIPTION = "Creates /home/zion on first boot after the persistent home \
partition (p4) is mounted at /home."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://zion-data-init.sh \
    file://zion-data-init.service \
"


inherit systemd

SYSTEMD_SERVICE:${PN}     = "zion-data-init.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash util-linux parted e2fsprogs-resize2fs"

do_install() {
    install -d ${D}${libdir}/zion-data-init
    install -m 0755 ${WORKDIR}/zion-data-init.sh \
                    ${D}${libdir}/zion-data-init/zion-data-init.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/zion-data-init.service \
                    ${D}${systemd_system_unitdir}/zion-data-init.service
}

FILES:${PN} += " \
    ${libdir}/zion-data-init/ \
    ${systemd_system_unitdir}/zion-data-init.service \
"
