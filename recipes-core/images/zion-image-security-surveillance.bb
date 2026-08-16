SUMMARY = "Zion OS image for security-surveillance deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the security-surveillance vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for security-surveillance
IMAGE_INSTALL:append = "     packagegroup-core-full-cmdline "
