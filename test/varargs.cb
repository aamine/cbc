import stdio;
import stdarg;

int
main(int argc, char** argv)
{
    myprintf("%d;%d;%d\n", 1, 2, 3);
}

static void
myprintf(char* fmt, ...)
{
    va_list ap = va_init(&fmt);
    vfprintf(stdout, fmt, ap);
}
