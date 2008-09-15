package net.loveruby.cflat.asm;

public class DirectMemoryReference extends MemoryReference {
    protected Literal value;

    public DirectMemoryReference(Label label) {
        this.value = new Symbol(label);
    }

    public DirectMemoryReference(IntegerLiteral n) {
        this.value = n;
    }

    public Literal value() {
        return this.value;
    }

    public String toString() {
        return this.value.toString();
    }
}
