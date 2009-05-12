package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.exception.IPCException;
import java.util.List;

public interface Linker {
    void generateExecutable(List<String> args, String destPath,
            LinkerOptions opts) throws IPCException;
    void generateSharedLibrary(List<String> args, String destPath,
            LinkerOptions opts) throws IPCException;
}
