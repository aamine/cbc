package net.loveruby.cflat.ir;
import net.loveruby.cflat.asm.Label;

public class Case implements Dumpable {
    public long value;
    public Label label;

    public Case(long value, Label label) {
        this.value = value;
        this.label = label;
    }

    public void dump(Dumper d) {
        d.printClass(this);
        d.printMember("value", value);
        d.printMember("label", label);
    }
}
