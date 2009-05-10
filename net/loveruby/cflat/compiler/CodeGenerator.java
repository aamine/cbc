package net.loveruby.cflat.compiler;

public interface CodeGenerator {
    String generate(net.loveruby.cflat.ir.IR ir);
}
