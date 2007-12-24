package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public interface ContinueableStmt {
    public Label continueLabel();
}
