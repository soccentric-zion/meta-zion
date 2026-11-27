#!/bin/bash
# generate-swu-keys.sh — generate the SWUpdate RSA-4096 keypair.
#
# Workflow:
#   1. The PRIVATE key (swupdate-private.pem) is committed ONLY to the
#      build/CI vault. It is .gitignored in meta/meta-zion/swupdate/keys/.
#   2. The PUBLIC key is committed in two places:
#        - meta/meta-zion/recipes-swupdate/zion-swupdate-config/files/swupdate-public.pem
#      (committed to the repo, baked into every image at
#       /etc/swupdate/swupdate-public.pem).
#
# On a developer workstation:
#     ./scripts/generate-swu-keys.sh
# This writes the private key to the local swupdate/keys/ directory
# and updates the public key in the recipe's files/ directory. NEVER
# commit the private key.

set -euo pipefail

PRIV="meta/meta-zion/swupdate/keys/swupdate-private.pem"
PUB="meta/meta-zion/recipes-swupdate/zion-swupdate-config/files/swupdate-public.pem"

BITS=4096

if [ -f "${PRIV}" ]; then
    echo "ERROR: ${PRIV} already exists.  Refusing to overwrite." >&2
    echo "       Delete it first if you really want to regenerate." >&2
    exit 1
fi

mkdir -p "$(dirname "${PRIV}")"
mkdir -p "$(dirname "${PUB}")"

echo "Generating RSA-${BITS} keypair ..."
openssl genrsa -out "${PRIV}" "${BITS}"
openssl rsa -in "${PRIV}" -pubout -out "${PUB}"
echo "Done."
echo "  Private: ${PRIV}  (commit to CI vault ONLY)"
echo "  Public : ${PUB}   (committed to repo)"
