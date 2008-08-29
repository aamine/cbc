void printf(char *fmt, ...) { return; }
void sprintf(char *buf, char *fmt, ...) { return; }
void snprintf(char *buf, long size, char *fmt, ...) {}

struct file {
    int fd;
    char *path;
};

int
main(int argc, char **argv)
{
    int i = 3;
    int j = 5 * 5;
    int *p = &i;

    g(0);
    g(+3);
    g(-3);
    g(+3U);
    g(-3U);
    g(+3UL);
    g(-3UL);

    g(i + j);
    g(i - j);
    g(i * j);
    g(i / j);
    g(i % j);
    g(i >> j);
    g(i << j);
    g(i & j);
    g(i | j);
    g(i ^ j);

    i += 1;
    i -= 1;
    i *= 1;
    i /= 1;
    i %= 1;
    i &= 1;
    i |= 1;
    i ^= 1;
    i <<= 1;
    i >>= 1;

    g(+i);
    g(-i);

    g(*p);
    g(**&p);

    if (0) {}
    if (0) {} else {}
    if (0) { g(0); }
    if (0) { g(0); } else { g(0); }

    if (1 && 0) {}
    if (0 || 0) {}
    if (1 == 0) {}
    if (0 != 0) {}
    if (1 < 0) {}
    if (1 <= 0) {}
    if (0 > 1) {}
    if (0 >= 1) {}

    while (0) {}
    while (0) { g(0); }
    while (0) { continue; }
    while (1) { break; }

    for (i = 0; i < 0; i++) {}
    for (i = 0; i < 0; i++) { g(0); }
    for (i = 0; i < 0; i++) { continue; }
    for (i = 0; i < 0; i++) { break; }

    {
         struct file f;
         struct file *fp;

         f.fd = 5;
         g(f.fd);
         fp = &f;
         fp->fd = 4;
         g(fp->fd);
    }

    return 0;
}

void g(int i) {}
