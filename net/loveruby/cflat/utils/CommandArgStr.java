package net.loveruby.cflat.utils;

public class CommandArgStr implements CommandArg {
    private String arg;

    public CommandArgStr(String arg) {
        this.arg = arg;
    }

    public boolean isSourceFile() {
        return false;
    }

    public String toString() {
        return arg;
    }
}
