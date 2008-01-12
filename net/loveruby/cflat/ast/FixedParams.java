package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class FixedParams extends Params {
    protected Location location;
    protected List parameters;

    public FixedParams(List params) {
        this(null, params);
    }

    public FixedParams(Location loc, List params) {
        super();
        this.location = loc;
        this.parameters = params;
    }

    public Location location() {
        return location;
    }

    /**
     * For DefinedFunction and UndefinedFunction, returns an iterator to
     * the list of parameters (Parameter).  For FunctionType, returns an
     * iterator to the list of parameter type (Type).
     */
    public Iterator parameters() {
        return parameters.iterator();
    }

    public int argc() {
        return parameters.size();
    }

    public int minArgc() {
        return parameters.size();
    }

    /**
     * For DefinedFunction and UndefinedFunction, returns a list of
     * parameters (Parameter).  For FunctionType, returns a list of
     * parameter types (Type).
     */
    protected List parametersList() {
        return parameters;
    }

    public boolean isVararg() {
        return false;
    }

    public boolean equals(Object other) {
        if (!(other instanceof FixedParams)) return false;
        FixedParams params = (FixedParams)other;
        return parameters.equals(params.parametersList());
    }

    public Params internTypes(TypeTable table) {
        Iterator it = parameters.iterator();
        List types = new ArrayList();
        while (it.hasNext()) {
            types.add(table.get((TypeRef)it.next()));
        }
        return new FixedParams(location, types);
    }

    public Params typeRefs() {
        Iterator it = parameters.iterator();
        List typerefs = new ArrayList();
        while (it.hasNext()) {
            Parameter param = (Parameter)it.next();
            typerefs.add(param.typeNode().typeRef());
        }
        return new FixedParams(location, typerefs);
    }

    /** parameters is a list of Type when
     *  this object is hold in FunctionType. */
    public Iterator types() {
        return parameters.iterator();
    }

    public boolean isSameType(Params other) {
        if (other.isVararg()) return false;
        if (other.argc() != argc()) return false;
        Iterator types = types();
        Iterator otherTypes = other.types();
        while (types.hasNext()) {
            Type t = (Type)types.next();
            Type tt = (Type)otherTypes.next();
            if (t.isSameType(t))
                return false;
        }
        return true;
    }
}
