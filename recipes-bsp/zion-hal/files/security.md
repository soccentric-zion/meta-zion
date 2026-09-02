# Zion HAL security policy.
#
# The Zion HAL ships no setuid binaries and no file capabilities — neither
# the recipe nor any systemd unit adds them. Future contributors must NOT
# add chmod u+s, setcap, or AmbientCapabilities without review.
