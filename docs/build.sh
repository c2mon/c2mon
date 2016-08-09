#!/usr/bin/env bash

function usage {
  echo "usage: $0 VERSION";
}

[[ -z $1 ]] && usage && exit 1
VERSION=$1

# Invoke mkdocs
mkdocs build --clean --site-dir=target/${VERSION}

# Generate versions.json from all git tags, plus current version
VERSIONS_FILE=target/versions.json

echo '[' > ${VERSIONS_FILE}
for v in `git tag -l`;
  do echo "  \"${v}\"," >> ${VERSIONS_FILE};
done
echo "  \"${VERSION}\"" >> ${VERSIONS_FILE};
echo ']' >> ${VERSIONS_FILE}
