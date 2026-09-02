SUMMARY = "Zion OS image for test-measurement deployments"
DESCRIPTION = "Full-featured embedded Linux image for the Zynq UltraScale+ platform with A/B OTA, gRPC, and hardware interface tools, tuned for the test-measurement vertical."
LICENSE = "MIT"

require recipes-core/images/zion-image.bb
require recipes-core/images/zion-image-common.inc

# Domain-specific packages for test & measurement: data acquisition, IIO, analysis
IMAGE_INSTALL:append = " \
    libiio sqlite3 \
    python3-numpy python3-pandas \
"
