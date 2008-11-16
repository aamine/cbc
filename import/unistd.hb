// unistd.hb

import sys.types;

extern void _exit(int status);
extern pid_t fork(void);
extern pid_t getpid(void);
extern pid_t getppid(void);
extern unsigned int sleep(unsigned int secs);
