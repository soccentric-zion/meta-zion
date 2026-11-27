SUMMARY = "Zion OS image for defense deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the defense vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for defense: secure comms, VPN, crypto
IMAGE_INSTALL:append = " \
    strongswan openvpn wireguard-tools \
    gnupg \
"
