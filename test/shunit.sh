#
# $Id: shunit.sh 1096 2006-11-02 12:31:46Z aamine $
#
# Simple Unit Test Framework for Shell Scripts
#

n_tests=0
n_failed=0

shunit_begin_test() {
    shunit_progress
    n_tests=`expr $n_tests "+" 1`
}

shunit_test_failed() {
    n_failed=`expr $n_failed "+" 1`
}

shunit_progress() {
    print '.'
}

shunit_invoke() {
    if [ "$SHUNIT_VERBOSE" = true ]
    then
        "$@"
    else
        "$@" >/dev/null 2>&1
    fi
}

shunit_report_result() {
    rm -rf tc.*
    echo
    if [ "$n_failed" = "0" ]
    then
        echo "PASS ($n_tests tests) -- `hostname` / `uname -srm`"
        exit 0
    else
        echo "FAIL ($n_failed/$n_tests failed) -- `hostname` / `uname -srm`"
        exit 1
    fi
}

error_exit() {
    echo "$0: error: $1"
    exit 1
}

if [ `/bin/echo -n x` = "-n" ]
then
    print() { /bin/echo "$1"'\c'; }
else
    print() { /bin/echo -n "$1"; }
fi

assert_status() {
    shunit_begin_test
    expected=$1; shift
    shunit_invoke "$@"
    really=$?
    assert_not_coredump "$1" || return
    if [ "$really" != "$expected" ]
    then
        echo "shunit[$@]: status $expected expected but was: $really"
        shunit_test_failed
        return 1
    fi
    return 0
}

assert_error() {
    shunit_begin_test
    shunit_invoke "$@"
    really=$?
    assert_not_coredump "$1" || return
    if [ "$really" = "0" ]
    then
        echo "shunit[$@]: non-zero status expected but was: $really"
        shunit_test_failed
        return 1
    fi
    return 0
}

assert_eq() {
    shunit_begin_test
    expected="$1"
    really="$2"
    if [ "$really" != "$expected" ]
    then
        echo "shunit: <$expected> expected but is: <$really>"
        shunit_test_failed
        return 1
    fi
    return 0
}

assert_equal() {
    shunit_begin_test
    _f="no"
    excmd=$1
    mycmd=$2
    eval "$excmd" >tc.out.expected 2>tc.err.expected
    eval "$mycmd" >tc.out.real     2>tc.err.real
    assert_not_coredump "$mycmd" || return
    cmp tc.out.real tc.out.expected >/dev/null 2>&1
    if [ "$?" != "0" ]
    then
        echo "stdout differ: \"$excmd\" and \"$mycmd\""
        diff -u tc.out.expected tc.out.real
        echo "----"
        _f="yes"
    fi
    cmp tc.err.real tc.err.expected >/dev/null 2>&1
    if [ "$?" != "0" ]
    then
        echo "stderr differ: \"$excmd\" and \"$mycmd\""
        diff -u tc.err.expected tc.err.real
        echo "----"
        _f="yes"
    fi
    if [ "$_f" = "yes" ]
    then
        shunit_test_failed
        return 1
    fi
    return 0
}

assert_equal_stdout() {
    shunit_begin_test
    _f="no"
    excmd=$1; shift
    mycmd=$1; shift
    eval "$excmd" >tc.out.expected 2>/dev/null
    eval "$mycmd" >tc.out.real     2>/dev/null
    assert_not_coredump "$mycmd" || return
    cmp tc.out.real tc.out.expected >/dev/null 2>&1
    if [ "$?" != "0" ]
    then
        echo "stdout differ: \"$excmd\" and \"$mycmd\""
        diff -u tc.out.expected tc.out.real
        echo "----"
        shunit_test_failed
    fi
    return 0
}

assert_stdout() {
    shunit_begin_test
    expected=$1; shift
    echo "$expected" > tc.out.expected
    "$@" >tc.out.real 2>/dev/null
    assert_not_coredump "$1" || return
    cmp tc.out.expected tc.out.real >/dev/null 2>&1
    if [ "$?" != "0" ]
    then
        echo "stdout differ: string \"$expected\" and cmd \"$@\""
        diff -u tc.out.expected tc.out.real
        echo "----"
        shunit_test_failed
    fi
    return 0
}

rm -f core
assert_not_coredump() {
    local cmd="$1"

    shunit_begin_test
    if [ -f core ]
    then
        echo "core dumped: $cmd"
        echo "----"
        shunit_test_failed
        rm -f core
        return 1
    fi
    return 0
}

assert_file() {
    local path="$1"; shift

    shunit_begin_test
    if [ ! -f "$path" ]
    then
        echo "not exist or not a file: $file"
        echo "----"
        shunit_test_failed
        return 1
    fi
    return 0
}

assert_not_exist() {
    local file="$1"; shift

    shunit_begin_test
    if [ -f "$file" ]
    then
        echo "exists: $file"
        echo "----"
        shunit_test_failed
        return 1
    fi
    return 0
}

assert_directory() {
    local dir="$1"; shift

    shunit_begin_test
    if [ ! -d "$dir" ]
    then
        echo "not directory: $dir"
        echo "----"
        shunit_test_failed
        return 1
    fi
    return 0
}
