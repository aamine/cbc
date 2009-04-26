package net.loveruby.cflat.ir;
import net.loveruby.cflat.asm.Label;

public class Case {
    public long value;
    public Label label;

    public Case(long value, Label label) {
        this.value = value;
        this.label = label;
    }
}
