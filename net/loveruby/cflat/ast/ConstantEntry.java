package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.asm.ImmediateValue;
import net.loveruby.cflat.asm.MemoryReference;

public class ConstantEntry {
    protected long id;
    protected String value;
    protected Label label;
    protected MemoryReference memref;
    protected ImmediateValue address;

    public ConstantEntry(long i, String val) {
        id = i;
        value = val;
    }

    public long id() {
        return id;
    }

    public String value() {
        return value;
    }

    public String symbol() {
        return this.label.toString();
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Label label() {
        if (this.label == null) {
            throw new Error("must not happen: label == null");
        }
        return this.label;
    }

    public void setMemref(MemoryReference mem) {
        this.memref = mem;
    }

    public MemoryReference memref() {
        if (this.memref == null) {
            throw new Error("must not happen: memref == null");
        }
        return this.memref;
    }

    public void setAddress(ImmediateValue imm) {
        this.address = imm;
    }

    public ImmediateValue address() {
        return this.address;
    }
}
