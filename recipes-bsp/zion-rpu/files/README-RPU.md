# RPU (Cortex-R5F) support — status

Zion-OS enables the **Linux-side** remoteproc/rpmsg plumbing for the
Zynq UltraScale+ MPSoC's dual-core Cortex-R5F RPU:

- Kernel config: `CONFIG_REMOTEPROC`, `CONFIG_ZYNQMP_R5_REMOTEPROC`,
  `CONFIG_RPMSG*`, `CONFIG_HWSPINLOCK` (see
  `recipes-kernel/linux/files/zion-base.cfg`, wired in via
  `recipes-kernel/linux/linux-xlnx_%.bbappend`).
- `zion-rpu-loader`: a systemd oneshot that, on boot, loads
  `/lib/firmware/zion-rpu0.elf` onto `remoteproc0` if present, and cleanly
  no-ops otherwise.

## What's a placeholder

**No RPU firmware image ships with Zion-OS.** A real firmware image is an
application-specific R5 program (e.g. a robotics control loop, sensor
fusion, or hard-real-time I/O handler) that doesn't exist yet — per
[`Zion/.claude/CLAUDE.md`](../../../../../.claude/CLAUDE.md), Zion has no
`Apps/` firmware tree (unlike Arches).

The upstream `openamp-fw-echo-testd` demo recipe family in
`meta-xilinx-tools` was evaluated as a stand-in and rejected: it requires
the deprecated `xsct` ESW workflow and explicitly raises
`bb.parse.SkipRecipe("OpenAMP is no longer supported when using xsct for
build.")` for our machine's SDT (system-device-tree) flow. Building it
would require force-overriding that guard, which is not a safe default for
a production OS layer.

Likewise, no Linux-userspace `libmetal`/`open-amp` RPC/rpmsg library
package is available from any layer already pulled by this project's `kas`
config — the Xilinx forks live behind a `dynamic-layers/openamp-layer`
gate that needs a dedicated `meta-openamp` layer we don't have, and no
generic OE recipe exists in `meta-openembedded` either. Adding that layer
is out of scope for a meta-zion-only change.

## To make this real

1. Cross-build a real R5 application (FreeRTOS or bare-metal) targeting
   `psu_cortexr5_0` for the `k26-smk-kr-sdt` machine, most likely via a
   future `Apps/` tree the way Arches has one.
2. Drop the resulting ELF into the rootfs as
   `/lib/firmware/zion-rpu0.elf` (e.g. via a new recipe's `do_install`, or
   for iteration, `scp` it onto a running board — `zion-rpu-loader` picks
   it up on the next boot with no other changes needed).
3. If Linux-side rpmsg userspace communication is needed, add a dedicated
   recipe in meta-zion that fetches `libmetal`/`open-amp` from the
   upstream OpenAMP project git and builds it with `cmake` — there's no
   ready-made package to lean on.
