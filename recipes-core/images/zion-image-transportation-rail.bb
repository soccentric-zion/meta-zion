SUMMARY = "Zion OS image for transportation-rail deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the transportation-rail vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for rail transport: signaling, CAN, GNSS, telemetry
IMAGE_INSTALL:append = " \
    can-utils gpsd \
    mosquitto python3-paho-mqtt \
"
