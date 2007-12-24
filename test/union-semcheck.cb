union a {
};

union b {
    union a x;
};

union c {
    union a x;
    union b y;
};

union d {
    union c x;
    union c y;
};

// direct recursion with pointer
union e {
    union e *ptr;
};

// indirect recursion with pointer
union f {
    union g x;
};

union g {
    union f * ptr;
};

int main(int argc, char **argv) { return 0; }
