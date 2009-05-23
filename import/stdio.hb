// #@@range/head{
// stdio.hb

import stddef;  // for NULL and size_t
import stdarg;

typedef unsigned long FILE;   // dummy

extern FILE* stdin;
extern FILE* stdout;
extern FILE* stderr;

extern FILE* fopen(char* path, char* mode);
extern FILE* fdopen(int fd, char* mode);
extern FILE* freopen(char* path, char* mode, FILE* stream);
extern int fclose(FILE* stream);
// #@@}
extern int getchar(void);
extern int getc(FILE* stream);
extern int fgetc(FILE* stream);
extern int ungetc(int c, FILE* stream);
extern char* gets(char* buf);
extern char* fgets(char* buf, int size, FILE* stream);
extern int putc(int c);
extern int putchar(int c);
extern int fputc(int c, FILE* stream);
extern int puts(char *str);
extern int fputs(char* str, FILE* stream);
extern int printf(char *fmt, ...);
extern int fprintf(FILE* stream, char* fmt, ...);
extern int sprintf(char* buf, char* fmt, ...);
extern int snprintf(char* buf, size_t size, char* fmt, ...);
extern size_t fread(void* buf, size_t size, size_t nmemb, FILE* stream);
extern size_t fwrite(void* buf, size_t size, size_t nmemb, FILE* stream);
extern int feof(FILE* stream);
extern int ferror(FILE* stream);
extern int fileno(FILE* stream);
extern void clearerr(FILE* stream);
extern void perror(char* param);
extern int vprintf(char* fmt, va_list ap);
extern int vfprintf(FILE* s, char* fmt, va_list ap);
extern int vsprintf(char *buf, char* fmt, va_list ap);
extern int vsnprintf(char *buf, size_t size, char* fmt, va_list ap);
