// indirect recursion
struct a { struct b x; };
typedef struct a struct_a;
struct b { struct c x; };
struct c { struct_a x; };

int main(int argc, char **argv) { return 0; }
