package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class DefinedFunction extends Function {
    protected LabelPool labelPool;
    protected Params params;
    protected BlockNode body;
    protected Map jumpMap;
    protected Frame frame;

    public DefinedFunction(LabelPool pool, boolean priv, TypeNode type,
                           String name, Params params, BlockNode body) {
        super(priv, type, name);
        this.labelPool = pool;
        this.params = params;
        this.body = body;
        this.jumpMap = new HashMap();
    }

    /** Returns an iterator to the list of parameter slots (Slot). */
    public Iterator parameters() {
        return params.parameters();
    }

    public BlockNode body() {
        return body;
    }

    public void setFrame(Frame f) {
        frame = f;
    }

    /**
     * Returns function local variables.
     * Does NOT include paramters.
     * Does NOT include static local variables.
     */
    public Iterator localVariables() {
        return frame.allVariables();
    }

    public boolean isDefined() {
        return true;
    }

    public boolean isFunction() {
        return true;
    }

    public void defineIn(ToplevelScope toplevel) {
        toplevel.defineFunction(this);
    }

    class JumpEntry {
        public Label label;
        public long numRefered;
        public boolean isDefined;
        public Location location;

        public JumpEntry(Label label) {
            this.label = label;
            numRefered = 0;
            isDefined = false;
        }
    }

    public Label defineLabel(String name, Location loc)
                                    throws SemanticException {
        JumpEntry ent = getJumpEntry(name);
        if (ent.isDefined) {
            throw new SemanticException(
                "duplicated jump labels in " + name + "(): " + name);
        }
        ent.isDefined = true;
        ent.location = loc;
        return ent.label;
    }

    public Label referLabel(String name) {
        JumpEntry ent = getJumpEntry(name);
        ent.numRefered++;
        return ent.label;
    }

    protected JumpEntry getJumpEntry(String name) {
        JumpEntry ent = (JumpEntry)jumpMap.get(name);
        if (ent == null) {
            ent = new JumpEntry(labelPool.newLabel());
            jumpMap.put(name, ent);
        }
        return ent;
    }

    public void checkJumpLinks(ErrorHandler handler) {
        Iterator ents = jumpMap.entrySet().iterator();
        while (ents.hasNext()) {
            Map.Entry ent = (Map.Entry)ents.next();
            String labelName = (String)ent.getKey();
            JumpEntry jump = (JumpEntry)ent.getValue();
            if (!jump.isDefined) {
                handler.error(jump.location,
                              name + ": undefined label: " + labelName);
            }
            if (jump.numRefered == 0) {
                handler.warn(jump.location,
                             name + ": useless label: " + labelName);
            }
        }
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("isPrivate", isPrivate);
        d.printMember("params", params);
        d.printMember("body", body);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
