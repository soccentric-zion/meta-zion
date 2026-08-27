SUMMARY = "Zion OS image for energy deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the energy vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for energy: SCADA/Modbus, MQTT, local DB
IMAGE_INSTALL:append = " \
    libmodbus mosquitto python3-paho-mqtt \
    sqlite3 \
"
