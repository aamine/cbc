package net.loveruby.cflat.type;

public class SignedIntType extends IntegerType {
    public SignedIntType(int size) {
        super(size, true, "int");
    }
}
