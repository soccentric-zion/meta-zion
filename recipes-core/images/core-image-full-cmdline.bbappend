# OTA via SWUpdate — A/B rootfs slot switching, web UI on :8080
IMAGE_INSTALL:append = " \
    swupdate \
    kr260-swupdate-config \
"

# Persistent /home partition init — creates /home/zion on first boot after
# the empty ext4 home partition (p4) is mounted over /home.
IMAGE_INSTALL:append = " kr260-home-init"

# Connectivity — GigE DHCP via systemd-networkd
# KR260 has no on-board WiFi or Bluetooth; networkd + resolved only.
IMAGE_INSTALL:append = " \
    kr260-connectivity \
"

# gRPC + Protocol Buffers (C++ runtime, compiler plugin, and Python bindings)
IMAGE_INSTALL:append = " \
    protobuf \
    protobuf-compiler \
    protobuf-lite \
    grpc \
    python3-grpcio \
    python3-protobuf \
    sudo \
"

# --- Hardware interface tools ------------------------------------------------

# I2C
IMAGE_INSTALL:append = " \
    i2c-tools \
    python3-smbus \
    python3-smbus2 \
"

# SPI
IMAGE_INSTALL:append = " \
    spitools \
    python3-spidev \
"

# GPIO
IMAGE_INSTALL:append = " \
    libgpiod \
    libgpiod-tools \
    python3-gpiod \
"

# UART / serial
IMAGE_INSTALL:append = " \
    picocom \
    minicom \
    python3-pyserial \
"

# USB
IMAGE_INSTALL:append = " \
    usbutils \
"

# PCI / NVMe / SCSI / storage
IMAGE_INSTALL:append = " \
    pciutils \
    nvme-cli \
    sg3-utils \
    hdparm \
    mtd-utils \
"

# CAN bus
IMAGE_INSTALL:append = " \
    can-utils \
"

# System / hardware inspection
IMAGE_INSTALL:append = " \
    lshw \
    usbutils \
    ethtool \
    devmem2 \
    cpupower \
    watchdog \
    sysstat \
    iotop \
    strace \
    v4l-utils \
    evtest \
    python3-periphery \
"

# Add local user 'zion' with password 'zion' and sudo access
inherit extrausers

EXTRA_USERS_PARAMS = "\
    useradd -m -s /bin/bash -G sudo zion; \
    usermod -p '\$6\$8.kfghMSYkTHsFqs\$fKQIEp.xMjitUYt9yXjEZBUvKqUOQvxekn5.9hWP9ewiLFSI0LQi5OpaDNfSqh4OU.tzN7co7olMAYg53/kCP0' zion; \
"
