package net.loveruby.cflat.asm;

public class IntegerLiteral implements Literal {
    protected long value;

    public IntegerLiteral(long n) {
        this.value = n;
    }

    public boolean equals(Object other) {
        return (other instanceof IntegerLiteral)
                && equals((IntegerLiteral)other);
    }

    public boolean equals(IntegerLiteral other) {
        return other.value == this.value;
    }

    public long value() {
        return this.value;
    }

    public boolean isZero() {
        return value == 0;
    }

    public IntegerLiteral plus(long diff) {
        return new IntegerLiteral(value + diff);
    }

    public IntegerLiteral integerLiteral() {
        return this;
    }

    public String toSource() {
        return new Long(value).toString();
    }

    public String toSource(SymbolTable table) {
        return toSource();
    }

    public void collectStatistics(AsmStatistics stats) {
        // does nothing
    }

    public String toString() {
        return "$" + value;
    }
}
