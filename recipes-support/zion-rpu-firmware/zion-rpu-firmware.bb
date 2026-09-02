SUMMARY = "Zion RPU (Cortex-R5) firmware lifecycle manager"
DESCRIPTION = "Manages the lifecycle of firmware running on the Zynq UltraScale+ \
RPU (dual-core Cortex-R5) via the Linux remoteproc framework. Firmware ELF files \
are staged into /lib/firmware at runtime (not baked into the board) and can be \
loaded, started, stopped, and reported on via the zion-rpu-firmware tool."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://zion-rpu-firmware.sh \
    file://zion-rpu-firmware.service \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "zion-rpu-firmware.service"
# Not auto-enabled: the service only runs when the autostart flag is set
# (/var/lib/zion-rpu-firmware/autostart), so firmware is not started unless
# explicitly requested.
SYSTEMD_AUTO_ENABLE:${PN} = "disable"

RDEPENDS:${PN} = "bash"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/zion-rpu-firmware.sh ${D}${bindir}/zion-rpu-firmware

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/zion-rpu-firmware.service \
                    ${D}${systemd_system_unitdir}/zion-rpu-firmware.service
}

FILES:${PN} += "${systemd_system_unitdir}/zion-rpu-firmware.service"
