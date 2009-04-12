package net.loveruby.cflat.ir;

public enum StmtKind {
    MOVE,       // assignment
    EXPR,       // expression statement
    BRANCH,     // conditional jump
    RETURN,     // return
    LABEL;      // label
}
