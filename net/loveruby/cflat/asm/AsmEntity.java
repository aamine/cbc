package net.loveruby.cflat.asm;

abstract public class AsmEntity {
    abstract public String toString();

    public boolean isAddress() {
        return false;
    }
}
