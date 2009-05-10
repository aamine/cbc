package net.loveruby.cflat.sysdep;
import net.loveruby.cflat.utils.CommandArg;
import net.loveruby.cflat.utils.CommandArgStr;
import java.util.List;
import java.util.ArrayList;

public class LinkerOptions {
    public boolean generatingSharedLibrary = false;
    public boolean generatingPIE = false;
    public boolean noStartFiles = false;
    public boolean noDefaultLibs = false;
    public boolean verbose = false;
    List<CommandArg> args = new ArrayList<CommandArg>();

    public void addArg(String a) {
        args.add(new CommandArgStr(a));
    }

    public void addArg(CommandArg a) {
        args.add(a);
    }

    public List<CommandArg> args() {
        return this.args;
    }
}
