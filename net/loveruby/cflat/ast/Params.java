package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.TypeRef;
import net.loveruby.cflat.type.ParamTypeRefs;
import java.util.*;

public class Params extends ParamSlots<Parameter> {
    public Params(Location loc, List<Parameter> paramDescs) {
        super(loc, paramDescs, false);
    }

    public List<Parameter> parameters() {
        return paramDescriptors;
    }

    public ParamTypeRefs parametersTypeRef() {
        List<TypeRef> typerefs = new ArrayList<TypeRef>();
        for (Parameter param : paramDescriptors) {
            typerefs.add(param.typeNode().typeRef());
        }
        return new ParamTypeRefs(location, typerefs, vararg);
    }

    public boolean equals(Object other) {
        return (other instanceof Params) && equals((Params)other);
    }

    public boolean equals(Params other) {
        return other.vararg == vararg
                && other.paramDescriptors.equals(paramDescriptors);
    }
    
    protected void _dump(Dumper d) {
        d.printNodeList("parameters", parameters());
    }
}
