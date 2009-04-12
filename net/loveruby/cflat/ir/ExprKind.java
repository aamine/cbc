package net.loveruby.cflat.ir;

public enum ExprKind {
    // primary
    CONST,      // integer constant
    NAME,       // assembly name (string literal, global variable)
    VAR,        // variable
    VARADDR,    // address of variable
    // unary
    UMINUS,     // unary minus
    NOT,        // bitwise not
    LNOT,       // logical not
    DEREF,      // dereference
    // binary
    ADD,        // addition
    SUB,        // subtract
    MUL,        // multiplication
    DIV,        // division
    MOD,        // modulo
    AND,        // bitwise and
    OR,         // bitwise or
    XOR,        // bitwise xor
    LSHIFT,     // left shift
    RSHIFT,     // right shift
    EQ,         // ==
    NEQ,        // !=
    GT,         // >
    GTEQ,       // >=
    LT,         // <
    LTEQ,       // <=
    // seq
    SEQ;        // expression sequence (for +=, ++, --)
}
