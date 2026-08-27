SUMMARY = "Zion OS — production image for AMD Xilinx Zynq UltraScale+ MPSoC (KV260 / KR260)"
DESCRIPTION = "Full image with A/B OTA, gRPC/Protobuf, Zion-HAL, \
               hardware interface tooling, and connectivity."
HOMEPAGE = "https://github.com/SiliconCentric/Zion-OS"
LICENSE = "MIT"

# Base on core-image-full-cmdline (provides bash, sshd-openssh,
# package-management, base IMAGE_INSTALL framework). This is the canonical
# base for all Zion image variants.
require recipes-extended/images/core-image-full-cmdline.bb
IMAGE_FEATURES += "ssh-server-openssh"

# OTA / swupdate
# zion-swupdate-config implements the inactive-slot, boot-selection, and
# rollback flow for sdimage-zynqmp.wks. Install on all K26 A/B machines
# (KV260 and KR260 both use the k26-smk machine override).
IMAGE_INSTALL:append:k26-smk = " swupdate swupdate-www zion-swupdate-config"

# Home partition init (persistent /home on the data partition)
IMAGE_INSTALL:append = " zion-data-init"

# Root filesystem expansion (single-partition deployments)
IMAGE_INSTALL:append = " expand-rootfs"

# Connectivity
IMAGE_INSTALL:append = " \
    zion-connectivity iw rfkill bluez5-dev \
"

# gRPC + Protobuf via packagegroup-zion-middleware
IMAGE_INSTALL:append = " packagegroup-zion-middleware sudo"

# Cloud/telemetry package group (not installed by default; add via
# packagegroup-zion-cloud when deployments need MQTT/NTP/cloud connectivity)
# IMAGE_INSTALL:append = " packagegroup-zion-cloud"

# Hardware interface tools
IMAGE_INSTALL:append = " \
    i2c-tools python3-smbus python3-smbus2 \
    spitools python3-spidev \
    libgpiod libgpiod-tools python3-gpiod \
    picocom minicom python3-pyserial \
    usbutils pciutils nvme-cli sg3-utils hdparm mtd-utils \
    can-utils \
    lshw ethtool devmem2 cpupower watchdog sysstat iotop strace \
    v4l-utils evtest python3-periphery vim \
"

# Vision AI / multimedia stack (KV260 Vision AI Starter Kit)
# The KV260 machine config (k26-smk-kv-sdt) already enables the Mali-400 GPU
# (mali400), the Video Codec Unit (vcu), FPGA overlay support (fpga-overlay),
# and hardware codecs (hwcodecs / libvcu-omxil). This adds the GStreamer
# pipeline + VCU OMX plugins so those features are usable on the image.
IMAGE_INSTALL:append:k26-smk = " packagegroup-xilinx-gstreamer"

# FPGA overlay / bitstream loading (Dynamic Function eXchange)
# fpga-manager-script — fpgautil: load/unload bitstreams via kernel fpga-manager
# dfx-mgr   — Dynamic Function eXchange manager daemon + fw-load service
# xmutil    — Kria overlay management tool (load/unload/list overlays)
# These provide the runtime mechanism to load FPGA bitstreams (overlays) onto
# the PL fabric without rebooting.
IMAGE_INSTALL:append:k26-smk = " fpga-manager-script dfx-mgr xmutil"

# RPU (Cortex-R5) firmware lifecycle management via OpenAMP / remoteproc
# packagegroup-openamp provides libmetal, open-amp, rpmsg tools and the
# remoteproc kernel modules needed to load/start/stop R5 firmware at runtime.
# zion-rpu-firmware adds the lifecycle management service (see below).
IMAGE_INSTALL:append:k26-smk = " packagegroup-openamp zion-rpu-firmware"

# Zion stack
IMAGE_INSTALL:append = " zion-hal zion-services"

# Target debugging and profiling
IMAGE_FEATURES += "tools-debug"
IMAGE_INSTALL:append = " gdbserver perf trace-cmd valgrind"

# Ptest package and runner support
IMAGE_FEATURES += "ptest-pkgs"

# Operator account — locked, SSH-key access only
# The data partition (/home) is created by zion-data-init on first boot,
# so use -M (--no-create-home) like the Acadia reference.
inherit extrausers
EXTRA_USERS_PARAMS = "\
    useradd -M -s /bin/bash -G sudo zion; \
    usermod -L zion; \
"
