package net.loveruby.cflat.asm;

public class IntegerLiteral extends Literal {
    protected long value;

    public IntegerLiteral(long n) {
        this.value = n;
    }

    public long value() {
        return this.value;
    }

    public String toString() {
        return new Long(value).toString();
    }
}
