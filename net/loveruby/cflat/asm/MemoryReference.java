package net.loveruby.cflat.asm;

abstract public class MemoryReference
        extends AsmOperand implements Comparable<MemoryReference> {
    public boolean isMemoryReference() {
        return true;
    }

    abstract protected int cmp(DirectMemoryReference mem);
    abstract protected int cmp(IndirectMemoryReference mem);
}
