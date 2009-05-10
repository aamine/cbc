package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.exception.IPCException;

public interface Assembler {
    void assemble(String srcPath, String destPath,
            AssemblerOptions opts) throws IPCException;
}
