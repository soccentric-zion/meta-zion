# meta-zion

Yocto/OpenEmbedded BSP layer for **Zion-OS** — the embedded Linux platform for
the AMD Xilinx **Zynq UltraScale+ MPSoC** (Kria KV260 Vision AI / KR260
Robotics Starter Kits), built against the Xilinx `rel-v2025.2` layer set with
A/B OTA updates via [swupdate](https://sbabic.github.io/swupdate/).

This repository **is** the OE layer. The build system (kas configs, Makefile,
Docker images, deploy tooling) lives in the [Zion-OS](https://github.com/soccentric-zion/Zion-OS)
repo, which pulls this layer in through `kas/repos/meta-zion.yaml`.

## Layer layout

```
conf/layer.conf              layer registration (BBFILE_COLLECTIONS meta-zion)
recipes-bsp/                 zion-hal, Zion U-Boot boot script (A/B slot logic)
recipes-kernel/              linux-xlnx bbappend + kernel config fragment
                             (zion-base.cfg: spidev, system monitor, TRNG,
                             NVMe, SPI CAN controllers)
recipes-connectivity/        zion-connectivity, zion-timesync, openssh
                             hardening drop-in (root SSH login disabled)
recipes-core/                zion-image (+ 20 domain variants), zion-data-init,
                             zion-services, packagegroups, systemd config
recipes-support/             zion-rpu-firmware, expand-rootfs
                             (single-partition layouts only)
recipes-swupdate/            zion-swupdate-config — runtime A/B OTA scripts,
                             boot watchdog, health check, fw_env config
scripts/lib/wic/             custom wic source plugins:
                               fslabel      — empty labelled ext4/vfat partitions
                               zion-bootimg — bootimg-partition compatible with
                                              meta-xilinx's wic (sector_size)
swupdate/                    sw-description + pre/post-install scripts packaged
                             into every .swu by the Zion-OS `make swu` target
wic/sdimage-zynqmp.wks       4-partition A/B SD card layout:
                               p1 512M FAT32 'boot'      BOOT.BIN, Image,
                                                         system.dtb, boot.scr,
                                                         A/B slot markers
                               p2 8G  ext4  'rootfs-a'   active slot
                               p3 8G  ext4  'rootfs-b'   standby slot
                               p4 512M ext4 'home'       persistent /home
```

## A/B OTA flow

1. `swupdate pre-install.sh` finds the inactive slot by partition label and
   backs up the shared kernel/DTB (`Image`, `system.dtb` → `.prev`).
2. The raw handler streams `rootfs.ext4.gz` into the inactive slot; the new
   kernel and DTB land in the boot partition.
3. `post-install.sh` sets the `/boot/slot_b` marker, drops `upgrade_pending`
   (arms the U-Boot rollback counter) and writes `/boot/boot_state`.
4. `boot.cmd.zion` (see `recipes-bsp/u-boot/`) selects the slot on every boot
   and rolls back automatically after 3 consecutive unconfirmed boots.
5. `swupdate-bootcheck.service` health-checks the new slot and confirms it
   (`/boot_ok`); `boot-watchdog.service` restores the previous kernel/DTB and
   clears the marker if the slot is unhealthy.

## U-Boot environment (fw_printenv / fw_setenv)

Kria SOMs always boot from QSPI, and the Zion U-Boot stores its environment
in the QSPI partitions "U-Boot storage variables" (0x2200000) and "...backup"
(0x2220000, redundant — survives a power cut mid-write). `fw_env.config`
points at `/dev/zion/uboot-env` and `/dev/zion/uboot-env-redund`, which
`60-zion-uboot-env.rules` symlinks from the QSPI MTD partition names so the
config survives device-tree partition renumbering.

## Operator account

`zion` / `zion` (TTY + SSH, member of `sudo` and `systemd-journal`). Set in
`recipes-core/images/zion-image.bb`; SSH policy in
`recipes-connectivity/openssh/` (root SSH login disabled, password auth for
the documented operator account only).

## Local (untracked) signing keys

SWU signing keys are generated with the Zion-OS repo's
`scripts/generate-swu-keys.sh` and are expected in `swupdate/keys/` — **never
committed** (see `.gitignore`).