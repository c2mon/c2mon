#!/usr/bin/env bash

function usage {
  echo "usage: $0 VERSION";
}

[[ -z $1 ]] && usage && exit 1
VERSION=$1

LATEST_RELEASE=`git describe --tags`

if [[ ${VERSION} =~ "SNAPSHOT" ]]; then
  LATEST_SNAPSHOT=${VERSION}
fi

# Invoke mkdocs
mkdocs build --clean --site-dir=target/${VERSION}

# Generate versions.json
VERSIONS_FILE=target/versions.json

echo "{" > ${VERSIONS_FILE}
echo "  \"release\":  \"$LATEST_RELEASE\","  >> ${VERSIONS_FILE}
echo "  \"snapshot\": \"$LATEST_SNAPSHOT\","  >> ${VERSIONS_FILE}
echo "  \"versions\": [" >> ${VERSIONS_FILE}

# Include all released versions
for v in `git tag -l`;
  do echo "    \"${v}\"," >> ${VERSIONS_FILE}
done

# If this is a snapshot build, also include the snapshot version
if [[ ${VERSION} =~ "SNAPSHOT" ]]; then
  echo "    \"${VERSION}\"" >> ${VERSIONS_FILE}
fi

echo "  ]" >> ${VERSIONS_FILE}
echo "}" >> ${VERSIONS_FILE}

# If this is a snapshot build, copy the docs to the "snapshot" directory.
# Otherwise, copy them to the "latest" directory. Note that a symlink will not
# work here as the CERN AFS website server does not allow following them.
if [[ ${VERSION} =~ "SNAPSHOT" ]]; then
  cp -r target/${VERSION} target/snapshot
else
  cp -r target/${VERSION} target/latest
fi

