package net.loveruby.cflat.ast;

public class Location {
    protected String source;
    protected int line;
    protected int column;

    public Location(String source, int line, int column) {
        this.source = source;
        this.line = line;
        this.column = column;
    }

    public String source() {
        return source;
    }

    public int line() {
        return line;
    }

    public String toString() {
        return source + ":" + line;
    }
}
