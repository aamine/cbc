package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.TypeNode;
import net.loveruby.cflat.ast.TypeDefinition;
import net.loveruby.cflat.ast.Slot;
import java.util.*;

public class TypeTable {
    static public TypeTable ilp32() { return newTable(1, 2, 4, 4, 8, 4); }
    static public TypeTable ilp64() { return newTable(1, 2, 8, 8, 8, 8); }
    static public TypeTable lp64()  { return newTable(1, 2, 4, 8, 8, 8); }
    static public TypeTable llp64() { return newTable(1, 2, 4, 4, 8, 8); }

    static protected final TypeRef voidTypeRef = new VoidTypeRef();
    static protected final TypeRef signedCharRef = new SignedCharRef();
    static protected final TypeRef signedShortRef = new SignedShortRef();
    static protected final TypeRef signedIntRef = new SignedIntRef();
    static protected final TypeRef signedLongRef = new SignedLongRef();
    static protected final TypeRef signedLongLongRef = new SignedLongLongRef();
    static protected final TypeRef unsignedCharRef = new UnsignedCharRef();
    static protected final TypeRef unsignedShortRef = new UnsignedShortRef();
    static protected final TypeRef unsignedIntRef = new UnsignedIntRef();
    static protected final TypeRef unsignedLongRef = new UnsignedLongRef();
    static protected final TypeRef unsignedLongLongRef = new UnsignedLongLongRef();

    static private TypeTable newTable(int charsize, int shortsize,
            int intsize, int longsize, int longlongsize, int ptrsize) {
        TypeTable table = new TypeTable(ptrsize);
        table.put(voidTypeRef, new VoidType());
        table.put(signedCharRef, new SignedCharType(charsize));
        table.put(signedShortRef, new SignedShortType(shortsize));
        table.put(signedIntRef, new SignedIntType(intsize));
        table.put(signedLongRef, new SignedLongType(longsize));
        table.put(signedLongLongRef, new SignedLongType(longlongsize));
        table.put(unsignedCharRef, new UnsignedCharType(charsize));
        table.put(unsignedShortRef, new UnsignedShortType(shortsize));
        table.put(unsignedIntRef, new UnsignedIntType(intsize));
        table.put(unsignedLongRef, new UnsignedLongType(longsize));
        table.put(unsignedLongLongRef, new UnsignedLongLongType(longlongsize));
        return table;
    }

    protected int pointerSize;
    protected Map table;

    public TypeTable(int ptrsize) {
        pointerSize = ptrsize;
        table = new HashMap();
    }

    public void put(TypeRef ref, Type t) {
        table.put(ref, t);
    }

    public Type get(TypeRef ref) {
        Type type = (Type)table.get(ref);
        if (type == null) {
            if (ref instanceof PointerTypeRef) {
                PointerTypeRef pref = (PointerTypeRef)ref;
                Type t = new PointerType(pointerSize, get(pref.base()));
                table.put(pref, t);
                return t;
            }
            else if (ref instanceof ArrayTypeRef) {
                ArrayTypeRef aref = (ArrayTypeRef)ref;
                Type t = new ArrayType(get(aref.base()), aref.length());
                table.put(aref, t);
                return t;
            }
            else if (ref instanceof FunctionTypeRef) {
                FunctionTypeRef fref = (FunctionTypeRef)ref;
                Type t = new FunctionType(get(fref.returnType()),
                                          fref.params().internTypes(this));
                table.put(fref, t);
                return t;
            }
            throw new Error("unregistered type: " + ref.toString());
        }
        return type;
    }

    public Iterator types() {
        return table.values().iterator();
    }

    public SignedCharType signedChar() {
        return (SignedCharType)table.get(signedCharRef);
    }

    public SignedShortType signedShort() {
        return (SignedShortType)table.get(signedShortRef);
    }

    public SignedIntType signedInt() {
        return (SignedIntType)table.get(signedIntRef);
    }

    public SignedLongType signedLong() {
        return (SignedLongType)table.get(signedLongRef);
    }

    public UnsignedCharType unsignedChar() {
        return (UnsignedCharType)table.get(unsignedCharRef);
    }

    public UnsignedShortType unsignedShort() {
        return (UnsignedShortType)table.get(unsignedShortRef);
    }

    public UnsignedIntType unsignedInt() {
        return (UnsignedIntType)table.get(unsignedIntRef);
    }

    public UnsignedLongType unsignedLong() {
        return (UnsignedLongType)table.get(unsignedLongRef);
    }

    public void define(TypeDefinition t) {
        t.defineIn(this);
    }

    public void defineStruct(StructTypeRef ref, List membs) {
        table.put(ref, new StructType(ref.name(), membs));
    }

    public void defineUnion(UnionTypeRef ref, List membs) {
        table.put(ref, new UnionType(ref.name(), membs));
    }

    public void defineUserType(UserTypeRef ref, TypeNode real) {
        table.put(ref, new UserType(ref.name(), real));
    }

    public PointerType pointerTo(Type base) {
        return new PointerType(pointerSize, base);
    }

    public void semanticCheck() {
        Iterator types = table.values().iterator();
        while (types.hasNext()) {
            Type t = (Type)types.next();
            if (t instanceof ComplexType) {
                checkRecursiveDefinition((ComplexType)t);
            }
        }
    }

    protected void checkRecursiveDefinition(ComplexType t) {
        checkRecursiveDefinition(t, new HashMap());
    }

    protected void checkRecursiveDefinition(ComplexType t, Map lock) {
        if (lock.containsKey(t)) { // FIXME: use exception
            throw new Error("recursive type definition: " + t.textize());
        }
        if (t.isRecursiveChecked()) return;
        lock.put(t, t);
        Iterator membs = t.members();
        while (membs.hasNext()) {
            Slot slot = (Slot)membs.next();
            if (slot.type() instanceof ComplexType) {
                checkRecursiveDefinition((ComplexType)slot.type(), lock);
            }
        }
        lock.remove(t);
        t.recursiveChecked();
    }
}
