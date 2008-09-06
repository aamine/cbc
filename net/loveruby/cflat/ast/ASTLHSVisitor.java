package net.loveruby.cflat.ast;

public interface ASTLHSVisitor {
    public void visitLHS(ArefNode node);
    public void visitLHS(MemberNode node);
    public void visitLHS(PtrMemberNode node);
    public void visitLHS(DereferenceNode node);
    public void visitLHS(VariableNode node);
}
