package net.loveruby.cflat.compiler;

class LdOption implements LdArg {
    private final String arg;

    LdOption(String arg) {
        this.arg = arg;
    }

    public boolean isSourceFile() {
        return false;
    }

    public String toString() {
        return arg;
    }
}
