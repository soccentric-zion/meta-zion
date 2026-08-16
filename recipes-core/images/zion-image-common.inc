# zion-image-common.inc — shared "completeness" layer for zion-image-<domain>
# variants. Each domain recipe does:
#
#     require recipes-core/images/zion-image.bb
#     require recipes-core/images/zion-image-common.inc
#     IMAGE_INSTALL:append = " <domain-specific packages>"
#
# This file is required AFTER zion-image.bb, so the appends below layer on
# top of the full Zion base (A/B OTA via SWUpdate, zion-connectivity,
# gRPC/Protobuf, hardware interface tools, zion user). It makes every
# domain variant a complete, self-contained image: SSH server, debug tools,
# a full command-line + build environment, monitoring/diagnostics, network
# utilities, and a broad Python toolchain.

# --- Production runtime foundation ------------------------------------------
IMAGE_INSTALL:append = " \
    dbus libpam shadow shadow-base \
    kmod udev util-linux \
    e2fsprogs e2fsprogs-resize2fs dosfstools parted \
    tzdata \
    systemd-analyze \
    logrotate rsyslog \
"

# SSH server + debug tools on every variant
IMAGE_FEATURES:append = " ssh-server-openssh tools-debug ptest-pkgs"

# Export development, source, profiling, and validation support to SDKs
SDKIMAGE_FEATURES:append = " dev-pkgs dbg-pkgs src-pkgs"
TOOLCHAIN_TARGET_TASK:append = " packagegroup-zion-middleware"

# --- Full command-line + native build environment ----------------------------
IMAGE_INSTALL:append = " \
    packagegroup-core-full-cmdline \
    packagegroup-core-buildessential \
    git make cmake pkgconfig autoconf automake libtool flex bison \
    gcc g++ gdb binutils diffutils patch cpio bc file gawk \
    bzip2 xz gzip tar unzip zip lz4 zstd \
"

# --- Python toolchain --------------------------------------------------------
IMAGE_INSTALL:append = " \
    python3 python3-pip python3-setuptools python3-wheel \
    python3-numpy python3-requests python3-pyyaml python3-jinja2 \
    python3-jsonschema python3-dateutil python3-six python3-pytz \
    python3-psutil python3-cryptography python3-dbus python3-pyserial \
    python3-periphery python3-pyusb python3-smbus2 python3-spidev \
    python3-gpiod python3-evdev python3-aiohttp \
    python3-pillow python3-matplotlib python3-sqlalchemy \
"

# --- Local data / serialization ----------------------------------------------
IMAGE_INSTALL:append = " \
    sqlite3 leveldb json-c jansson \
"

# --- Network client utilities + diagnostics ----------------------------------
IMAGE_INSTALL:append = " \
    curl wget rsync lsof socat netcat-openbsd \
    iproute2 iptables tcpdump nmap ethtool traceroute mtr ipcalc \
    net-tools iputils \
"

# --- Time sync, certs, crypto, entropy ---------------------------------------
# rng-tools-service is the rngd daemon (separate subpackage from the
# rng-tools CLI tools already listed here); it feeds /dev/hwrng into the
# kernel entropy pool via its default /etc/default/rng-tools config
# (EXTRA_ARGS="-r /dev/hwrng", unmodified upstream default). /dev/hwrng is
# backed by the ZynqMP hardware TRNG once CONFIG_HW_RANDOM_XILINX_PS is
# active (see recipes-kernel/linux/files/zion-base.cfg). haveged remains as
# a software/jitter-entropy source alongside it, not a replacement for it.
IMAGE_INSTALL:append = " \
    chrony ca-certificates openssl gnupg gnutls rng-tools rng-tools-service haveged \
"

# --- System monitoring, editors, shells -------------------------------------
IMAGE_INSTALL:append = " \
    htop tmux screen nano vim mc procps sysstat iotop \
    usbutils pciutils nvme-cli smartmontools \
"

# --- Profiling / tracing / debug helpers -------------------------------------
IMAGE_INSTALL:append = " \
    valgrind ltrace perf trace-cmd elfutils \
    logrotate rsyslog audit \
"

# --- Hardware I/O libraries --------------------------------------------------
IMAGE_INSTALL:append = " \
    libgpiod i2c-tools spitools libiio \
"

# --- VCU (video codec) — not applicable on this hardware ---------------------
# The Kria K26 SOM (used on KR260) does not implement the hardened VCU
# (H.264/H.265 encode/decode) block present on some larger Zynq
# UltraScale+ EG/EV parts (e.g. ZCU106/ZCU104). This is a silicon gap, not
# a missing recipe — no VCU packages are added here.

# --- DisplayPort — SoC-capable, board-not-wired -------------------------------
# The ZynqMP PS DisplayPort DMA/controller (dppsu/dpdma) is present in
# MACHINE_FEATURES for this SoC, but the KR260 carrier board does not route
# a physical DisplayPort connector (it's a headless robotics carrier). No
# Weston/DRM-KMS display stack is added here as a result — there is
# nothing to display to. Revisit if targeting a carrier board that exposes
# DP (e.g. a KV260/KD240 with a DP-capable carrier).
