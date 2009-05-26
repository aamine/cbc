package net.loveruby.cflat.sysdep;
import java.io.PrintStream;

public interface AssemblyFile {
    String toSource();
    void dump();
    void dump(PrintStream s);
}
