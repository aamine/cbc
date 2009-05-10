package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.utils.CommandUtils;
import net.loveruby.cflat.utils.CommandArg;
import net.loveruby.cflat.utils.CommandArgStr;
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

    public void generateExecutable(
            String destPath, LinkerOptions opts) throws IPCException {
        List<CommandArg> cmd = new ArrayList<CommandArg>();
        cmd.add(arg("ld"));
        cmd.add(arg("-dynamic-linker"));
        cmd.add(arg(DYNAMIC_LINKER));
        if (opts.generatingPIE) {
            cmd.add(arg("-pie"));
        }
        if (! opts.noStartFiles) {
            cmd.add(arg(opts.generatingPIE
                        ? C_RUNTIME_START_PIE
                        : C_RUNTIME_START));
            cmd.add(arg(C_RUNTIME_INIT));
        }
        cmd.addAll(opts.args);
        if (! opts.noDefaultLibs) {
            cmd.add(arg("-lc"));
            cmd.add(arg("-lcbc"));
        }
        if (! opts.noStartFiles) {
            cmd.add(arg(C_RUNTIME_FINI));
        }
        cmd.add(arg("-o"));
        cmd.add(arg(destPath));
        CommandUtils.invoke(cmd, errorHandler, opts.verbose);
    }

    public void generateSharedLibrary(
            String destPath, LinkerOptions opts) throws IPCException {
        List<CommandArg> cmd = new ArrayList<CommandArg>();
        cmd.add(arg("ld"));
        cmd.add(arg("-shared"));
        if (! opts.noStartFiles) {
            cmd.add(arg(C_RUNTIME_INIT));
        }
        cmd.addAll(opts.args);
        if (! opts.noDefaultLibs) {
            cmd.add(arg("-lc"));
            cmd.add(arg("-lcbc"));
        }
        if (! opts.noStartFiles) {
            cmd.add(arg(C_RUNTIME_FINI));
        }
        cmd.add(arg("-o"));
        cmd.add(arg(destPath));
        CommandUtils.invoke(cmd, errorHandler, opts.verbose);
    }

    private CommandArg arg(String a) {
        return new CommandArgStr(a);
    }
}
