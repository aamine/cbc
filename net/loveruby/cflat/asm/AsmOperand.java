package net.loveruby.cflat.asm;

abstract public class AsmOperand {
    abstract public String toSource();

    public boolean isMemoryReference() {
        return false;
    }
}
