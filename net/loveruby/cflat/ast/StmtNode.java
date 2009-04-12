package net.loveruby.cflat.ast;

abstract public class StmtNode extends Node {
    protected Location location;

    public StmtNode(Location loc) {
        this.location = loc;
    }

    public Location location() {
        return location;
    }

    abstract public <S,E> S accept(ASTVisitor<S,E> visitor);
}
