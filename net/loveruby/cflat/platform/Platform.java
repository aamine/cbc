package net.loveruby.cflat.platform;

public interface Platform {
    net.loveruby.cflat.type.TypeTable typeTable();
    net.loveruby.cflat.asm.Type naturalType();
    long align(long size);
    long stackWordSize();
    long alignStack(long size);
    long stackSizeFromWordNum(long numWords);
}
