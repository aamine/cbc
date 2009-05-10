package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.codegenerator.CodeGenerator;
import net.loveruby.cflat.codegenerator.CodeGeneratorOptions;
import net.loveruby.cflat.utils.ErrorHandler;

public interface Platform {
    TypeTable typeTable();
    CodeGenerator codeGenerator(CodeGeneratorOptions opts, ErrorHandler h);
}
