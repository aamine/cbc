package net.loveruby.cflat.compiler;

// package private
class LdOption implements LdArg {
    protected String option;

    public LdOption(String option) {
        this.option = option;
    }

    public boolean isSourceFile() {
        return false;
    }

    public String toString() {
        return this.option;
    }
}
