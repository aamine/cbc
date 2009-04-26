package net.loveruby.cflat.asm;

public enum Type {
    S_INT8,
    U_INT8,
    S_INT16,
    U_INT16,
    S_INT32,
    U_INT32,
    S_INT64,
    U_INT64;

    static public Type get(boolean isSigned, long size) {
        if (isSigned) {
            switch ((int)size) {
            case 1:
                return S_INT8;
            case 2:
                return S_INT16;
            case 4:
                return S_INT32;
            case 8:
                return S_INT64;
            default:
                throw new Error("unsupported asm type size: " + size);
            }
        }
        else {
            switch ((int)size) {
            case 1:
                return U_INT8;
            case 2:
                return U_INT16;
            case 4:
                return U_INT32;
            case 8:
                return U_INT64;
            default:
                throw new Error("unsupported asm type size: " + size);
            }
        }
    }

    public boolean isSigned() {
        switch (this) {
        case S_INT8:
        case S_INT16:
        case S_INT32:
        case S_INT64:
            return true;
        default:
            return false;
        }
    }

    public int size() {
        switch (this) {
        case S_INT8:
        case U_INT8:
            return 1;
        case S_INT16:
        case U_INT16:
            return 2;
        case S_INT32:
        case U_INT32:
            return 4;
        case S_INT64:
        case U_INT64:
            return 8;
        default:
            throw new Error("must not happen");
        }
    }
}
