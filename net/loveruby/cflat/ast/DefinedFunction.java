package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.Symbol;
import net.loveruby.cflat.asm.BaseSymbol;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class DefinedFunction extends Function {
    protected Params params;
    protected BlockNode body;
    protected Map<String, JumpEntry> jumpMap;
    protected LocalScope scope;
    protected Label epilogueLabel;

    public DefinedFunction(boolean priv,
                           TypeNode type,
                           String name,
                           Params params,
                           BlockNode body) {
        super(priv, type, name);
        this.params = params;
        this.body = body;
        this.jumpMap = new HashMap<String, JumpEntry>();
        this.epilogueLabel = new Label();
    }

    public boolean isDefined() {
        return true;
    }

    public List<Parameter> parameters() {
        return params.parameters();
    }

    public BlockNode body() {
        return body;
    }

    public void setScope(LocalScope scope) {
        this.scope = scope;
    }

    /**
     * Returns function local variables.
     * Does NOT include paramters.
     * Does NOT include static local variables.
     */
    public List<DefinedVariable> localVariables() {
        return scope.allLocalVariables();
    }

    public Label epilogueLabel() {
        return this.epilogueLabel;
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
        JumpEntry ent = jumpMap.get(name);
        if (ent == null) {
            ent = new JumpEntry(new Label());
            jumpMap.put(name, ent);
        }
        return ent;
    }

    public void checkJumpLinks(ErrorHandler handler) {
        for (Map.Entry<String, JumpEntry> ent : jumpMap.entrySet()) {
            String labelName = ent.getKey();
            JumpEntry jump = ent.getValue();
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
