SUMMARY = "Zion OS image for telecom deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the telecom vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for telecom: edge networking, firewall, VPN
IMAGE_INSTALL:append = " \
    iptables tcpdump nmap \
    strongswan \
"
