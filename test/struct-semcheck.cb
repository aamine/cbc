struct a {
};

struct b {
    struct a x;
};

struct c {
    struct a x;
    struct b y;
};

struct d {
    struct c x;
    struct c y;
};

// direct recursion with pointer
struct e {
    struct e *ptr;
};

// indirect recursion with pointer
struct f {
    struct g x;
};

struct g {
    struct f * ptr;
};

int main(int argc, char **argv) { return 0; }
