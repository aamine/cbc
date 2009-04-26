package net.loveruby.cflat.ir;
import net.loveruby.cflat.ast.Location;

abstract public class Stmt {
    protected Location location;

    public Stmt(Location loc) {
        this.location = loc;
    }

    abstract public <S,E> S accept(IRVisitor<S,E> visitor);

    public Location location() {
        return location;
    }

    abstract protected void _dump(Dumper d);
}
