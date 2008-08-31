// string.hb

import stddef;  // for size_t

extern char* strcat(char* dest, char* src);
extern char* strncat(char* dest, char* src, size_t len);
extern char* strchr(char* str, int c);
extern char* strrchr(char* str, int c);
extern int strcmp(char* str1, char* str2);
extern int strncmp(char* str1, char* str2, size_t len);
extern char* strcpy(char* dest, char* src);
extern char* strncpy(char* dest, char* src, size_t len);
extern char* strdup(char* str);
extern size_t strlen(char* str);
extern char* strstr(char* str, char* pattern);
extern size_t strspn(char* str, char* accept);
extern size_t strcspn(char* str, char* reject);
extern char* strerror(int errnum);
extern char* strerror_r(int errnum, char* buf, size_t len);
extern void* memcpy(void* dest, void* src, size_t len);
extern void* memccpy(void* dest, void* src, int c, size_t len);
extern void* memmove(void* dest, void* src, size_t len);
