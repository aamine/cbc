#!/bin/bash

main() {
    if [ $# -eq 0 ]
    then
        run_class TestAll
    else
        for t in "$@"
        do
            run_class $t
        done
    fi
}

run_java() {
    java -classpath .:../lib/cbc.jar:./junit-4.5.jar "$@"
}

main "$@"
