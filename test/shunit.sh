#
# $Id: shunit.sh 1096 2006-11-02 12:31:46Z aamine $
#
# Simple Unit Test Framework for Shell Scripts
#

n_tests=0
n_failed=0
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

print() {
    if [ `/bin/echo -n x` = "-n" ]
    then
        /bin/echo $1'\c'
    else
        /bin/echo -n $1
    fi
}

progress() {
    print '.'
}

begin_test() {
    progress
    n_tests=`expr $n_tests "+" 1`
}

assertion_failed() {
    n_failed=`expr $n_failed "+" 1`
}

execute_command() {
    if [ "$SHUNIT_VERBOSE" = true ]
    then
        "$@"
    else
        "$@" >/dev/null 2>&1
    fi
}

assert_status() {
    begin_test
    expected=$1; shift
    execute_command "$@"
    really=$?
    assert_not_coredump || return
    if [ "$really" != "$expected" ]
    then
        echo "shunit[$@]: status $expected expected but was: $really"
        assertion_failed
        return 1
    fi
    return 0
}

assert_error() {
    begin_test
    execute_command "$@"
    really=$?
    assert_not_coredump || return
    if [ "$really" = "0" ]
    then
        echo "shunit[$@]: non-zero status expected but was: $really"
        assertion_failed
        return 1
    fi
    return 0
}

assert_eq() {
    begin_test
    expected="$1"
    really="$2"
    if [ "$really" != "$expected" ]
    then
        echo "shunit: <$expected> expected but is: <$really>"
        assertion_failed
        return 1
    fi
    return 0
}

assert_equal() {
    begin_test
    _f="no"
    excmd=$1
    mycmd=$2
    eval "$excmd" >tc.out.expected 2>tc.err.expected
    eval "$mycmd" >tc.out.real     2>tc.err.real
    assert_not_coredump || return
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
        assertion_failed
        return 1
    fi
    return 0
}

assert_equal_stdout() {
    begin_test
    _f="no"
    excmd=$1; shift
    mycmd=$1; shift
    eval "$excmd" >tc.out.expected 2>/dev/null
    eval "$mycmd" >tc.out.real     2>/dev/null
    assert_not_coredump || return
    cmp tc.out.real tc.out.expected >/dev/null 2>&1
    if [ "$?" != "0" ]
    then
        echo "stdout differ: \"$excmd\" and \"$mycmd\""
        diff -u tc.out.expected tc.out.real
        echo "----"
        assertion_failed
    fi
    return 0
}

assert_stdout() {
    begin_test
    expected=$1; shift
    echo "$expected" > tc.out.expected
    "$@" >tc.out.real 2>/dev/null
    assert_not_coredump || return
    cmp tc.out.expected tc.out.real >/dev/null 2>&1
    if [ "$?" != "0" ]
    then
        echo "stdout differ: string \"$expected\" and cmd \"$@\""
        diff -u tc.out.expected tc.out.real
        echo "----"
        assertion_failed
    fi
    return 0
}

rm -f core
assert_not_coredump() {
    begin_test
    if [ -f core ]
    then
        echo "core dumped: $mycmd"
        echo "----"
        assertion_failed
        return 1
    fi
    rm -f core
    return 0
}

assert_not_exist() {
    begin_test
    file=$1; shift
    if [ -f $file ]
    then
        echo "exists: $file"
        echo "----"
        assertion_failed
        return 1
    fi
    return 0
}

assert_directory() {
    begin_test
    dir=$1; shift
    if [ ! -d $dir ]
    then
        echo "not directory: $dir"
        echo "----"
        assertion_failed
        return 1
    fi
    return 0
}
