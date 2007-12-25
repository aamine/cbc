#!/bin/sh

CBC=./cbc

. ./shunit.sh

assert_ok() {
    assert_stdout "OK" $1
    assert_status 0 $1
}

assert_out() {
    assert_stdout "$1" $2
    assert_status 0 $2
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

assert_private() {
    assert_eq "private" `symbol_visibility $1 $2`
}

assert_status 0 ./zero
assert_status 1 ./one

assert_out "Hello, World!" ./hello
assert_out "Hello, World!" ./hello2
assert_out "Hello, World!" ./hello3
assert_out "Hello, World!" ./hello4

assert_status 0 ./funcall0
assert_status 0 ./funcall1
assert_status 0 ./funcall2
assert_status 0 ./funcall3
assert_status 0 ./funcall4
assert_out "1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16;17;18;19;20;21;22;23;24;25;26;27;28;29;30;31;32;33;34;35;36;37;38;39;40;41;42;43;44;45;46;47;48;49;50;51;52;53;54;55" ./funcall5

assert_out "$(/bin/echo -e ';;a;aa;b;";'\'';\a\b\0033\f\n\r\t\v;ABCabc')" ./string

assert_out "1;2" ./param
assert_out "1;2" ./lvar1
assert_out "1;2;3;4;5" ./lvar2
assert_out "1;2" ./comm
assert_out "1;2" ./scomm
assert_private scomm static_common_symbol
assert_out "1;2" ./gvar
assert_out "1;2" ./sgvar
assert_private sgvar static_global_variable
assert_out "1;2" ./slvar
assert_private slvar static_variable
assert_out "1;2" ./slcomm
assert_private slcomm static_variable

assert_out "1;2;3;4;5;6;7;8;9;10;11" ./add
assert_out "1;2;3;4;5;6;7;8;9;10;11;12;13" ./sub
assert_out "1;4;15" ./mul
assert_out "1;2;2;2" ./div
assert_out "0;0;1;4;7" ./mod

assert_out "0;1;2;3;4" ./bitand
assert_out "0;1;2;3;2;6;8;10" ./bitor
assert_out "1;2;0;0;2" ./bitxor
assert_out "-1;-2;0" ./bitnot
assert_out "1;2;4;8;16" ./lshift
assert_out "16;8;4;2;1" ./rshift

assert_out "1;0;0" ./logicalnot
assert_out "-1;0;1" ./unaryminus
assert_out "1;0;-1" ./unaryplus

assert_out "0;1;0" ./eq
assert_out "1;0;1" ./neq
assert_out "1;0;0" ./gt
assert_out "0;0;1" ./lt
assert_out "1;1;0" ./gteq
assert_out "0;1;1" ./lteq

assert_out "2;2" ./assign
assert_out "3;4;3;12;4;1;1;7;5;1;4" ./opassign
assert_out "0;1;2;2;3;3;4" ./inc
assert_out "4;3;2;2;1;1;0" ./dec

assert_ok ./if1
assert_ok ./if2

assert_out "OK;OK;OK;OK" ./condexpr
assert_out "0;0;0;2" ./logicaland
assert_out "0;1;1;1" ./logicalor

assert_ok ./while1
assert_ok ./while2
assert_out "3;3;2;1;0" ./while3
assert_ok ./while-break
assert_ok ./while-continue

assert_ok ./dowhile1
assert_ok ./dowhile2
assert_out "3;3;2;1;0" ./dowhile3
assert_ok ./dowhile-break
assert_ok ./dowhile-continue

assert_out "3;3;2;1;0" ./for1
assert_ok ./for-break
assert_ok ./for-continue

assert_error $CBC break-semcheck.cb
assert_error $CBC continue-semcheck.cb

assert_out "1;5;9" ./array
assert_out "11;22" ./struct
assert_status 0 ./struct-semcheck
assert_error $CBC struct-semcheck2.cb
assert_error $CBC struct-semcheck3.cb
assert_error $CBC struct-semcheck4.cb
assert_out "1;2;513" ./union   # little endian
assert_status 0 ./union-semcheck
assert_error $CBC union-semcheck2.cb
assert_error $CBC union-semcheck3.cb
assert_error $CBC union-semcheck4.cb
assert_out "5;5" ./pointer
assert_out "1;2" ./ptrmemb
assert_out "OK" ./funcptr

assert_out "2;64;-128;0" ./charops
assert_out "2;64;128;0" ./ucharops
assert_out "2;16384;-32768;0" ./shortops
assert_out "2;16384;32768;0" ./ushortops
assert_out "2;1073741824;-2147483648;0" ./intops
assert_out "2;1073741824;2147483648;0" ./uintops
assert_out "1;2;1073741824;-2147483648;0" ./longops  # 32bit
assert_out "1;2;1073741824;2147483648;0" ./ulongops  # 32bit

assert_out "2" ./block
assert_out "25000000" ./cast
assert_out "1;2;3" ./defvar

assert_error $CBC defun-semcheck.cb
assert_error $CBC funcall-semcheck.cb

assert_out "3" ./assoc

test_finished
