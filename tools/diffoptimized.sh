#!/bin/bash

CBC=$(dirname $0)/../bin/cbc

for src in "$@"
do
    if $CBC $src >/dev/null 2>&1
    then
        asm=$(basename $src .cb).s
        echo "$src:"
        $CBC -O -S $src -o $asm.opt &&
        diff -u $asm $asm.opt
    fi
done
