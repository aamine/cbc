package net.loveruby.cflat.sysdep;
import java.util.List;
import java.util.ArrayList;

public class AssemblerOptions {
    public boolean verbose = false;
    List<String> args = new ArrayList<String>();

    public void addArg(String a) {
        args.add(a);
    }
}
