package net.loveruby.cflat.asm;

public class AsmComment extends Assembly {
    protected String string;
    protected int indentLevel;

    public AsmComment(String string) {
        this(string, 0);
    }

    public AsmComment(String string, int indentLevel) {
        this.string = string;
        this.indentLevel = indentLevel;
    }

    public boolean isComment() {
        return true;
    }

    public String toSource(SymbolTable table) {
        return "\t" + indent() + "# " + string;
    }

    protected String indent() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < indentLevel; i++) {
            buf.append("  ");
        }
        return buf.toString();
    }
}
