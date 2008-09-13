// stdarg.hb

typedef unsigned long va_arg_t;
typedef va_arg_t* va_list;

extern va_list va_init(void* arg);
extern void* va_next(va_list* ap);
