// setjmp.hb

typedef char[160] jmp_buf;
typedef char[160] sigjmp_buf;

extern int setjmp(jmp_buf buf);
extern int sigsetjmp(sigjmp_buf buf, int savesigs);
extern void longjmp(jmp_buf buf, int value);
extern void siglongjmp(sigjmp_buf buf, int value);
