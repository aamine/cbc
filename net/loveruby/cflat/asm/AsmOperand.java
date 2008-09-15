package net.loveruby.cflat.asm;

abstract public class AsmOperand {
    abstract public String toString();

    public boolean isMemoryReference() {
        return false;
    }
}
