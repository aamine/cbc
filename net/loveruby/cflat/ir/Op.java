package net.loveruby.cflat.ir;

public enum Op {
    ADD,
    SUB,
    MUL,
    DIV,
    MOD,
    BIT_AND,
    BIT_OR,
    BIT_XOR,
    LSHIFT,
    RSHIFT,

    EQ,
    NEQ,
    GT,
    GTEQ,
    LT,
    LTEQ,

    UMINUS,
    BIT_NOT,
    NOT,

    CAST;

    static public Op internBinary(String op) {
        if (op.equals("+")) {
            return Op.ADD;
        }
        else if (op.equals("-")) {
            return Op.SUB;
        }
        else if (op.equals("*")) {
            return Op.MUL;
        }
        else if (op.equals("/")) {
            return Op.DIV;
        }
        else if (op.equals("%")) {
            return Op.MOD;
        }
        else if (op.equals("&")) {
            return Op.BIT_AND;
        }
        else if (op.equals("|")) {
            return Op.BIT_OR;
        }
        else if (op.equals("^")) {
            return Op.BIT_XOR;
        }
        else if (op.equals("<<")) {
            return Op.LSHIFT;
        }
        else if (op.equals(">>")) {
            return Op.RSHIFT;
        }
        else if (op.equals("==")) {
            return Op.EQ;
        }
        else if (op.equals("!=")) {
            return Op.NEQ;
        }
        else if (op.equals("<")) {
            return Op.LT;
        }
        else if (op.equals("<=")) {
            return Op.LTEQ;
        }
        else if (op.equals(">")) {
            return Op.GT;
        }
        else if (op.equals(">=")) {
            return Op.GTEQ;
        }
        else {
            throw new Error("unknown binary op: " + op);
        }
    }

    static public Op internUnary(String op) {
        if (op.equals("+")) {
            throw new Error("unary+ should not be in IR");
        }
        else if (op.equals("-")) {
            return Op.UMINUS;
        }
        else if (op.equals("~")) {
            return Op.BIT_NOT;
        }
        else if (op.equals("!")) {
            return Op.NOT;
        }
        else {
            throw new Error("unknown unary op: " + op);
        }
    }
}
