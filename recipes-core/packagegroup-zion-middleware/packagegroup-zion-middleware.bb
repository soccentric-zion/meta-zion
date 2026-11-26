SUMMARY = "Zion middleware package group — gRPC, Protobuf, and messaging support"
DESCRIPTION = "Meta-package pulling in the gRPC/Protobuf middleware stack \
used by Zion-HAL and Zion-Client. Add this single package group to \
IMAGE_INSTALL instead of listing individual middleware packages so the set \
stays consistent across every per-industry Zion image."
HOMEPAGE = "https://github.com/soccentric-zion/Zion-OS"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# Machine-specific: this packagegroup pulls in protobuf/grpc which are
# auto-renamed to SONAME-based names (libprotobuf25.8.0). An allarch
# packagegroup cannot depend on dynamically-renamed packages, so pin the
# packagegroup architecture to the machine. Must be set BEFORE inheriting
# packagegroup (the class snapshots PACKAGE_ARCH at inherit time).
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

PACKAGES = "${PN}"
ALLOW_EMPTY:${PN} = "1"

RDEPENDS:${PN} = " \
    protobuf \
    protobuf-lite \
    grpc \
    python3-grpcio \
    python3-protobuf \
"
