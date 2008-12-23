import stdio;
import setjmp;

int
main(void)
{
    func1();
    return 0;
}

static jmp_buf buf;

void
func1(void)
{
    if (setjmp(buf) == 0) {
        func2(buf);
    }
    puts("OK");
}

void
func2(jmp_buf buf)
{
    func3(buf);
    puts("func2: NG");
}

void
func3(jmp_buf buf)
{
    longjmp(buf, 1);
    puts("func3: NG");
}
