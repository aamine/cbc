package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class IntegerTypeRef extends TypeRef {
    static public IntegerTypeRef charRef(Location loc) {
        return new IntegerTypeRef("char", loc);
    }

    static public IntegerTypeRef charRef() {
        return new IntegerTypeRef("char");
    }

    static public IntegerTypeRef shortRef(Location loc) {
        return new IntegerTypeRef("short", loc);
    }

    static public IntegerTypeRef shortRef() {
        return new IntegerTypeRef("short");
    }

    static public IntegerTypeRef intRef(Location loc) {
        return new IntegerTypeRef("int", loc);
    }

    static public IntegerTypeRef intRef() {
        return new IntegerTypeRef("int");
    }

    static public IntegerTypeRef longRef(Location loc) {
        return new IntegerTypeRef("long", loc);
    }

    static public IntegerTypeRef longRef() {
        return new IntegerTypeRef("long");
    }

    static public IntegerTypeRef ucharRef(Location loc) {
        return new IntegerTypeRef("unsigned char", loc);
    }

    static public IntegerTypeRef ucharRef() {
        return new IntegerTypeRef("unsigned char");
    }

    static public IntegerTypeRef ushortRef(Location loc) {
        return new IntegerTypeRef("unsigned short", loc);
    }

    static public IntegerTypeRef ushortRef() {
        return new IntegerTypeRef("unsigned short");
    }

    static public IntegerTypeRef uintRef(Location loc) {
        return new IntegerTypeRef("unsigned int", loc);
    }

    static public IntegerTypeRef uintRef() {
        return new IntegerTypeRef("unsigned int");
    }

    static public IntegerTypeRef ulongRef(Location loc) {
        return new IntegerTypeRef("unsigned long", loc);
    }

    static public IntegerTypeRef ulongRef() {
        return new IntegerTypeRef("unsigned long");
    }

    protected String name;

    public IntegerTypeRef(String name) {
        this(name, null);
    }

    public IntegerTypeRef(String name, Location loc) {
        super(loc);
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean equals(Object other) {
        if (! (other instanceof IntegerTypeRef)) return false;
        IntegerTypeRef ref = (IntegerTypeRef)other;
        return name.equals(ref.name);
    }

    public String toString() {
        return name;
    }
}
