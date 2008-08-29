#!/usr/bin/ksh

main() {
    cd "$(dirname "$0")"
    . ./shunit.sh
    . ./test.sh

    case "$1" in
    -t)
        # run specified tests
        shift
        local pattern="$1"; shift
        defined_tests | grep "$pattern" | sort | run_tests
        ;;
    *)
        # run all defined tests
        defined_tests | sort | run_tests
        ;;
    esac
}

run_tests() {
    local pattern="$1"
    local f

    while read f
    do
        $f
    done
    shunit_report_result
}

defined_tests() {
    defined_functions | grep '^test_'
}

defined_functions() {
    typeset +f
}

main "$@"
