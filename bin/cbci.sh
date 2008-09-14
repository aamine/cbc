#!/bin/bash

TMPDIR=/tmp
TMPNAME="cflatexpr$$"

main() {
    local basedir="$(expand_path "$(dirname "$0")")"

    cd "$TMPDIR"
    trap "rm -f $TMPNAME*" EXIT
    cat <<EndSource > "$TMPNAME.cb"
import stdio;

int
main(int argc, char** argv)
{
    printf("%d\n", $1);
    return 0;
}
EndSource
    "$basedir/cbc" "$TMPNAME.cb" || exit 1
    "./$TMPNAME"
    st=$?
    echo "status: $st" 1>&2
    exit 0
}

expand_path() {
    local path="$1"

    if [ $(expr "$path" : "/") -eq 1 ]
    then
        echo "$path"
    else
        echo "$(pwd)/$path"
    fi
}

main "$@"
