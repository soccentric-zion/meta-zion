SUMMARY = "SWUpdate runtime configuration and A/B OTA helper scripts for KR260"
DESCRIPTION = "Installs swupdate.cfg, pre/post-install helper scripts, \
a boot-check service that confirms successful boot via fw_setenv."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://swupdate.cfg \
    file://pre-install.sh \
    file://post-install.sh \
    file://swupdate-bootcheck.sh \
    file://swupdate-bootcheck.service \
    file://fw_env.config \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "swupdate-bootcheck.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# fw_setenv / fw_printenv come from libubootenv-bin (added in IMAGE_INSTALL)
RDEPENDS:${PN} = "bash util-linux libubootenv-bin"

do_install() {
    # SWUpdate runtime config
    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/swupdate.cfg       ${D}${sysconfdir}/swupdate.cfg
    install -m 0644 ${WORKDIR}/fw_env.config       ${D}${sysconfdir}/fw_env.config

    # OTA helper scripts
    install -d ${D}${libdir}/kr260-swupdate
    install -m 0755 ${WORKDIR}/pre-install.sh      ${D}${libdir}/kr260-swupdate/pre-install.sh
    install -m 0755 ${WORKDIR}/post-install.sh     ${D}${libdir}/kr260-swupdate/post-install.sh
    install -m 0755 ${WORKDIR}/swupdate-bootcheck.sh \
                                                   ${D}${libdir}/kr260-swupdate/swupdate-bootcheck.sh

    # Systemd boot-check unit
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/swupdate-bootcheck.service \
                    ${D}${systemd_system_unitdir}/swupdate-bootcheck.service
}

FILES:${PN} += " \
    ${sysconfdir}/swupdate.cfg \
    ${sysconfdir}/fw_env.config \
    ${libdir}/kr260-swupdate \
    ${systemd_system_unitdir}/swupdate-bootcheck.service \
"
