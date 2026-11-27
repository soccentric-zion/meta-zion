SUMMARY = "Zion OS image for security-surveillance deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the security-surveillance vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for security/surveillance: video, RTSP, analytics
IMAGE_INSTALL:append = " \
    gstreamer1.0-plugins-good gstreamer1.0-plugins-bad gstreamer1.0-rtsp-server \
    v4l-utils opencv \
"
