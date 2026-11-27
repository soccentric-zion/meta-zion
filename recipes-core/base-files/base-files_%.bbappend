# Every Zion device identifies itself as "zion": /etc/hostname, the
# 127.0.1.1 /etc/hosts entry and the DHCP host-name option (sent by
# systemd-networkd) all derive from the 'hostname' variable of base-files.
#
# This must be a bbappend (recipe scope) — a hostname:pn-base-files
# assignment in an image recipe would not reach base-files' datastore.
hostname = "zion"