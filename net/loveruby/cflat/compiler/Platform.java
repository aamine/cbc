package net.loveruby.cflat.compiler;
import net.loveruby.cflat.type.TypeTable;

public interface Platform {
    TypeTable typeTable();
    CodeGenerator codeGenerator(CodeGeneratorOptions opts, ErrorHandler h);
}
