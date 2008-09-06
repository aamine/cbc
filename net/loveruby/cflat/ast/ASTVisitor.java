package net.loveruby.cflat.ast;

public interface ASTVisitor {
    // Declarations
    public void visit(DefinedVariable var);
    public void visit(UndefinedVariable var);
    public void visit(DefinedFunction func);
    public void visit(UndefinedFunction func);
    public void visit(StructNode struct);
    public void visit(UnionNode union);
    public void visit(TypedefNode typedef);

    // Statements
    public void visit(BlockNode node);
    public void visit(IfNode node);
    public void visit(SwitchNode node);
    public void visit(CaseNode node);
    public void visit(WhileNode node);
    public void visit(DoWhileNode node);
    public void visit(ForNode node);
    public void visit(BreakNode node);
    public void visit(ContinueNode node);
    public void visit(GotoNode node);
    public void visit(LabelNode node);
    public void visit(ReturnNode node);

    // Expressions
    public void visit(AssignNode node);
    public void visit(OpAssignNode node);
    public void visit(CondExprNode node);
    public void visit(LogicalOrNode node);
    public void visit(LogicalAndNode node);
    public void visit(BinaryOpNode node);
    public void visit(UnaryOpNode node);
    public void visit(PrefixOpNode node);
    public void visit(SuffixOpNode node);
    public void visit(ArefNode node);
    public void visit(MemberNode node);
    public void visit(PtrMemberNode node);
    public void visit(FuncallNode node);
    public void visit(DereferenceNode node);
    public void visit(AddressNode node);
    public void visit(CastNode node);
    public void visit(SizeofExprNode node);
    public void visit(SizeofTypeNode node);
    public void visit(VariableNode node);
    public void visit(IntegerLiteralNode node);
    public void visit(StringLiteralNode node);
}
