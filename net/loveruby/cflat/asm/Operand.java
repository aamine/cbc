package net.loveruby.cflat.asm;

abstract public class Operand implements OperandPattern {
    abstract public String toSource(SymbolTable table);
    abstract public String dump();

    public boolean isRegister() {
        return false;
    }

    public boolean isMemoryReference() {
        return false;
    }

    public IntegerLiteral integerLiteral() {
        return null;
    }

    abstract public void collectStatistics(Statistics stats);

    // default implementation
    public boolean match(Operand operand) {
        return equals(operand);
    }
}
