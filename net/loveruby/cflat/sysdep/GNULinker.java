package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.utils.CommandUtils;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.IPCException;
import java.util.List;
import java.util.ArrayList;

class GNULinker implements Linker {
    static final protected String DYNAMIC_LINKER      = "/lib/ld-linux.so.2";
    static final protected String C_RUNTIME_INIT      = "/usr/lib/crti.o";
    static final protected String C_RUNTIME_START     = "/usr/lib/crt1.o";
    static final protected String C_RUNTIME_START_PIE = "/usr/lib/Scrt1.o";
    static final protected String C_RUNTIME_FINI      = "/usr/lib/crtn.o";

    ErrorHandler errorHandler;

    GNULinker(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void generateExecutable(String destPath,
            List<String> args, LinkerOptions opts) throws IPCException {
        List<String> cmd = new ArrayList<String>();
        cmd.add("ld");
        cmd.add("-dynamic-linker");
        cmd.add(DYNAMIC_LINKER);
        if (opts.generatingPIE) {
            cmd.add("-pie");
        }
        if (! opts.noStartFiles) {
            cmd.add(opts.generatingPIE
                        ? C_RUNTIME_START_PIE
                        : C_RUNTIME_START);
            cmd.add(C_RUNTIME_INIT);
        }
        cmd.addAll(args);
        if (! opts.noDefaultLibs) {
            cmd.add("-lc");
            cmd.add("-lcbc");
        }
        if (! opts.noStartFiles) {
            cmd.add(C_RUNTIME_FINI);
        }
        cmd.add("-o");
        cmd.add(destPath);
        CommandUtils.invoke(cmd, errorHandler, opts.verbose);
    }

    public void generateSharedLibrary(String destPath,
            List<String> args, LinkerOptions opts) throws IPCException {
        List<String> cmd = new ArrayList<String>();
        cmd.add("ld");
        cmd.add("-shared");
        if (! opts.noStartFiles) {
            cmd.add(C_RUNTIME_INIT);
        }
        cmd.addAll(args);
        if (! opts.noDefaultLibs) {
            cmd.add("-lc");
            cmd.add("-lcbc");
        }
        if (! opts.noStartFiles) {
            cmd.add(C_RUNTIME_FINI);
        }
        cmd.add("-o");
        cmd.add(destPath);
        CommandUtils.invoke(cmd, errorHandler, opts.verbose);
    }
}
