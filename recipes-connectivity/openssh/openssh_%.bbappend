# openssh_%.bbappend — Zion SSH hardening
#
# Ships a drop-in into /etc/ssh/sshd_config.d/ (oe-core's sshd_config has
# "Include /etc/ssh/sshd_config.d/*.conf" at the top, so this is applied
# first and wins over later defaults):
#
#   * PermitRootLogin no        — root is password-locked in the image
#                                 (root:*: in /etc/shadow); this also
#                                 blocks key-based root logins.
#   * PasswordAuthentication yes — documented lab operator login zion/zion
#                                 (see meta-zion/README.md). Production
#                                 deployments switch to key-only by
#                                 replacing this file.
#   * MaxAuthTries 4            — brute-force damping on exposed ports.

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://50-zion.conf"

do_install:append() {
    install -d ${D}${sysconfdir}/ssh/sshd_config.d
    install -m 0644 ${WORKDIR}/50-zion.conf \
                    ${D}${sysconfdir}/ssh/sshd_config.d/50-zion.conf
}

FILES:${PN} += "${sysconfdir}/ssh/sshd_config.d/50-zion.conf"