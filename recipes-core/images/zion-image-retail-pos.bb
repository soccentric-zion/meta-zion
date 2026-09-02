SUMMARY = "Zion OS image for retail-pos deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the retail-pos vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for retail/POS: display, camera/barcode, imaging
IMAGE_INSTALL:append = " \
    gstreamer1.0-plugins-good v4l-utils \
    python3-pillow \
"
