package net.loveruby.cflat.ast;

public interface DefinitionVisitor {
    public void visit(DefinedVariable var);
    public void visit(UndefinedVariable var);
    public void visit(DefinedFunction func);
    public void visit(UndefinedFunction func);
    public void visit(StructNode struct);
    public void visit(UnionNode union);
    public void visit(TypedefNode typedef);
}
