SUMMARY = "Zion OS — production image for AMD Xilinx Zynq UltraScale+ MPSoC (KV260 / KR260)"
DESCRIPTION = "Full image with A/B OTA, gRPC/Protobuf, Zion-HAL, \
               hardware interface tooling, and connectivity."
HOMEPAGE = "https://github.com/soccentric-zion/Zion-OS"
LICENSE = "MIT"

# The device hostname is set to "zion" in recipes-core/base-files/base-files_%.bbappend
# (recipe-scope: an image-recipe assignment cannot reach base-files' datastore).

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
# NOTE: expand-rootfs must NOT be installed in the A/B OTA layout — the
# rootfs slots are fixed-size (rootfs-a/rootfs-b) and a resized active slot
# would overlap the standby slot.
IMAGE_INSTALL:append = " zion-data-init"

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

# Kria SOM monitoring — temperature/voltage/fan speed. The kernel fragment
# (recipes-kernel/linux) enables the ZynqMP system monitor (XILINX_AMS) and
# iio-hwmon, so `sensors` reports SOM temperature rails out of the box.
# meta-kria's fancontrol config manages the KV260/KR260 cooling fan.
IMAGE_INSTALL:append:k26-smk = " lmsensors-sensors lmsensors-config-kria-fancontrol"

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

# Operator account — username: zion, password: zion (TTY + SSH login)
# The data partition (/home) is created by zion-data-init on first boot,
# so use -M (--no-create-home) like the Acadia reference.
# NOTE: EXTRA_USERS_PARAMS values are expanded through several layers of
# shell (the set_user_group function and again inside `flock -c "..."`), so
# a shadow hash would get mangled. Instead create the user here and set the
# password directly in /etc/shadow afterwards (single controlled expansion).
# Hash is sha512crypt("zion", salt="zion"), i.e. login zion/zion.
# Groups: sudo — operator admin rights; systemd-journal — read access to
# the full persistent journal (req. R11.4: Zion-Client can forward logs).
inherit extrausers
EXTRA_USERS_PARAMS = "useradd -M -s /bin/bash -G sudo,systemd-journal zion"

# Shadow hash of "zion" (salt "zion"), precomputed so the rootfs postprocess
# below needs no shell command substitution (BitBake's shell parser chokes on
# $(...) inside ROOTFS_POSTPROCESS_COMMAND bodies).
ZION_SHADOW_HASH = '$6$zion$p2QLWg13mOPq.YMllRmpv6V4BjAlcfSBN87wHMUS5Be11BI7O2s0S0AYYoPKBIyZAcnZuPcYYOOw/Trchoq87/'

# "Day of last password change" for /etc/shadow, in days since 1970-01-01
# (2024-09-27). Any fixed non-zero day in the past works: with min=0 and
# max=99999 the password never expires and is never forced to change.
ZION_LASTCHG = "20000"

zion_user_password () {
    grep -q "^zion:" ${IMAGE_ROOTFS}/etc/passwd || return 0
    h='${ZION_SHADOW_HASH}'
    # Fields after the hash: lastchg:min:max:warn:inact:expire:flag.
    # lastchg MUST be non-zero — 0 means "change password at next login"
    # (pam_unix/sshd then reject the password and force a change loop).
    sed -ri "s|^zion:[^:]*:.*|zion:${h}:${ZION_LASTCHG}:0:99999:7:::|" ${IMAGE_ROOTFS}/etc/shadow
    bbnote "Set shadow password for user zion"
}
ROOTFS_POSTPROCESS_COMMAND:append = " zion_user_password"

# Mount the FAT boot partition (p1, label 'boot') and the persistent data
# partition (p4, label 'home') — both created at build time by the meta-zion
# wic plugins. p4 (home) is grown to fill the card on first boot by
# zion-data-init. LABEL= is used (never /dev/ names) so the same image works
# on KV260 (SD = mmcblk1) and KR260 (SD = sda via USB bridge).
zion_fstab_ab () {
    grep -q "LABEL=boot" ${IMAGE_ROOTFS}/etc/fstab || \
        echo "LABEL=boot  /boot  vfat  defaults  0  0" >> ${IMAGE_ROOTFS}/etc/fstab
    grep -q "LABEL=home" ${IMAGE_ROOTFS}/etc/fstab || \
        echo "LABEL=home  /home  ext4  defaults,nofail  0  2" >> ${IMAGE_ROOTFS}/etc/fstab
}
ROOTFS_POSTPROCESS_COMMAND:append = " zion_fstab_ab"
