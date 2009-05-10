package net.loveruby.cflat.sysdep.x86;
import net.loveruby.cflat.sysdep.Platform;
import net.loveruby.cflat.sysdep.CodeGeneratorOptions;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.type.TypeTable;

public class X86Linux implements Platform {
    public TypeTable typeTable() {
        return TypeTable.ilp32();
    }

    public CodeGenerator codeGenerator(
            CodeGeneratorOptions opts, ErrorHandler h) {
        return new CodeGenerator(opts, h);
    }
}
