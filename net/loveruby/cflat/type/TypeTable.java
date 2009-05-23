package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class TypeTable {
    static public TypeTable ilp32() { return newTable(1, 2, 4, 4, 4); }
    static public TypeTable ilp64() { return newTable(1, 2, 8, 8, 8); }
    static public TypeTable lp64()  { return newTable(1, 2, 4, 8, 8); }
    static public TypeTable llp64() { return newTable(1, 2, 4, 4, 8); }

    static private TypeTable newTable(int charsize, int shortsize,
                                      int intsize, int longsize, int ptrsize) {
        TypeTable table = new TypeTable(intsize, longsize, ptrsize);
        table.put(new VoidTypeRef(), new VoidType());
        table.put(IntegerTypeRef.charRef(),
                  new IntegerType(charsize,  true, "char"));
        table.put(IntegerTypeRef.shortRef(),
                  new IntegerType(shortsize, true, "short"));
        table.put(IntegerTypeRef.intRef(),
                  new IntegerType(intsize, true, "int"));
        table.put(IntegerTypeRef.longRef(),
                  new IntegerType(longsize, true, "long"));
        table.put(IntegerTypeRef.ucharRef(),
                  new IntegerType(charsize, false, "unsigned char"));
        table.put(IntegerTypeRef.ushortRef(),
                  new IntegerType(shortsize, false, "unsigned short"));
        table.put(IntegerTypeRef.uintRef(),
                  new IntegerType(intsize, false, "unsigned int"));
        table.put(IntegerTypeRef.ulongRef(),
                  new IntegerType(longsize, false, "unsigned long"));
        return table;
    }

    private int intSize;
    private int longSize;
    private int pointerSize;
    private Map<TypeRef, Type> table;

    public TypeTable(int intSize, int longSize, int pointerSize) {
        this.intSize = intSize;
        this.longSize = longSize;
        this.pointerSize = pointerSize;
        this.table = new HashMap<TypeRef, Type>();
    }

    public boolean isDefined(TypeRef ref) {
        return table.containsKey(ref);
    }

    public void put(TypeRef ref, Type t) {
        if (table.containsKey(ref)) {
            throw new Error("duplicated type definition: " + ref);
        }
        table.put(ref, t);
    }

    public Type get(TypeRef ref) {
        Type type = table.get(ref);
        if (type == null) {
            if (ref instanceof UserTypeRef) {
                // If unregistered UserType is used in program, it causes
                // parse error instead of semantic error.  So we do not
                // need to handle this error.
                UserTypeRef uref = (UserTypeRef)ref;
                throw new Error("undefined type: " + uref.name());
            }
            else if (ref instanceof PointerTypeRef) {
                PointerTypeRef pref = (PointerTypeRef)ref;
                Type t = new PointerType(pointerSize, get(pref.baseType()));
                table.put(pref, t);
                return t;
            }
            else if (ref instanceof ArrayTypeRef) {
                ArrayTypeRef aref = (ArrayTypeRef)ref;
                Type t = new ArrayType(get(aref.baseType()),
                                       aref.length(),
                                       pointerSize);
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

    // array is really a pointer on parameters.
    public Type getParamType(TypeRef ref) {
        Type t = get(ref);
        return t.isArray() ? pointerTo(t.baseType()) : t;
    }

    public int intSize() {
        return this.intSize;
    }

    public int longSize() {
        return this.longSize;
    }

    public int pointerSize() {
        return this.pointerSize;
    }

    public int maxIntSize() {
        return this.pointerSize;
    }

    public Type ptrDiffType() {
        return get(ptrDiffTypeRef());
    }

    // returns a IntegerTypeRef whose size is equals to pointer.
    public TypeRef ptrDiffTypeRef() {
        return new IntegerTypeRef(ptrDiffTypeName());
    }

    protected String ptrDiffTypeName() {
        if (signedLong().size == pointerSize) return "long";
        if (signedInt().size == pointerSize) return "int";
        if (signedShort().size == pointerSize) return "short";
        throw new Error("must not happen: integer.size != pointer.size");
    }

    public Type signedStackType() {
        return signedLong();
    }

    public Type unsignedStackType() {
        return unsignedLong();
    }

    public Collection<Type> types() {
        return table.values();
    }

    public VoidType voidType() {
        return (VoidType)table.get(new VoidTypeRef());
    }

    public IntegerType signedChar() {
        return (IntegerType)table.get(IntegerTypeRef.charRef());
    }

    public IntegerType signedShort() {
        return (IntegerType)table.get(IntegerTypeRef.shortRef());
    }

    public IntegerType signedInt() {
        return (IntegerType)table.get(IntegerTypeRef.intRef());
    }

    public IntegerType signedLong() {
        return (IntegerType)table.get(IntegerTypeRef.longRef());
    }

    public IntegerType unsignedChar() {
        return (IntegerType)table.get(IntegerTypeRef.ucharRef());
    }

    public IntegerType unsignedShort() {
        return (IntegerType)table.get(IntegerTypeRef.ushortRef());
    }

    public IntegerType unsignedInt() {
        return (IntegerType)table.get(IntegerTypeRef.uintRef());
    }

    public IntegerType unsignedLong() {
        return (IntegerType)table.get(IntegerTypeRef.ulongRef());
    }

    public PointerType pointerTo(Type baseType) {
        return new PointerType(pointerSize, baseType);
    }

    public void semanticCheck(ErrorHandler h) {
        for (Type t : types()) {
            // We can safely use "instanceof" instead of isXXXX() here,
            // because the type refered from UserType must be also
            // kept in this table.
            if (t instanceof CompositeType) {
                checkVoidMembers((CompositeType)t, h);
                checkDuplicatedMembers((CompositeType)t, h);
            }
            else if (t instanceof ArrayType) {
                checkVoidMembers((ArrayType)t, h);
            }
            checkRecursiveDefinition(t, h);
        }
    }

    protected void checkVoidMembers(ArrayType t, ErrorHandler h) {
        if (t.baseType().isVoid()) {
            h.error("array cannot contain void");
        }
    }

    protected void checkVoidMembers(CompositeType t, ErrorHandler h) {
        for (Slot s : t.members()) {
            if (s.type().isVoid()) {
                h.error(t.location(), "struct/union cannot contain void");
            }
        }
    }

    protected void checkDuplicatedMembers(CompositeType t, ErrorHandler h) {
        Map<String, Slot> seen = new HashMap<String, Slot>();
        for (Slot s : t.members()) {
            if (seen.containsKey(s.name())) {
                h.error(t.location(),
                        t.toString() + " has duplicated member: " + s.name());
            }
            seen.put(s.name(), s);
        }
    }

    // #@@range/checkRecursiveDefinition{
    protected void checkRecursiveDefinition(Type t, ErrorHandler h) {
        _checkRecursiveDefinition(t, new HashMap<Type, Object>(), h);
    }

    static final protected Object checking = new Object();
    static final protected Object checked = new Object();

    protected void _checkRecursiveDefinition(Type t,
                                             Map<Type, Object> marks,
                                             ErrorHandler h) {
        if (marks.get(t) == checking) {
            h.error(((NamedType)t).location(),
                    "recursive type definition: " + t);
            return;
        }
        else if (marks.get(t) == checked) {
            return;
        }
        else {
            marks.put(t, checking);
            if (t instanceof CompositeType) {
                CompositeType ct = (CompositeType)t;
                for (Slot s : ct.members()) {
                    _checkRecursiveDefinition(s.type(), marks, h);
                }
            }
            else if (t instanceof ArrayType) {
                ArrayType at = (ArrayType)t;
                _checkRecursiveDefinition(at.baseType(), marks, h);
            }
            else if (t instanceof UserType) {
                UserType ut = (UserType)t;
                _checkRecursiveDefinition(ut.realType(), marks, h);
            }
            marks.put(t, checked);
        }
    }
    // #@@}
}
