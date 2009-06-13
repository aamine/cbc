package net.loveruby.cflat.sysdep;
import java.io.PrintStream;

public interface AssemblyCode {
    String toSource();
    void dump();
    void dump(PrintStream s);
}
