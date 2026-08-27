# Zion-OS

Yocto-based embedded Linux OS for the AMD Xilinx Zynq UltraScale+ MPSoC platform.
Default target board: **KV260** (Kria KV260 Vision AI Starter Kit).

Built with [kas](https://kas.readthedocs.io/) against the Xilinx `rel-v2025.2` layer set.
Supports A/B OTA updates via [swupdate](https://sbabic.github.io/swupdate/).

---

## Table of Contents

- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Boards](#boards)
- [Build Targets](#build-targets)
- [OTA Update (.swu)](#ota-update-swu)
- [Flash to SD Card](#flash-to-sd-card)
- [KR260 Boot & Flash Instructions](#kr260-boot--flash-instructions)
- [SDK / eSDK](#sdk--esdk)
- [Layer Structure](#layer-structure)
- [Adding a New Board](#adding-a-new-board)

---

## Architecture

```
Zion-OS
├── kas/
│   ├── common.yaml          # Shared Yocto config (distro, image, caches)
│   ├── boards/
│   │   ├── kv260.yaml      # KV260 machine config (default)
│   │   └── kr260.yaml      # KR260 machine config
│   └── repos/
│       ├── poky.yaml              # Xilinx fork of poky (rel-v2025.2)
│       ├── meta-openembedded.yaml # Xilinx fork of OE (rel-v2025.2)
│       ├── meta-xilinx.yaml       # Zynq UltraScale+ BSP
│       ├── meta-xilinx-tools.yaml # Xilinx build tools (bootgen, FSBL, etc.)
│       ├── meta-kria.yaml         # Kria SOM / KR260 machine configs
│       ├── meta-swupdate.yaml     # swupdate OTA framework
│       └── meta-zion.yaml        # Custom Zion layer (image recipe, HAL, Client)
├── docker/
│   ├── yocto/     # kas build container (zion-kas)
│   ├── sdk/       # Yocto cross-toolchain SDK container (zion-sdk)
│   └── esdk/      # Yocto extensible SDK container (zion-esdk)
└── Makefile       # Top-level build orchestration
```

**Boot flow on KV260:**
```
SD/QSPI → FSBL (First Stage Boot Loader)
        → PMU Firmware
        → ATF (Arm Trusted Firmware / TF-A)
        → U-Boot
        → Linux (Image + zynqmp-smk-k26-revA-sck-kv-g-revB.dtb)
        → systemd → Zion-HAL, Zion-Client
```

**A/B OTA layout:**
```
Partition table (sdimage-zynqmp.wks in meta-zion):
  boot       FAT32   BOOT.BIN + Image + DTB
  rootfsA    ext4    slot A (active after initial flash)
  rootfsB    ext4    slot B (updated by swupdate, then activated)
  data       ext4    /var/lib/zion — persistent, never touched by OTA
```

---

## Prerequisites

### Host packages

```bash
# Ubuntu 22.04 / 24.04
sudo apt-get install -y \
    docker.io curl git make \
    bmap-tools   # for 'make flash'
```

Docker must be runnable without `sudo`. Add your user to the `docker` group and re-login if needed:

```bash
sudo usermod -aG docker $USER
```

### kas-container

Downloaded automatically on first build. Requires `curl` and Docker.

---

## Quick Start

```bash
# 1. Clone
git clone git@github.com:SiliconCentric/Zion-OS.git
cd Zion-OS

# 2. Build the KAS Docker image (once)
make kas-image

# 3. Build the Yocto image for KV260 (default board)
make image

# Build artifacts land in:
#   deploy/images/kv260_<timestamp>/  ← .wic.bz2 + .wic.bmap
```

Tab completion (optional):

```bash
make completion    # adds source line to ~/.bashrc
source ~/.bashrc
```

---

## Boards

| Board name | MACHINE           | SoC                          |
|------------|-------------------|------------------------------|
| `kv260`    | `k26-smk-kv-sdt`  | Zynq UltraScale+ XCZU5EV     |
| `kr260`    | `k26-smk-kr-sdt`  | Zynq UltraScale+ XCZU5EV     |

Add more boards by creating `kas/boards/<name>.yaml` (see [Adding a New Board](#adding-a-new-board)).

---

## Build Targets

```
make image  kv260          Build Yocto SD card image (default board: kv260)
make build  kv260          Alias for image
make rebuild-image kv260   cleansstate image recipe, then rebuild
make sdk    kv260          Cross-toolchain SDK installer → deploy/sdk/
make esdk   kv260          Extensible SDK installer     → deploy/esdk/
make shell  kv260          Interactive kas shell in the build container
make swu                   Build .swu OTA package       → deploy/swu/
make flash  kv260 DEVICE=/dev/sdX   Flash image to SD card

# Override image version for OTA package:
make swu SWU_VERSION=1.2.3

# Docker image management:
make kas-image             Build/rebuild KAS build container
make sdk-image             Full chain: SDK build → Docker image
make esdk-image            Full chain: eSDK build → Docker image

# Clean:
make clean                 Remove build/
make clean-all             Remove build/, downloads/, sstate-cache/
make clean-docker          Remove kas/sdk/esdk Docker images
```

---

## OTA Update (.swu)

```bash
make swu
# → build/<BOARD>-<VERSION>.swu
# → deploy/swu/<BOARD>_<TIMESTAMP>/<BOARD>-<VERSION>.swu
```

The `.swu` contains:
- `rootfs.ext4.gz` — compressed root filesystem for the inactive slot
- `Image` — kernel
- `zynqmp-smk-k26-revA-sck-kr-g-revB.dtb` — device tree blob
- `sw-description` — swupdate manifest (see `meta/meta-zion/swupdate/sw-description`)

**Signing:** If `meta/meta-zion/swupdate/keys/swupdate-private.pem` is present, the `.swu` is RSA-4096 signed. Generate keys with `scripts/generate-swu-keys.sh`.

**Apply OTA on device:**

```bash
# via swupdate web UI (port 8080) or CLI:
swupdate -i /path/to/kr260-<version>.swu
```

After a successful update, U-Boot boots the new slot. The health-check service
(`zion-health-check.service`) marks the slot confirmed by calling `fw_setenv upgrade_available 0`.

---

## Flash to SD Card

```bash
make flash kv260 DEVICE=/dev/sdX
# Uses bmaptool for fast, verified write.
```

**Identify your SD card device first:**

```bash
lsblk -d -o NAME,SIZE,MODEL
```

---

## KV260 Boot & Flash Instructions

### Initial SD card setup

1. Build the image: `make image kv260`
2. Flash: `make flash kv260 DEVICE=/dev/sdX`
3. Insert the SD card into the KV260 microSD slot.
4. Set the boot mode jumper on KV260 to SD card boot (SW1: `1100` — check the KV260 Rev1 board user guide).
5. Connect a USB-UART cable to the KV260 UART0/USB port (J4).
6. Open a serial terminal at **115200 8N1** (e.g. `minicom -D /dev/ttyUSB1 -b 115200`).
7. Power on; U-Boot and Linux boot messages appear on UART0.
8. Default login: configured by `meta-zion` (key-based SSH only; see layer README).

### QSPI boot (production)

BOOT.BIN (FSBL + PMU FW + ATF + U-Boot) can be programmed to QSPI flash for
standalone board boot without an SD card. Use `xsct` or `program_flash` from
Vitis/PetaLinux. Rootfs A/B and the data partition remain on eMMC or SD.

### Recovery (bricked board)

1. Connect JTAG via the KR260 JTAG/USB port.
2. Boot a known-good image from SD card using the SD boot mode override.
3. Or use `xsct` to directly load and run U-Boot over JTAG, then re-flash.

---

## SDK / eSDK

```bash
# Build cross-toolchain SDK (no Docker image):
make sdk kr260
# → deploy/sdk/kr260_<timestamp>/*.sh

# Build SDK + Docker image (for CI cross-compilation containers):
make sdk-image

# Build extensible SDK + Docker image:
make esdk-image
```

Use the SDK Docker image (`zion-sdk:latest`) to cross-compile Zion-HAL and Zion-Client
against the exact sysroot the image was built with.

---

## Layer Structure

| Layer               | Source                                    | Purpose                              |
|---------------------|-------------------------------------------|--------------------------------------|
| `meta`              | Xilinx/poky (rel-v2025.2)                 | OE core                              |
| `meta-poky`         | Xilinx/poky (rel-v2025.2)                 | Poky distro policy                   |
| `meta-oe`           | Xilinx/meta-openembedded (rel-v2025.2)    | Broad package set                    |
| `meta-python`       | Xilinx/meta-openembedded (rel-v2025.2)    | Python packages                      |
| `meta-networking`   | Xilinx/meta-openembedded (rel-v2025.2)    | Network utilities                    |
| `meta-multimedia`   | Xilinx/meta-openembedded (rel-v2025.2)    | Multimedia (optional)                |
| `meta-xilinx-core`  | Xilinx/meta-xilinx (rel-v2025.2)          | ZynqMP BSP, kernel, U-Boot, ATF      |
| `meta-xilinx-standalone` | Xilinx/meta-xilinx (rel-v2025.2)    | Bare-metal / FSBL recipes            |
| `meta-xilinx-tools` | Xilinx/meta-xilinx-tools (rel-v2025.2)    | bootgen, XSCT, Vitis integration     |
| `meta-kria`         | Xilinx/meta-kria (rel-v2025.2)            | Kria SOM + KR260 machine configs     |
| `meta-swupdate`     | sbabic/meta-swupdate (scarthgap)          | swupdate OTA framework               |
| `meta-zion`         | SiliconCentric/meta-zion (rel-v2025.2)    | Zion image, HAL, Client, swu config  |

---

## Adding a New Board

1. Create `kas/boards/<board-name>.yaml`:

```yaml
header:
  version: 14
  includes:
    - ../common.yaml

machine: <yocto-machine-name>    # from meta-kria or meta-xilinx-core

local_conf_header:
  wks: |
    WKS_FILE = "sdimage-zynqmp.wks"
```

2. Verify the machine name exists in `meta-kria` or `meta-xilinx-core`.

3. Build: `make image <board-name>`

---

## Changelog

### Zion-OS (initial — ported from Acadia-OS)

- **Platform:** Raspberry Pi → AMD Xilinx Zynq UltraScale+ MPSoC (KR260)
- **BSP layer:** `meta-raspberrypi` → `meta-xilinx` + `meta-xilinx-tools` + `meta-kria`
- **poky source:** upstream → Xilinx fork (`rel-v2025.2`)
- **meta-openembedded source:** upstream → Xilinx fork (`rel-v2025.2`)
- **Custom layer:** `meta-acadia` → `meta-zion`
- **Image recipe:** `acadia-image` → `zion-image`
- **Default board:** `rpi5` → `kr260` (machine: `kr260-robotics`)
- **Docker images:** `rpi5-kas`, `rpi5-sdk`, `rpi5-esdk` → `zion-kas`, `zion-sdk`, `zion-esdk`
- **Kernel image:** `kernel_2712.img` (RPi5-specific) → `Image` (standard arm64)
- **DTB:** `bcm2712-rpi-5-b.dtb` → `zynqmp-smk-k26-revA-sck-kr-g-revB.dtb`
- **U-Boot env tool:** `u-boot-fw-utils` → `libubootenv-bin`
- **Removed:** RPi-specific vars (`GPU_MEM`, `ENABLE_UART`, `RPI_USE_U_BOOT`, `rpi_arm64_defconfig`, `synaptics-killswitch` licence)
- **Removed:** All RPi board configs (rpi0–rpi5, cm1–cm5, rpi-armv7, rpi-armv8)
- **Removed:** `patch_health.py` (Acadia-Server specific)
- **Renamed:** `rpi-make-completion.bash` → `zion-make-completion.bash`
- **Updated:** `config` (HAL repository URL: Acadia-HAL → Zion-HAL)
- **Updated:** `req.md` (audit prompt: Acadia-OS → Zion-OS, RPi → KR260)
- **swupdate OTA:** A/B dual-rootfs structure preserved; sw-description path updated to `meta/meta-zion/swupdate/sw-description`
- **Boot flow:** RPi GPU-based boot (config.txt) → Zynq FSBL → PMU FW → ATF → U-Boot → Linux
