package net.loveruby.cflat.asm;

public class Reference extends Address {
    protected Label label;

    public Reference(Label label) {
        this.label = label;
    }

    public AsmEntity add(long n) {
        // FIXME??
        // MemberNode#address calls this method.
        // Some combination does not allows this optimization,
        // caller should validate operation before call.
        // Or, throw specific exception and catch it.
        throw new Error("Reference#add");
    }

    public String toString() {
        return "$" + label.toString();
    }
}
