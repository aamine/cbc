#include <stdio.h>
#include <setjmp.h>

int
main(int argc, char **argv)
{
    printf("sizeof(jmp_buf)=%lu\n", (unsigned long)sizeof(jmp_buf));
    printf("sizeof(sigjmp_buf)=%lu\n", (unsigned long)sizeof(sigjmp_buf));
    return 0;
}
