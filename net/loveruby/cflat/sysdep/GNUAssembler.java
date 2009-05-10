package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.utils.CommandUtils;
import net.loveruby.cflat.utils.CommandArg;
import net.loveruby.cflat.utils.CommandArgStr;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.IPCException;
import java.util.List;
import java.util.ArrayList;

class GNUAssembler implements Assembler {
    ErrorHandler errorHandler;

    GNUAssembler(ErrorHandler h) {
        this.errorHandler = h;
    }

    public void assemble(String srcPath, String destPath,
                            AssemblerOptions opts) throws IPCException {
        List<CommandArg> cmd = new ArrayList<CommandArg>();
        cmd.add(arg("as"));
        cmd.addAll(opts.args);
        cmd.add(arg("-o"));
        cmd.add(arg(destPath));
        cmd.add(arg(srcPath));
        CommandUtils.invoke(cmd, errorHandler, opts.verbose);
    }

    private CommandArg arg(String a) {
        return new CommandArgStr(a);
    }
}
