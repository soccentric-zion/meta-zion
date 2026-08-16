SUMMARY = "Zion Hardware Abstraction Layer"
DESCRIPTION = "HAL library for the Zion platform (Zynq UltraScale+ MPSoC), \
providing hardware abstraction interfaces."
HOMEPAGE = "https://github.com/soccentric-zion/Zion-HAL"
LICENSE = "CLOSED"
# Upstream is a private repo; no LICENSE file is shipped. BitBake requires
# a LIC_FILES_CHKSUM entry. We point at a stable local file.
LIC_FILES_CHKSUM = "file://security.md;md5=1606e3c70e7eb33ddfd7fa686c3b5ff0"

SRC_URI = " \
    git://github.com/soccentric-zion/Zion-HAL.git;protocol=ssh;user=git;branch=develop \
    file://security.md \
"
SRCREV = "a2acd66da1963d4166f2be6eb3ddec63d1ed9845"

PV = "1.0+git${SRCPV}"
# zion's pinned poky has no UNPACKDIR (older walnascar), so S stays under WORKDIR.
S = "${WORKDIR}/git"

inherit cmake

DEPENDS = "protobuf protobuf-native grpc grpc-native abseil-cpp pkgconfig-native"

OECMAKE_SOURCEPATH = "${S}"
# Force gRPC to be found via pkg-config rather than its CMake config: the gRPC
# config re-includes protobuf's config, which conflicts with the protobuf
# already found (protobuf 5.x libupb target mismatch) and breaks configure.
OECMAKE_EXTRA_OECMAKE = "-DGRPC_CPP_PLUGIN=${STAGING_BINDIR_NATIVE}/grpc_cpp_plugin"

do_configure:prepend() {
    # Skip the gRPC CMake config (which re-includes protobuf and breaks) and
    # always use the pkg-config fallback.
    sed -i -e 's/find_package(gRPC CONFIG QUIET)/set(gRPC_FOUND FALSE)/' \
           -e 's/if(gRPC_FOUND)/if(FALSE)/' ${S}/protos/CMakeLists.txt
}

CXXFLAGS:append = " -Wno-error=unused-result"
LDFLAGS:append = " -Wl,--copy-dt-needed-entries"

do_install() {
    install -d ${D}${libdir}
    find ${B}/lib -maxdepth 1 -name "*.so*" -exec install -m 0755 {} ${D}${libdir}/ \;
    find ${B}/lib -maxdepth 1 -name "*.a" -exec install -m 0644 {} ${D}${libdir}/ \;

    install -d ${D}${bindir}
    find ${B}/bin -maxdepth 1 -type f -exec install -m 0755 {} ${D}${bindir}/ \;

    install -d ${D}${includedir}/${PN}
    find ${S}/src -name "*.h" -o -name "*.hpp" | while read f; do
        install -m 0644 "$f" ${D}${includedir}/${PN}/
    done

    install -d ${D}${docdir}/${PN}
    install -m 0644 ${WORKDIR}/security.md ${D}${docdir}/${PN}/security.md
}

FILES:${PN}      += "${libdir}/*.so"
FILES:${PN}-dev  = "${includedir} ${libdir}/cmake ${libdir}/pkgconfig ${datadir}/cmake ${datadir}/${BPN}"
INSANE_SKIP:${PN} += "dev-so"

PRIVATE_LIBS = "libzion-hal*.so"
