# PL / FPGA fabric support — status

## Complete: static bitstream + overlay loading

`zion-fpga-loader` is a systemd oneshot that, on boot, loads
`/lib/firmware/zion.bit.bin` (+ optional `/lib/firmware/zion.dtbo`) via the
kernel `fpga_manager` framework, using `fpgautil` (already provided
upstream by `meta-xilinx-core`, `recipes-bsp/fpga-manager-script`; pulled
explicitly into `IMAGE_INSTALL` by `zion-image.bb` since it's not otherwise
guaranteed transitively installed).

**No bitstream is shipped by meta-zion.** A `.bit.bin` is a PL-team/
hardware-design artifact, not OS repo content — drop one into
`/lib/firmware/zion.bit.bin` on a deployed image (or add a recipe that
installs one) and the loader picks it up on the next boot with no other
changes needed.

This package is **not** added to `zion-image.bb` by default — like
`packagegroup-zion-cloud`, it's opt-in per deployment
(`IMAGE_INSTALL:append = " zion-fpga-loader"` in whichever
`zion-image-<domain>.bb` variant actually uses the PL fabric).

## Placeholder: partial reconfiguration (DFX)

Dynamic Function eXchange (loading a new region of the PL fabric at
runtime without a full reboot/reload) is **not implemented**. The upstream
`dfx-mgr` recipe's only board-specific firmware-detect script in
`meta-xilinx-tools` (`recipes-bsp/dfx-mgr/dfx-mgr_%.bbappend`) targets
`zcu106-zynqmp` — there is no equivalent wired up for `k26-smk-kr-sdt`.
Making DFX real for KR260 would require:

1. A PL design partitioned into static + reconfigurable regions (produced
   by the hardware/PL team, same as the full bitstream above).
2. A `k26-smk-kr-sdt`-specific firmware-detect script analogous to
   `zcu106-xlnx-firmware-load`, contributed as a `dfx-mgr` bbappend.

Neither exists today; this is flagged so it isn't silently missing.
