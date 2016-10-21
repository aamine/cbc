package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.utils.CommandUtils;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.IPCException;
import java.util.List;
import java.util.ArrayList;

class GNULinker implements Linker {
    // 32bit Linux dependent
    // #@@range/vars{
    static final private String LINKER = "/usr/bin/ld";
    static final private String DYNAMIC_LINKER      = "/lib/ld-linux.so.2";
    static final private String C_RUNTIME_INIT      = "/usr/lib32/crti.o";
    static final private String C_RUNTIME_START     = "/usr/lib32/crt1.o";
    static final private String C_RUNTIME_START_PIE = "/usr/lib32/Scrt1.o";
    static final private String C_RUNTIME_FINI      = "/usr/lib32/crtn.o";
    // #@@}

    ErrorHandler errorHandler;

    GNULinker(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    // #@@range/generateExecutable{
    public void generateExecutable(List<String> args,
            String destPath, LinkerOptions opts) throws IPCException {
        List<String> cmd = new ArrayList<String>();
        cmd.add(LINKER);
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
    // #@@}

    // #@@range/generateSharedLibrary{
    public void generateSharedLibrary(List<String> args,
            String destPath, LinkerOptions opts) throws IPCException {
        List<String> cmd = new ArrayList<String>();
        cmd.add(LINKER);
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
    // #@@}
}
