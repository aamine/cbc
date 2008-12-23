// setjmp.hb

// sizeof(jmp_buf)==156 on Linux/i386/glibc2.3.
typedef char[156] jmp_buf;
typedef char[156] sigjmp_buf;

extern int setjmp(jmp_buf buf);
extern int sigsetjmp(sigjmp_buf buf, int savesigs);
extern void longjmp(jmp_buf buf, int value);
extern void siglongjmp(sigjmp_buf buf, int value);
