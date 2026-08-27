SUMMARY = "Zion OS image for maritime deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the maritime vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for maritime: GNSS/navigation, time sync, telemetry
IMAGE_INSTALL:append = " \
    gpsd chrony \
    python3-numpy \
"
