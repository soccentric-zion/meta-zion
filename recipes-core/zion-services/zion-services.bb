SUMMARY = "Zion system health services (dmesg capture + disk usage monitor)"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://dmesg-capture.service \
    file://disk-usage-check.sh \
    file://disk-usage-check.service \
    file://disk-usage-check.timer \
"

inherit allarch systemd

RDEPENDS:${PN} = "systemd util-linux"

SYSTEMD_SERVICE:${PN} = " \
    dmesg-capture.service \
    disk-usage-check.service \
    disk-usage-check.timer \
"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install() {
    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/disk-usage-check.sh \
                    ${D}${sbindir}/disk-usage-check.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/dmesg-capture.service \
                    ${D}${systemd_system_unitdir}/dmesg-capture.service
    install -m 0644 ${WORKDIR}/disk-usage-check.service \
                    ${D}${systemd_system_unitdir}/disk-usage-check.service
    install -m 0644 ${WORKDIR}/disk-usage-check.timer \
                    ${D}${systemd_system_unitdir}/disk-usage-check.timer
}

FILES:${PN} = " \
    ${sbindir}/disk-usage-check.sh \
    ${systemd_system_unitdir}/dmesg-capture.service \
    ${systemd_system_unitdir}/disk-usage-check.service \
    ${systemd_system_unitdir}/disk-usage-check.timer \
"
