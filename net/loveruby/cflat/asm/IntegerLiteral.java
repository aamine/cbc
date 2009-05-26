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

    public void collectStatistics(Statistics stats) {
        // does nothing
    }

    public String toString() {
        return new Long(value).toString();
    }

    public int compareTo(Literal lit) {
        return -(lit.cmp(this));
    }

    public int cmp(IntegerLiteral i) {
        return new Long(value).compareTo(new Long(i.value));
    }

    public int cmp(NamedSymbol sym) {
        return -1;
    }

    public int cmp(UnnamedSymbol sym) {
        return -1;
    }

    public int cmp(SuffixedSymbol sym) {
        return -1;
    }

    public String dump() {
        return "(IntegerLiteral " + new Long(value).toString() + ")";
    }
}
