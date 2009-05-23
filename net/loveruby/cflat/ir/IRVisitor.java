package net.loveruby.cflat.ir;

public interface IRVisitor<S,E> {
    public S visit(ExprStmt s);
    public S visit(Assign s);
    public S visit(CJump s);
    public S visit(Jump s);
    public S visit(Switch s);
    public S visit(LabelStmt s);
    public S visit(Return s);

    public E visit(Uni s);
    public E visit(Bin s);
    public E visit(Call s);
    public E visit(Addr s);
    public E visit(Mem s);
    public E visit(Var s);
    public E visit(Int s);
    public E visit(Str s);
}
