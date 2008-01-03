// indirect recursion
union a { union b x; };
typedef union a union_a;
union b { union c x; };
union c { union_a x; };

int main(int argc, char **argv) { return 0; }
