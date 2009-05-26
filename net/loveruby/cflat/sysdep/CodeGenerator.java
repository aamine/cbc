package net.loveruby.cflat.sysdep;

public interface CodeGenerator {
    AssemblyFile generate(net.loveruby.cflat.ir.IR ir);
}
