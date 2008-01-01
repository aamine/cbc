union a {
    int x;
};

int main(int argc, char **argv)
{
    union a u;
    union a* ptr = &u;
    ptr->x = 1;
    return ptr->nosuchmember;
}
