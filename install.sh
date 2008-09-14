#!/bin/bash

version=1.0.0
prefix="${1:-/usr/local/cbc/$version}"

invoke() {
    echo "$@"
    if ! "$@"
    then
        echo "install failed." 1>&2
        exit 1
    fi
}

echo "version=$version"
echo "prefix=$prefix"
invoke mkdir -p "$prefix/bin"
invoke cp bin/cbc bin/cbci "$prefix/bin
invoke mkdir -p "$prefix/lib"
invoke cp cbc.jar lib/libcbc.o "$prefix/lib"
echo "cbc successfully installed as $prefix/bin/cbc"
