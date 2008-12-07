        .text
.globl alloca
        .type   alloca,@function
alloca:
        addl    $4, %esp
        movl    -4(%esp), %ecx
        movl    (%esp), %eax
        addl    $3, %eax
        andl    $-4, %eax
        subl    %eax, %esp
        movl    %esp, %eax
        addl    $4, %eax
        jmp     *%ecx
        .size   alloca, .-alloca
