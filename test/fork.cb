import stdio;
import unistd;

int
main(int argc, char **argv)
{
    if (fork()) {
        printf("parent: pid=%d ppid=%d\n", getpid(), getppid());
    }
    else {
        printf("child : pid=%d ppid=%d\n", getpid(), getppid());
    }
    return 0;
}
