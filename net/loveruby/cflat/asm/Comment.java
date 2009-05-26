package net.loveruby.cflat.asm;
import net.loveruby.cflat.utils.TextUtils;

public class Comment extends Assembly {
    protected String string;
    protected int indentLevel;

    public Comment(String string) {
        this(string, 0);
    }

    public Comment(String string, int indentLevel) {
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

    public String dump() {
        return "(Comment " + TextUtils.dumpString(string) + ")";
    }
}
