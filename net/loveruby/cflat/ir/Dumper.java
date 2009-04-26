package net.loveruby.cflat.ir;
import net.loveruby.cflat.ast.DefinedVariable;
import net.loveruby.cflat.ast.DefinedFunction;
import java.util.List;
import java.io.PrintStream;

public class Dumper {
    PrintStream stream;
    private int numIndent;

    Dumper(PrintStream s) {
        stream = s;
        numIndent = 0;
    }

    public void printMember(String name, int memb) {
        // FIXME
    }

    public void printMember(String name, long memb) {
        // FIXME
    }

    public void printMember(String name, boolean memb) {
        // FIXME
    }

    public void printMember(String name, String memb) {
        // FIXME
    }

    public void printMember(String name, Stmt memb) {
        // FIXME
    }

    public void printMember(String name, Expr memb) {
        // FIXME
    }

    public void printStmts(String name, List<Stmt> memb) {
        // FIXME
    }

    public void printVariables(String name, List<DefinedVariable> memb) {
        // FIXME
    }

    public void printFunctions(String name, List<DefinedFunction> memb) {
        // FIXME
    }
}
