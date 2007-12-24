package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public interface LHSNode {
    public boolean isConstantAddress();
    public AsmEntity address();
}
