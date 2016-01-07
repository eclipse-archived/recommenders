#! /bin/bash -e

REPO_FORMAT=format-5

if [ -n "$1" ]; then
    REPO_DIR=$1
else
    read -p "Please enter a location for the snippet repository: " REPO_DIR
fi

if [[ -e "${REPO_DIR}" ]]; then
   >&2 echo
   >&2 echo "Cannot create snippet repository: '${REPO_DIR}' already exists."
   exit 1
fi

if [ -n "$2" ]; then
    REPO_DESCRIPTION=$2
else
    read -p "Please enter a short description of the snippet repository (<= 25 chars): " REPO_DESCRIPTION
fi

git init --bare "${REPO_DIR}"
pushd "${REPO_DIR}" > /dev/null
git symbolic-ref HEAD refs/heads/${REPO_FORMAT}
echo "${REPO_DESCRIPTION}" > description
popd > /dev/null

CLONE_DIR=$(mktemp -d -t $(basename "${REPO_DIR}").XXXXXX)
git clone --quiet "${REPO_DIR}" "${CLONE_DIR}"
pushd "${CLONE_DIR}" > /dev/null
git checkout -b ${REPO_FORMAT}
cat > .gitignore <<EOF
# Ignore every file except for this one and the actual snippets
*
!.gitignore
!snippets/
!snippets/*
EOF
git add .gitignore
git commit --message "Initial commit (${REPO_FORMAT})"
git push origin ${REPO_FORMAT}
popd > /dev/null
rm -rf "${CLONE_DIR}"
