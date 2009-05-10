package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.type.TypeTable;

public class X86Linux implements Platform {
    public TypeTable typeTable() {
        return TypeTable.ilp32();
    }

    public CodeGenerator codeGenerator(
            CodeGeneratorOptions opts, ErrorHandler h) {
        return new net.loveruby.cflat.sysdep.x86.CodeGenerator(opts, h);
    }

    public Assembler assembler(ErrorHandler h) {
        return new GNUAssembler(h);
    }

    public Linker linker(ErrorHandler h) {
        return new GNULinker(h);
    }
}
