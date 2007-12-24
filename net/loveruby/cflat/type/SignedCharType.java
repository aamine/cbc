package net.loveruby.cflat.type;

public class SignedCharType extends IntegerType {
    public SignedCharType(int size) {
        super(size, true, "char");
    }
}
