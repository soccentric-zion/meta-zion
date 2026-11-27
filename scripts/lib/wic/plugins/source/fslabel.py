#
# Zion A/B OTA — empty formatted partition wic source plugin
#
# SPDX-License-Identifier: MIT
#
# The stock 'empty' plugin creates UNFORMATTED partitions (no filesystem,
# no label). Zion's A/B OTA layout needs rootfs-b and the /home data
# partition to exist as properly labelled filesystems in the .wic image
# itself, so that:
#
#   - /etc/fstab can mount the data partition via LABEL=home on first boot
#   - SWUpdate's pre-install script can locate the inactive slot via
#     lsblk LABEL (rootfs-a / rootfs-b)
#
# WKS usage:
#   part --source fslabel --sourceparams="label=rootfs-b" \
#        --fstype=ext4 --fixed-size 8192M
#
import logging
import os

from wic.pluginbase import SourcePlugin
from wic.misc import exec_native_cmd
from wic import WicError

logger = logging.getLogger('wic')


class FSLabelPartitionPlugin(SourcePlugin):
    """
    Create an empty, formatted partition with a filesystem label.

    Supports --fstype ext2/3/4 (mke2fs) and vfat (mkfs.vfat). The
    filesystem label comes from --sourceparams "label=..." and falls back
    to the --label wks option. Requires --fixed-size.
    """

    name = 'fslabel'

    @classmethod
    def do_prepare_partition(cls, part, source_params, cr, cr_workdir,
                             oe_builddir, bootimg_dir, kernel_dir,
                             rootfs_dir, native_sysroot, sector_size=None):
        if part.fixed_size == 0:
            raise WicError("fslabel plugin requires --fixed-size")

        # part.fixed_size is expressed in KiB (see wic.ksparser.sizetype)
        size_bytes = part.fixed_size * 1024

        label = source_params.get('label', '') or part.fslabel or ''
        if not label:
            raise WicError("fslabel: no label given (use --sourceparams=\"label=...\")")

        fsimg = os.path.join(cr_workdir, 'fslabel-%s.%s' % (label, part.fstype))
        with open(fsimg, 'wb') as f:
            f.truncate(size_bytes)

        if part.fstype.startswith('ext'):
            # -F: force on regular file; lazy_* keep image creation fast
            cmd = "mke2fs -F -q -t %s -L %s -E lazy_itable_init=0,lazy_journal_init=0 %s" % \
                  (part.fstype, label, fsimg)
        elif part.fstype in ('vfat', 'msdos'):
            cmd = "mkfs.vfat -F 32 -S 512 -n %s %s" % (label, fsimg)
        else:
            raise WicError("fslabel: unsupported fstype '%s'" % part.fstype)

        exec_native_cmd(cmd, native_sysroot)

        part.size = part.fixed_size          # KiB, for wic accounting
        part.source_file = fsimg

        logger.info("fslabel: created %s filesystem '%s' (%d MiB)",
                    part.fstype, label, part.fixed_size // 1024)