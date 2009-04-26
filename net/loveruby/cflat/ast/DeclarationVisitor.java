package net.loveruby.cflat.ast;

public interface DeclarationVisitor<T> {
    public T visit(DefinedVariable var);
    public T visit(UndefinedVariable var);
    public T visit(DefinedFunction func);
    public T visit(UndefinedFunction func);
    public T visit(StructNode struct);
    public T visit(UnionNode union);
    public T visit(TypedefNode typedef);
}
