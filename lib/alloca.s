        .text
.globl alloca
        .type   alloca,@function
alloca:
        popl    %ecx
        movl    (%esp), %eax
        addl    $3, %eax
        andl    $-4, %eax
        subl    %eax, %esp
        leal    4(%esp), %eax
        jmp     *%ecx
        .size   alloca, .-alloca
