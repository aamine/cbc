package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.exception.IPCException;
import java.util.List;

public interface Linker {
    void generateExecutable(String destPath,
            List<String> args, LinkerOptions opts) throws IPCException;
    void generateSharedLibrary(String destPath,
            List<String> args, LinkerOptions opts) throws IPCException;
}
