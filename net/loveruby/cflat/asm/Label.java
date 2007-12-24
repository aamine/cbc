package net.loveruby.cflat.asm;

public class Label extends AsmEntity {
    long seq;
    String name;

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

    public String toString() {
        return name;
    }

    public AsmEntity add(long offset) {
        throw new Error("Label#add");  // FIXME
    }
}
