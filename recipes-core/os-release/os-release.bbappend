# os-release_%.bbappend — identify this image as "Zion OS".
DISTRO_NAME  = "Zion OS"
DISTRO_VERSION = "${PV}"
PRETTY_NAME = "Zion OS ${PV}"
OS_RELEASE_FIELDS:append = " ID_LIKE"
ID_LIKE = "poky"

do_install:append() {
    cat > ${D}${sysconfdir}/os-release <<OSEOF
ID=zion
ID_LIKE=poky
NAME="Zion OS"
VERSION="${PV}"
VERSION_ID="${PV}"
PRETTY_NAME="Zion OS ${PV}"
HOME_URL="https://zion.dev/"
SUPPORT_URL="https://zion.dev/support"
BUG_REPORT_URL="https://zion.dev/issues"
OSEOF
}
