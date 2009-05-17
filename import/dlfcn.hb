// dlfcn.hb

// from bits/dlfcn.h
const int RTLD_LAZY     = 0x0001;
const int RTLD_NOW      = 0x0002;
const int RTLD_NOLOAD   = 0x0004;
const int RTLD_DEEPBIND = 0x0008;
const int RTLD_LOCAL    = 0x0000;
const int RTLD_GLOBAL   = 0x0100;
const int RTLD_NODELETE = 0x1000;

// from dlfcn.h
extern void* dlopen(char* filename, int flag);
extern char* dlerror(void);
extern void* dlsym(void* handle, char* symbol);
extern int dlclose(void* handle);
