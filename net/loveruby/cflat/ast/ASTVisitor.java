package net.loveruby.cflat.ast;

public interface ASTVisitor {
    // Declarations
    public DefinedVariable visit(DefinedVariable var);
    public UndefinedVariable visit(UndefinedVariable var);
    public DefinedFunction visit(DefinedFunction func);
    public UndefinedFunction visit(UndefinedFunction func);
    public StructNode visit(StructNode struct);
    public UnionNode visit(UnionNode union);
    public TypedefNode visit(TypedefNode typedef);

    // Statements
    public BlockNode visit(BlockNode node);
    public ExprStmtNode visit(ExprStmtNode node);
    public IfNode visit(IfNode node);
    public SwitchNode visit(SwitchNode node);
    public CaseNode visit(CaseNode node);
    public WhileNode visit(WhileNode node);
    public DoWhileNode visit(DoWhileNode node);
    public ForNode visit(ForNode node);
    public BreakNode visit(BreakNode node);
    public ContinueNode visit(ContinueNode node);
    public GotoNode visit(GotoNode node);
    public LabelNode visit(LabelNode node);
    public ReturnNode visit(ReturnNode node);

    // Expressions
    public AssignNode visit(AssignNode node);
    public OpAssignNode visit(OpAssignNode node);
    public CondExprNode visit(CondExprNode node);
    public LogicalOrNode visit(LogicalOrNode node);
    public LogicalAndNode visit(LogicalAndNode node);
    public BinaryOpNode visit(BinaryOpNode node);
    public UnaryOpNode visit(UnaryOpNode node);
    public PrefixOpNode visit(PrefixOpNode node);
    public SuffixOpNode visit(SuffixOpNode node);
    public ArefNode visit(ArefNode node);
    public MemberNode visit(MemberNode node);
    public PtrMemberNode visit(PtrMemberNode node);
    public FuncallNode visit(FuncallNode node);
    public DereferenceNode visit(DereferenceNode node);
    public AddressNode visit(AddressNode node);
    public CastNode visit(CastNode node);
    public SizeofExprNode visit(SizeofExprNode node);
    public SizeofTypeNode visit(SizeofTypeNode node);
    public VariableNode visit(VariableNode node);
    public IntegerLiteralNode visit(IntegerLiteralNode node);
    public StringLiteralNode visit(StringLiteralNode node);
}
