package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Slot;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

abstract public class ComplexType extends Type {
    protected String name;
    protected List members;     // List<Slot>
    protected long size;
    protected boolean isRecursiveChecked;

    public ComplexType(String n, List membs) {
        name = n;
        members = membs;
        size = Type.sizeUnknown;
        isRecursiveChecked = false;
    }

    public boolean isComplexType() {
        return true;
    }

    public String name() {
        return name;
    }

    public long size() {
        if (size == Type.sizeUnknown) {
            computeOffsets();
        }
        return size;
    }

    public Iterator members() {
        return members.iterator();
    }

    public boolean hasMember(String name) {
        return (get(name) != null);
    }

    public Type memberType(String name) {
        return fetch(name).type();
    }

    public long memberOffset(String name) {
        Slot s = fetch(name);
        if (s.offset() == Type.sizeUnknown) {
            computeOffsets();
        }
        return s.offset();
    }

    abstract protected void computeOffsets();

    protected long align(long n) {
        // platform dependent
        return n;
    }

    protected Slot fetch(String name) {
        Slot s = get(name);
        if (s == null) {
            throw new SemanticError("no such member in "
                                    + textize() + ": " + name);
        }
        return s;
    }

    public Slot get(String name) {
        Iterator membs = members.iterator();
        while (membs.hasNext()) {
            Slot s = (Slot)membs.next();
            if (s.name().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public boolean isRecursiveChecked() {
        return isRecursiveChecked;
    }

    public void recursiveChecked() {
        isRecursiveChecked = true;
    }
}
