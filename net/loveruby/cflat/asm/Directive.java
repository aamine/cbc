package net.loveruby.cflat.asm;

public class Directive extends Assembly {
    protected String content;

    public Directive(String content) {
        this.content = content;
    }

    public String toSource() {
        return this.content;
    }
}
