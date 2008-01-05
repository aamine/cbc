package net.loveruby.cflat.ast;

abstract public class Declaration extends Node {
    protected String name;

    public Declaration(String n) {
        name = n;
    }

    public String name() {
        return name;
    }
}
