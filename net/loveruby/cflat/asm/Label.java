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

    public boolean isLabel() {
        return true;
    }

    public long seq() {
        return seq;
    }

    public String name() {
        return name;
    }

    public boolean equals(Object other) {
        if (other instanceof Label) {
            return equals((Label)other);
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Label label) {
        return name.equals(label.name());
    }

    public String toSource() {
        return name + ":";
    }

    public String toString() {
        return name;
    }
}
