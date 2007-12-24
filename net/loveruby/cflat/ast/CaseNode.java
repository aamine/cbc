package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CaseNode extends Node {
    protected LabelPool pool;
    protected Label beginLabel;
    protected List values;
    protected BlockNode body;

    public CaseNode(LabelPool pool, List values, BlockNode body) {
        super();
        this.pool = pool;
        this.values = values;
        this.body = body;
        this.beginLabel = null;
    }

    public Iterator values() {
        return values.iterator();
    }

    public BlockNode body() {
        return body;
    }

    public Label beginLabel() {
        if (beginLabel == null) {
            beginLabel = pool.newLabel();
        }
        return beginLabel;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
