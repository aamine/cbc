package net.loveruby.cflat.platform;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.asm.Type;
import net.loveruby.cflat.utils.AsmUtils;

public class X86Linux implements Platform {
    static final Type naturalType = Type.INT32;
    static final long alignment = 4;
    // #@@range/stackParams{
    static final long stackWordSize = 4;
    // #@@}

    public TypeTable typeTable() {
        return TypeTable.ilp32();
    }

    public Type naturalType() {
        return naturalType;
    }

    public long align(long size) {
        return AsmUtils.align(size, alignment);
    }

    public long stackWordSize() {
        return stackWordSize;
    }

    public long alignStack(long size) {
        return AsmUtils.align(size, stackWordSize);
    }

    public long stackSizeFromWordNum(long num) {
        return num * stackWordSize;
    }
}
