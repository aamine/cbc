#
# test_cbc.sh
#

CBC=${CBC:-../bin/cbc}

test_01_exec() {
    assert_stat 0 ./zero
    assert_stat 1 ./one
}

test_02_print() {
    assert_out "Hello, World!" ./hello
    assert_out "Hello, World!" ./hello2
    assert_out "Hello, World!" ./hello3
    assert_out "Hello, World!" ./hello4
}

test_03_integer() {
    assert_out "0;0;0;1;1;1;9;9;9;17;17;17" ./integer
}

test_04_funcall() {
    assert_stat 0 ./funcall0
    assert_stat 0 ./funcall1
    assert_stat 0 ./funcall2
    assert_stat 0 ./funcall3
    assert_stat 0 ./funcall4
    assert_out "1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16;17;18;19;20;21;22;23;24;25;26;27;28;29;30;31;32;33;34;35;36;37;38;39;40;41;42;43;44;45;46;47;48;49;50;51;52;53;54;55" ./funcall5
}

test_05_string() {
    assert_out "$(/bin/echo -e ';;a;aa;b;";'\'';\a\b\0033\f\n\r\t\v;ABCabc')" ./string
    assert_out "$(cat utf.out)" ./utf
}

test_06_variables() {
    assert_out "1;2" ./param
    assert_out "1;2" ./lvar1
    assert_out "1;2;3;4;5" ./lvar2
    assert_out "4;80;0;local" ./initializer
    assert_out "16;16;16;msgstring" ./const
    assert_compile_error var-semcheck.cb

    assert_out "1;2;OK;NEW" ./comm &&
    assert_public comm global_int &&
    assert_public comm global_string
    assert_out "1;2;OK;NEW" ./scomm &&
    assert_private scomm static_common_symbol &&
    assert_private scomm static_common_string

    assert_out "1;2;OK;NEW" ./gvar &&
    assert_public comm global_int &&
    assert_public comm global_string
    assert_out "1;2;OK;NEW" ./sgvar &&
    assert_private sgvar static_global_variable &&
    assert_private sgvar static_global_string

    assert_out "1;2;OK;NEW" ./slvar &&
    assert_private slvar static_variable &&
    assert_private slvar static_string
    assert_out "1;2;OK;NEW" ./slcomm &&
    assert_private slcomm static_variable &&
    assert_private slcomm static_string
}

test_07_arithmetic() {
    assert_out "-1;0;1" ./unaryminus
    assert_out "1;0;-1" ./unaryplus

    assert_out "1;2;3;4;5;6;7;8;9;10;11" ./add
    assert_out "1;2;3;4;5;6;7;8;9;10;11;12;13" ./sub
    assert_out "1;4;15" ./mul
    assert_out "1;2;2;2;4" ./div
    assert_out "0;0;1;4;7" ./mod

    assert_out "3" ./assoc
}

test_08_bitop() {
    assert_out "0;1;2;3;4" ./bitand
    assert_out "0;1;2;3;2;6;8;10" ./bitor
    assert_out "1;2;0;0;2" ./bitxor
    assert_out "-1;-2;0" ./bitnot
    assert_out "1;2;4;8;16" ./lshift
    assert_out "16;8;4;2;1" ./rshift
}

test_09_cmp() {
    assert_out "0;1;0;0;1;0" ./eq
    assert_out "1;0;1;1;0;1" ./neq
    assert_out "1;0;0" ./gt
    assert_out "0;0;1" ./lt
    assert_out "1;1;0" ./gteq
    assert_out "0;1;1" ./lteq
}

test_10_assign() {
    assert_out "1;2;2;3;4;5;6;7;8;8;9;10;11;777;S;12" ./assign
    assert_out "3;4;3;12;4;1;1;7;5;1;4;e;H;76;75;1;3;6;82;81" ./opassign
    assert_out "0;1;2;2;3;3;4;5;5" ./inc
    assert_out "4;3;2;2;1;1;0" ./dec
    assert_compile_error textwrite.cb
}

test_12_if() {
    assert_ok ./if1
    assert_ok ./if2
}

test_13_logical() {
    assert_out "1;0;0;0;1" ./logicalnot
    assert_out "OK;OK;OK;OK;OK" ./condexpr
    assert_out "0;0;0;2;OK" ./logicaland
    assert_out "0;1;1;1;OK" ./logicalor
}

test_14_while() {
    assert_ok ./while1
    assert_ok ./while2
    assert_out "3;3;2;1;0" ./while3
}

test_15_dowhile() {
    assert_ok ./dowhile1
    assert_ok ./dowhile2
    assert_out "3;3;2;1;0" ./dowhile3
}

test_16_for() {
    assert_out "3;3;2;1;0" ./for1
}

test_17_jump() {
    assert_ok ./while-break
    assert_ok ./dowhile-break
    assert_ok ./for-break
    assert_compile_error break-semcheck.cb

    assert_ok ./while-continue
    assert_ok ./dowhile-continue
    assert_ok ./for-continue
    assert_compile_error continue-semcheck.cb
}

test_18_array() {
    assert_out "1;5;9" ./array
    assert_out "0;0;0" ./array2
    assert_out "3;4;5;6;7;8;9;10;11;" ./mdarray
    assert_compile_success mdarray2.cb &&
    if ruby_exists
    then
        local offsets=$(./mdarray2 | ruby -e '
            addrs = $stdin.read.split.map {|n| n.hex }
            puts addrs.map {|a| a - addrs.first }.join(";")
        ')
        assert_eq "0;4;8;12;16;20;24;28;32" "$offsets"
    fi
    assert_out "775;776;777;778;775;776;777;778;775;776;777;778;775;776;777;778;" ./ptrarray
    assert_compile_error aref-semcheck.cb
    assert_compile_error aref-semcheck2.cb
    assert_compile_error array-semcheck1.cb
    assert_compile_error array-semcheck2.cb
}

ruby_exists() {
    ruby -e "" 2>/dev/null
}

test_19_struct() {
    assert_out "11;22" ./struct
    assert_out "701;702;703;704" ./struct2
    assert_out "7" ./struct3
    assert_stat 0 ./struct-semcheck
    assert_compile_success empstruct.cb
    assert_compile_error struct-semcheck2.cb
    assert_compile_error struct-semcheck3.cb
    assert_compile_error struct-semcheck4.cb
    assert_compile_error struct-semcheck5.cb
    assert_compile_error struct-semcheck6.cb
    assert_compile_error struct-semcheck7.cb
    assert_compile_error struct-semcheck8.cb
    assert_compile_error struct-semcheck9.cb
    assert_compile_error struct-semcheck10.cb
}

test_20_union() {
    assert_out "1;2;513" ./union   # little endian
    assert_stat 0 ./union-semcheck
    assert_compile_error union-semcheck2.cb
    assert_compile_error union-semcheck3.cb
    assert_compile_error union-semcheck4.cb
    assert_compile_error union-semcheck5.cb
    assert_compile_error union-semcheck6.cb
    assert_compile_error union-semcheck7.cb
    assert_compile_error union-semcheck8.cb
    assert_compile_error union-semcheck9.cb
    assert_compile_error union-semcheck10.cb
}

test_21_typedef() {
    assert_out "1;2;1;1;3;4;5;6;OK" ./usertype
    assert_compile_error recursivetypedef.cb
}

test_22_pointer() {
    assert_out "5;5" ./pointer
    assert_out "777" ./pointer2
    assert_out "1;777;3;4;1;777;3;4" ./pointer3
    assert_out "777" ./pointer4
    assert_out "1;2;3;4;5;6;77;78" ./ptrmemb
    assert_out "7" ./ptrmemb2
    assert_out "OK;OK;OK;OK" ./addressof
    assert_out "-4;-5;-5;-3" ./ptrdiff
    assert_compile_error deref-semcheck1.cb
    assert_compile_error deref-semcheck2.cb
    assert_compile_error deref-semcheck3.cb
    assert_compile_error deref-semcheck4.cb
    assert_compile_error deref-semcheck5.cb
}

test_23_limits() {
    assert_out "2;64;-128;0" ./charops
    assert_out "-2;-64;-128;0" ./charops2
    assert_out "2;64;128;0" ./ucharops
    assert_out "254;192;128;0" ./ucharops2
    assert_out "2;16384;-32768;0" ./shortops
    assert_out "-2;-16384;-32768;0" ./shortops2
    assert_out "2;16384;32768;0" ./ushortops
    assert_out "65534;49152;32768;0" ./ushortops2
    assert_out "2;1073741824;-2147483648;0" ./intops
    assert_out "2;1073741824;2147483648;0" ./uintops
    assert_out "1;2;1073741824;-2147483648;0" ./longops  # 32bit
    assert_out "1;2;1073741824;2147483648;0" ./ulongops  # 32bit
}

test_24_cast() {
    assert_out "25000000;1;1;-1;-1;1;1;-1;-1" ./cast
    assert_out "777;666" ./cast2
}

test_25_block() {
    assert_out "1;2;3;1;OK" ./block
    assert_out "1;2;3" ./defvar
    assert_out "77" ./decloverride
    assert_compile_error decloverride2.cb
}

test_26_funcptr() {
    assert_out "OK;OK;OK;OK" ./funcptr
    assert_out ";OK;OK;OK;OK;OK" ./implicitaddr
    assert_compile_error defun-semcheck.cb
    assert_compile_error defun-semcheck2.cb
    assert_compile_error defun-semcheck3.cb
    assert_compile_error defun-semcheck4.cb
    assert_compile_error defun-semcheck5.cb
    assert_compile_error defun-semcheck6.cb
    assert_compile_error defun-semcheck7.cb
    assert_compile_error defun-semcheck8.cb
    assert_compile_error funcall-semcheck.cb
    assert_compile_error funcall-semcheck2.cb
}

test_27_switch() {
    if assert_compile_success switch.cb
    then
        assert_stdout "1 or 2" ./switch
        assert_stdout "1 or 2" ./switch x
        assert_stdout "3 or 4" ./switch x x
        assert_stdout "3 or 4" ./switch x x x
        assert_stdout "5 or 6" ./switch x x x x
        assert_stdout "5 or 6" ./switch x x x x x
        assert_stdout "other"  ./switch x x x x x x
        assert_stdout "other"  ./switch x x x x x x x
    fi
}

test_28_syntax() {
    assert_out "1, 2, 0" ./syntax1
    assert_compile_success syntax2.cb
    assert_stat 0 ./syntax3
}

test_29_import() {
    assert_compile_success duplicated-import.cb
    assert_compile_success vardecl.cb &&
    assert_status 0 ./vardecl
    assert_compile_success -fPIC vardecl.cb &&
    assert_status 0 ./vardecl
}

test_30_staticfunction() {
    assert_compile_success staticfunc.cb &&
    assert_private staticfunc private_function
}

test_31_sizeof() {
    assert_out "1;1;2;2;4;4;4;4;4;4;4;16;12;16;12" ./sizeof-type
    assert_out "12;20;1;2;6;3" ./sizeof-struct
    assert_out "1;1;4;8" ./sizeof-union
    assert_out "1;2;4;4;4;8;12;16;12" ./sizeof-expr
}

test_32_noreturn() {
    assert_stat 0 ./noreturn
}

test_33_multipleinput() {
    assert_compile_success src1.cb src2.cb -o src &&
    assert_status 4 ./src
}

test_34_varargs() {
    assert_out "1;2;3" ./varargs
}

test_35_invalidstmt() {
    assert_compile_error invalidstmt1.cb
    assert_compile_error invalidstmt2.cb
    assert_compile_success validstmt1.cb
}

test_36_alloca() {
    assert_out "<<Hello>>" ./alloca
    assert_out "17;17;17;17" ./alloca2
    assert_compile_success -fPIE -pie alloca2.cb &&
    assert_out "17;17;17;17" ./alloca2
}

test_37_setjmp() {
    assert_out "OK" ./setjmptest
}

###
### Local Assertions
###

assert_stat() {
    st=$1; shift
    assert_compile_success "$1.cb" &&
    assert_status $st "$@"
}

assert_out() {
    msg="$1"; shift
    if [ -n "$SHUNIT_FAST" ]
    then
        assert_compile_success "$1.cb" &&
        assert_stdout "$msg" "$@"
    else
        assert_compile_success "$1.cb" &&
        assert_stdout "$msg" "$@" &&
        assert_compile_success -O "$1.cb" &&
        assert_stdout "$msg" "$@" &&
        assert_compile_success -fPIC "$1.cb" &&
        assert_stdout "$msg" "$@" &&
        assert_compile_success -O -fPIC "$1.cb" &&
        assert_stdout "$msg" "$@"
    fi
}

assert_ok() {
    assert_out "OK" "$@"
}

symbol_visibility() {
    bin="$1"
    sym="$2"
    tmp=`readelf -s "$bin" | grep "$sym" | awk '{print $5}'`
    if [ "$tmp" = "LOCAL" ]
    then echo "private"
    else echo "public"
    fi
}

assert_public() {
    assert_eq "public" `symbol_visibility $1 $2`
}

assert_private() {
    assert_eq "private" `symbol_visibility $1 $2`
}

assert_compile_success() {
    assert_status 0 $CBC "$@"
}

assert_compile_error() {
    shunit_begin_test
    if "$CBC" "$@" >tc.out 2>&1
    then
        echo "shunit[$@]: compile error not occured"
        shunit_test_failed
        return 1
    fi
    assert_not_coredump || return
    if egrep -qv 'cbc: (error|warning): ' tc.out
    then
        echo "shunit[$@]: abnormal cbc error; error message is:"
        cat tc.out
        shunit_test_failed
        return 1
    fi
    return 0
}
