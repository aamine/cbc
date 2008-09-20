package net.loveruby.cflat.asm;

public class DirectMemoryReference extends MemoryReference {
    protected Literal value;

    public DirectMemoryReference(Label label) {
        this.value = new LabelRef(label);
    }

    public DirectMemoryReference(IntegerLiteral n) {
        this.value = n;
    }

    public Literal value() {
        return this.value;
    }

    public void collectStatistics(AsmStatistics stats) {
        // does nothing
    }

    public String toSource() {
        return this.value.toSource();
    }
}
