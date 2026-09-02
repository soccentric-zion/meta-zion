# Enable RSA signed-image verification at compile time.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append = " file://zion-signing.cfg"
