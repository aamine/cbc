package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.exception.IPCException;

public interface Linker {
    void generateExecutable(String destPath, LinkerOptions opts) throws IPCException;
    void generateSharedLibrary(String destPath, LinkerOptions opts) throws IPCException;
}
