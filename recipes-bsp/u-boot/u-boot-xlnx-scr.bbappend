# Zion A/B OTA boot script for Kria SDT machines (KV260 / KR260)
#
# Replaces the stock Xilinx boot.cmd.generic with a Zion version that adds
# A/B slot selection (slot marker on the boot partition) and a firmware-level
# rollback boot counter. The OTA flow it serves:
#
#   swupdate pre-install.sh   finds the inactive rootfs slot by label
#   swupdate raw handler      writes the new rootfs into the inactive slot
#   swupdate post-install.sh  touches /boot/slot_b (or removes it), writes
#                             /boot/upgrade_pending + /boot/boot_state
#   next boot                 boot.cmd.zion selects the slot, enforces the
#                             boot counter, consumes /boot_ok when present
#   swupdate-bootcheck.sh     health-checks the slot, confirms (/boot_ok)
#                             or rolls back (restores .prev kernel/DTB)

FILESEXTRAPATHS:prepend := "${THISDIR}/u-boot-xlnx-scr:"

SRC_URI:append:k26-smk-kr-sdt = " file://boot.cmd.zion"
SRC_URI:append:k26-smk-kv-sdt = " file://boot.cmd.zion"

# Select boot.cmd.zion (BOOTMODE + empty BOOTFILE_EXT -> boot.cmd.zion)
BOOTMODE:k26-smk-kr-sdt = "zion"
BOOTMODE:k26-smk-kv-sdt = "zion"
BOOTFILE_EXT:k26-smk-kr-sdt = ""
BOOTFILE_EXT:k26-smk-kv-sdt = ""