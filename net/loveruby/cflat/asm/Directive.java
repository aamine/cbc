package net.loveruby.cflat.asm;
import net.loveruby.cflat.utils.TextUtils;

public class Directive extends Assembly {
    protected String content;

    public Directive(String content) {
        this.content = content;
    }

    public boolean isDirective() {
        return true;
    }

    public String toSource(SymbolTable table) {
        return this.content;
    }

    public String dump() {
        return "(Directive " + TextUtils.dumpString(content.trim()) + ")";
    }
}
