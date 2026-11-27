SUMMARY = "Zion OS image for automotive deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the automotive vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for automotive: CAN bus, vision/ADAS, telemetry
IMAGE_INSTALL:append = " \
    can-utils opencv \
    gstreamer1.0-plugins-good \
    python3-numpy \
"
