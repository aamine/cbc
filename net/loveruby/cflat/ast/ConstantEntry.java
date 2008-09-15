package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.AsmOperand;
import net.loveruby.cflat.asm.Label;

public class ConstantEntry
{
    protected long id;
    protected String value;
    protected Label label;

    public ConstantEntry(long i, String val) {
        id = i;
        value = val;
        label = new Label(".LC" + id);
    }

    public long id() {
        return id;
    }

    public String value() {
        return value;
    }

    public Label label() {
        return label;
    }
}
