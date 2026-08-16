SUMMARY = "Expand root filesystem to fill the SD card on first boot"
DESCRIPTION = "Systemd one-shot service that grows the root partition \
and resizes the ext4 filesystem so all available SD card space is used."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://expand-rootfs.sh \
    file://expand-rootfs.service \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "expand-rootfs.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash parted e2fsprogs-resize2fs util-linux"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/expand-rootfs.sh ${D}${bindir}/expand-rootfs.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/expand-rootfs.service \
                    ${D}${systemd_system_unitdir}/expand-rootfs.service
}

FILES:${PN} += "${systemd_system_unitdir}/expand-rootfs.service"
