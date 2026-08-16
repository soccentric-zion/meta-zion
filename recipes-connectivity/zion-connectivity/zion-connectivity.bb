SUMMARY = "Zion connectivity — DHCP on Ethernet, WiFi (wpa_supplicant) and Bluetooth"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://10-eth.network \
    file://20-wlan.network \
    file://wpa_supplicant-wlan0.conf \
    file://80-zion-connectivity.preset \
"

RDEPENDS:${PN} = "wpa-supplicant bluez5 systemd"

do_install() {
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${WORKDIR}/10-eth.network \
                    ${D}${sysconfdir}/systemd/network/10-eth.network
    install -m 0644 ${WORKDIR}/20-wlan.network \
                    ${D}${sysconfdir}/systemd/network/20-wlan.network

    install -d ${D}${sysconfdir}/wpa_supplicant
    install -m 0600 ${WORKDIR}/wpa_supplicant-wlan0.conf \
                    ${D}${sysconfdir}/wpa_supplicant/wpa_supplicant-wlan0.conf

    install -d ${D}${systemd_unitdir}/system-preset
    install -m 0644 ${WORKDIR}/80-zion-connectivity.preset \
                    ${D}${systemd_unitdir}/system-preset/80-zion-connectivity.preset

    install -d ${D}${sysconfdir}
    ln -sf ../run/systemd/resolve/stub-resolv.conf \
           ${D}${sysconfdir}/resolv.conf
}

FILES:${PN} += " \
    ${sysconfdir}/systemd/network/ \
    ${sysconfdir}/wpa_supplicant/ \
    ${systemd_unitdir}/system-preset/ \
    ${sysconfdir}/resolv.conf \
"
