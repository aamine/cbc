// stdlib.hb

import stddef;   // for size_t

extern void exit(int status);
extern void* calloc(size_t nmemb, size_t size);
extern void* malloc(size_t size);
extern void free(void* ptr);
extern void* realloc(void* ptr, size_t size);
extern int system(char* command);
