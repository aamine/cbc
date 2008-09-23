#!/bin/bash

CBC=../bin/cbc

for src in "$@"
do
    if $CBC $src >/dev/null 2>&1
    then
        echo "$src:"
        $CBC -O -S $src -o ${src%%.cb}.s.opt &&
        diff -u ${src%%.cb}.s ${src%%.cb}.s.opt
    fi
done
