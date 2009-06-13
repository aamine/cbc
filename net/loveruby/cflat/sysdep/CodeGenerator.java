package net.loveruby.cflat.sysdep;

public interface CodeGenerator {
    AssemblyCode generate(net.loveruby.cflat.ir.IR ir);
}
