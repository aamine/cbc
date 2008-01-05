package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.exception.*;
import java.util.*;

abstract public class NamedType extends Type {
    protected String name;
    protected Location location;

    public NamedType(String name, Location loc) {
        this.name = name;
        this.location = loc;
    }

    public String name() {
        return name;
    }

    public Location location() {
        return location;
    }
}
