package net.loveruby.cflat.type;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.exception.*;
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
                Type t = new ArrayType(get(aref.baseType()), aref.length());
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

    public PointerType pointerTo(Type baseType) {
        return new PointerType(pointerSize, baseType);
    }

    public void semanticCheck(ErrorHandler h) {
        Iterator types = table.values().iterator();
        while (types.hasNext()) {
            // We can safely use instanceof instead of isXXXX() here,
            // because the type refered from UserType must be also
            // kept in this table.
            Type t = (Type)types.next();
            if (t instanceof ComplexType) {
                checkVoidMembers((ComplexType)t, h);
                checkDuplicatedMembers((ComplexType)t, h);
                checkRecursiveDefinition((ComplexType)t, h);
            }
            else if (t instanceof ArrayType) {
                checkVoidMembers((ArrayType)t, h);
            }
            else if (t instanceof UserType) {
                checkRecursiveDefinition((UserType)t, h);
            }
        }
    }

    protected void checkVoidMembers(ArrayType t, ErrorHandler h) {
        if (t.baseType().isVoid()) {
            h.error("array cannot contain void");
        }
    }

    protected void checkVoidMembers(ComplexType t, ErrorHandler h) {
        Iterator membs = t.members();
        while (membs.hasNext()) {
            Slot memb = (Slot)membs.next();
            if (memb.type().isVoid()) {
                h.error("struct/union cannot contain void");
            }
        }
    }

    protected void checkDuplicatedMembers(ComplexType t, ErrorHandler h) {
        Map seen = new HashMap();
        Iterator membs = t.members();
        while (membs.hasNext()) {
            Slot memb = (Slot)membs.next();
            if (seen.containsKey(memb.name())) {
                h.error(t.toString() + " has duplicated member: "
                        + memb.name());
            }
            seen.put(memb.name(), memb);
        }
    }

    protected void checkRecursiveDefinition(Type t, ErrorHandler h) {
        _checkRecursiveDefinition(t, new HashMap(), h);
    }

    static final protected Object checking = new Object();
    static final protected Object checked = new Object();

    protected void _checkRecursiveDefinition(Type t, Map seen,
                                             ErrorHandler h) {
        if (seen.get(t) == checking) {
            h.error("recursive type definition: " + t);
            return;
        }
        else if (seen.get(t) == checked) {
            return;
        }
        seen.put(t, checking);
        if (t instanceof ComplexType) {
            ComplexType ct = (ComplexType)t;
            Iterator membs = ct.members();
            while (membs.hasNext()) {
                Slot slot = (Slot)membs.next();
                if (slot.type().isComplexType()) {
                    _checkRecursiveDefinition(slot.type().getComplexType(),
                                              seen, h);
                }
            }
        }
        else if (t instanceof UserType) {
            UserType ut = (UserType)t;
            if (ut.realType() instanceof UserType) {
                _checkRecursiveDefinition(ut.realType(), seen, h);
            }
        }
        seen.put(t, checked);
    }
}
