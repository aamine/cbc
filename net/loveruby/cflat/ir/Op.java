package net.loveruby.cflat.ir;

public enum Op {
    ADD,
    SUB,
    MUL,
    S_DIV,
    U_DIV,
    S_MOD,
    U_MOD,
    BIT_AND,
    BIT_OR,
    BIT_XOR,
    BIT_LSHIFT,
    BIT_RSHIFT,
    ARITH_RSHIFT,

    EQ,
    NEQ,
    S_GT,
    S_GTEQ,
    S_LT,
    S_LTEQ,
    U_GT,
    U_GTEQ,
    U_LT,
    U_LTEQ,

    UMINUS,
    BIT_NOT,
    NOT,

    S_CAST,
    U_CAST;

    static public Op internBinary(String op, boolean isSigned) {
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
            return isSigned ? Op.S_DIV : Op.U_DIV;
        }
        else if (op.equals("%")) {
            return isSigned ? Op.S_MOD : Op.U_MOD;
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
            return Op.BIT_LSHIFT;
        }
        else if (op.equals(">>")) {
            return isSigned ? Op.ARITH_RSHIFT : Op.BIT_RSHIFT;
        }
        else if (op.equals("==")) {
            return Op.EQ;
        }
        else if (op.equals("!=")) {
            return Op.NEQ;
        }
        else if (op.equals("<")) {
            return isSigned ? Op.S_LT : Op.U_LT;
        }
        else if (op.equals("<=")) {
            return isSigned ? Op.S_LTEQ : Op.U_LTEQ;
        }
        else if (op.equals(">")) {
            return isSigned ? Op.S_GT : Op.U_GT;
        }
        else if (op.equals(">=")) {
            return isSigned ? Op.S_GTEQ : Op.U_GTEQ;
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
