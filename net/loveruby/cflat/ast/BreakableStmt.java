package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public interface BreakableStmt {
    public Label endLabel();
}
