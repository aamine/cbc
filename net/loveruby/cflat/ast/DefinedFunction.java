package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class DefinedFunction extends Function {
    protected LabelPool labelPool;
    protected Params params;
    protected Node body;
    protected Map jumpMap;
    protected Frame frame;

    public DefinedFunction(LabelPool pool, boolean priv, TypeNode type,
                           String name, Params params, Node body) {
        super(priv, type, name);
        this.labelPool = pool;
        this.params = params;
        this.body = body;
        this.jumpMap = new HashMap();
    }

    public Iterator parameters() {
        return params.parameters();
    }

    public Node body() {
        return body;
    }

    public void setFrame(Frame f) {
        frame = f;
    }

    // returns function local variables.
    // Does NOT include paramters.
    // Does NOT include static local variables.
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

        public JumpEntry(Label label) {
            this.label = label;
            numRefered = 0;
            isDefined = false;
        }
    }

    public Label defineLabel(String name) throws SemanticException {
        JumpEntry ent = getJumpEntry(name);
        if (ent.isDefined) {
            throw new SemanticException(
                "duplicated jump labels in " + name + "(): " + name);
        }
        ent.isDefined = true;
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
                handler.error("undefined label in function " +
                              name + ": " + labelName);
            }
            if (jump.numRefered == 0) {
                handler.warn("useless label: " + labelName);
            }
        }
    }

    public void accept(DefinitionVisitor visitor) {
        visitor.visit(this);
    }

    public AsmEntity address() {
        throw new Error("must not happen: Function#address called");
    }
}
