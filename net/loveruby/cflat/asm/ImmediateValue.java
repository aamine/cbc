package net.loveruby.cflat.asm;

public class ImmediateValue extends AsmEntity {
    long value;

    public ImmediateValue(long n) {
        value = n;
    }

    public long value() {
        return value;
    }

    public String toString() {
        return "$" + value;
    }

    public AsmEntity add(long n) {
        return new ImmediateValue(value + n);
    }
}
