#!/bin/bash

main() {
    local pattern="."

    cd "$(dirname "$0")"
    . ./shunit.sh

    while [ $# -gt 0 ]
    do
        case "$1" in
        -t)
            # run specified tests
            [ -n "$2" ] || error_exit "-t option requires argument"
            local pattern="$2"
            shift; shift
            ;;
        --help)
            print_usage
            exit 0
            ;;
        -*)
            print_usage 1>&2
            exit 1
            ;;
        *)
            break
            ;;
        esac
    done
    if [ $# -eq 0 ]
    then
        load_tests test_*.sh
    else
        load_tests "$@"
    fi
    if [ "$pattern" = "." ]
    then
        # run all defined tests
        defined_tests | sort | run_tests
    else
        # run only matched tests
        defined_tests | grep "$pattern" | sort | run_tests
    fi
}

print_usage() {
    echo "Usage: $0 [-t PATTERN] [--help] [<file>...]"
}

load_tests() {
    local file
    for file in "$@"
    do
        . "./$file" || error_exit "load failed: $file"
    done
}

run_tests() {
    local pattern="$1"
    local f

    while read f
    do
        echo 1>&2
        print "$(echo $f | sed 's/^test_//')" 1>&2
        $f
    done
    shunit_report_result
}

defined_tests() {
    defined_functions | grep '^test_'
}

defined_functions() {
    typeset +f | awk '/^[^ ]+ \(\)/ { print $1 }'
}

main "$@"
