package net.loveruby.cflat.ast;

abstract public class Definition extends Declaration {
    public Definition(String name) {
        super(name);
    }

    public boolean isEntity() {
        return false;
    }

    public boolean isVariable() {
        return false;
    }

    public boolean isFunction() {
        return false;
    }

    public boolean isType() {
        return false;
    }

    public boolean isComplexType() {
        return false;
    }

    public boolean isStruct() {
        return false;
    }

    public boolean isUnion() {
        return false;
    }

    public boolean isUserType() {
        return false;
    }
}
