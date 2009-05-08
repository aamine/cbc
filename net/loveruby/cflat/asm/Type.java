package net.loveruby.cflat.asm;

public enum Type {
    INT8, INT16, INT32, INT64;

    static public Type get(long size) {
        switch ((int)size) {
        case 1:
            return INT8;
        case 2:
            return INT16;
        case 4:
            return INT32;
        case 8:
            return INT64;
        default:
            throw new Error("unsupported asm type size: " + size);
        }
    }

    public int size() {
        switch (this) {
        case INT8:
            return 1;
        case INT16:
            return 2;
        case INT32:
            return 4;
        case INT64:
            return 8;
        default:
            throw new Error("must not happen");
        }
    }
}
