SUMMARY = "Zion OS image for robotics deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the robotics vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for robotics: vision, motion control, CAN, telemetry
IMAGE_INSTALL:append = " \
    opencv gstreamer1.0-plugins-good \
    can-utils python3-numpy \
"
