SUMMARY     = "SWUpdate runtime configuration and A/B OTA helper scripts for Zynq KR260"
DESCRIPTION = "Installs swupdate.cfg, slot-detection / boot-slot scripts, a \
               systemd drop-in that enables the Mongoose web UI on port 8080, \
               boot-watchdog for rollback counter, health-check service, and \
               startup wrapper with optional signing + web UI auth."
LICENSE     = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://swupdate.cfg \
    file://swupdate-bootcheck.sh \
    file://swupdate-bootcheck.service \
    file://swupdate-webui.conf \
    file://boot-watchdog.sh \
    file://boot-watchdog.service \
    file://swupdate-start.sh \
    file://bootcount-reset.sh \
    file://bootcount-reset.service \
    file://swupdate-public.pem \
"


inherit systemd

SYSTEMD_SERVICE:${PN} = " \
    swupdate-bootcheck.service \
    boot-watchdog.service \
    bootcount-reset.service \
"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "swupdate bash util-linux openssl libubootenv"

do_install() {
    # swupdate main config
    install -d ${D}${sysconfdir}/swupdate
    install -m 0644 ${WORKDIR}/swupdate.cfg \
                    ${D}${sysconfdir}/swupdate/swupdate.cfg

    # A/B slot helper scripts
    install -d ${D}${libdir}/swupdate
    install -m 0755 ${WORKDIR}/swupdate-bootcheck.sh \
                    ${D}${libdir}/swupdate/swupdate-bootcheck.sh
    install -m 0755 ${WORKDIR}/bootcount-reset.sh \
                    ${D}${libdir}/swupdate/bootcount-reset.sh

    # Boot watchdog
    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/boot-watchdog.sh \
                    ${D}${sbindir}/boot-watchdog.sh

    # swupdate startup wrapper
    install -m 0755 ${WORKDIR}/swupdate-start.sh \
                    ${D}${sbindir}/swupdate-start.sh

    # systemd units
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/swupdate-bootcheck.service \
                    ${D}${systemd_system_unitdir}/swupdate-bootcheck.service
    install -m 0644 ${WORKDIR}/boot-watchdog.service \
                    ${D}${systemd_system_unitdir}/boot-watchdog.service
    install -m 0644 ${WORKDIR}/bootcount-reset.service \
                    ${D}${systemd_system_unitdir}/bootcount-reset.service

    # Drop-in: replace ExecStart with swupdate-start.sh wrapper
    install -d ${D}${systemd_system_unitdir}/swupdate.service.d
    install -m 0644 ${WORKDIR}/swupdate-webui.conf \
                    ${D}${systemd_system_unitdir}/swupdate.service.d/webui.conf

    # SWUpdate RSA public key for signature verification
    install -m 0644 ${WORKDIR}/swupdate-public.pem \
                    ${D}${sysconfdir}/swupdate/swupdate-public.pem
}

FILES:${PN} += " \
    ${sysconfdir}/swupdate/ \
    ${libdir}/swupdate/ \
    ${sbindir}/boot-watchdog.sh \
    ${sbindir}/swupdate-start.sh \
    ${systemd_system_unitdir}/swupdate-bootcheck.service \
    ${systemd_system_unitdir}/boot-watchdog.service \
    ${systemd_system_unitdir}/bootcount-reset.service \
    ${systemd_system_unitdir}/swupdate.service.d/ \
    ${sysconfdir}/swupdate/swupdate-public.pem \
"
