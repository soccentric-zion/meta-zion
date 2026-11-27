SUMMARY = "Zion OS image for drones-uav deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the drones-uav vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for drones/UAV: GPS, telemetry, CAN, MQTT
IMAGE_INSTALL:append = " \
    gpsd can-utils \
    mosquitto python3-paho-mqtt \
    python3-numpy \
"
