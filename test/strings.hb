// strings.hb

import stddef;

extern int strcasecmp(char* str1, char* str2);
extern int strncasecmp(char* str1, char* str2, size_t len);
extern char* index(char* src, int ch);
extern char* rindex(char* src, int ch);
