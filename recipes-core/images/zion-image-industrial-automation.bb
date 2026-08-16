SUMMARY = "Zion OS image for industrial-automation deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the industrial-automation vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for industrial-automation
IMAGE_INSTALL:append = "     packagegroup-core-full-cmdline "
