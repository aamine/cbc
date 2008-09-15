package net.loveruby.cflat.asm;
import net.loveruby.cflat.type.Type;

public class Instruction extends Assembly {
    protected String mnemonic;
    protected String suffix;
    protected AsmEntity[] operands;

    public Instruction(String mnemonic) {
        this(mnemonic, "", new AsmEntity[0]);
    }

    public Instruction(String mnemonic, String suffix, AsmEntity a1) {
        this(mnemonic, suffix, new AsmEntity[] { a1 });
    }

    public Instruction(String mnemonic, String suffix,
                       AsmEntity a1, AsmEntity a2) {
        this(mnemonic, suffix, new AsmEntity[] { a1, a2 });
    }

    public Instruction(String mnemonic, String suffix, AsmEntity[] operands) {
        this.mnemonic = mnemonic;
        this.suffix = suffix;
        this.operands = operands;
    }

    public boolean isInstruction() {
        return true;
    }

    public String mnemonic() {
        return this.mnemonic;
    }

    public int numOperands() {
        return this.operands.length;
    }

    public AsmEntity operand1() {
        return this.operands[0];
    }

    public AsmEntity operand2() {
        return this.operands[1];
    }

    public String toSource() {
        StringBuffer buf = new StringBuffer();
        buf.append("\t");
        buf.append(mnemonic + suffix);
        String sep = "\t";
        for (int i = 0; i < operands.length; i++) {
            buf.append(sep); sep = ", ";
            buf.append(operands[i]);
        }
        return buf.toString();
    }

    public String toString() {
        return "#<Insn " + mnemonic + ">";
    }
}
