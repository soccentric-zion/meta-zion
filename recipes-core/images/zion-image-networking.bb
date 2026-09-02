SUMMARY = "Zion OS image for networking deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the networking vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for networking: firewall, VPN, diagnostics
IMAGE_INSTALL:append = " \
    iptables tcpdump nmap \
    strongswan openvpn wireguard-tools \
"
