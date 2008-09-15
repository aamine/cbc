package net.loveruby.cflat.asm;

public class Label extends Assembly {
    protected long seq;
    protected String name;

    public Label(String n) {
        seq = -1;
        name = n;
    }

    public Label(long s, String n) {
        seq = s;
        name = n;
    }

    public long seq() {
        return seq;
    }

    public String name() {
        return name;
    }

    public String toSource() {
        return name + ":";
    }

    public String toString() {
        return name;
    }
}
