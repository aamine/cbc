package net.loveruby.cflat.asm;

public class AsmComment extends Assembly {
    protected String string;

    public AsmComment(String string) {
        this.string = string;
    }

    public String toSource() {
        return "\t# " + string;
    }
}
