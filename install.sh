#!/bin/bash

prefix="${1:-/usr/local/cbc}"
BINS="bin/cbc"
LIBS="lib/cbc.jar lib/libcbc.a"

main()
{
    if ! [[ -f lib/cbc.jar && -f lib/libcbc.a ]]
    then
        echo "lib/cbc.jar and lib/libcbc.a do not exist.  Build it first" 1>&2
        exit 1
    fi
    echo "prefix=$prefix"
    invoke mkdir -p "$prefix/bin"
    invoke install -m755 $BINS "$prefix/bin"
    invoke mkdir -p "$prefix/lib"
    invoke cp $LIBS "$prefix/lib"
    invoke rm -rf "$prefix/import"
    invoke cp -r import "$prefix/import"
    echo "cbc successfully installed as $prefix/bin/cbc"
}

invoke()
{
    echo "$@"
    if ! "$@"
    then
        echo "install failed." 1>&2
        exit 1
    fi
}

main "$@"
