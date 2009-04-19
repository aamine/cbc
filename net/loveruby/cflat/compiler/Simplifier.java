package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

class Simplifier extends Visitor {
    protected ErrorHandler errorHandler;
    protected TypeTable typeTable;

    // #@@range/ctor{
    public Simplifier(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    // #@@}

    protected void compile(StmtNode node) {
        visitStmt(node);
    }

    protected void compile(ExprNode node) {
        visitExpr(node);
    }

    // #@@range/transform{
    public AST transform(AST ast) throws SemanticException {
        typeTable = ast.typeTable();
        for (DefinedVariable var : ast.definedVariables()) {
            visit(var);
        }
        for (DefinedFunction f : ast.definedFunctions()) {
            compile(f.body());
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("IR generation failed.");
        }
        return ast;
    }
    // #@@}

    public Void visit(OpAssignNode node) {
        super.visit(node);
        if (node.operator().equals("+") || node.operator().equals("-")) {
            if (node.lhs().type().isDereferable()) {
                node.setRHS(multiplyPtrBaseSize(node.rhs(), node.lhs()));
            }
        }
        return null;
    }

    // #@@range/BinaryOpNode{
    public Void visit(BinaryOpNode node) {
        super.visit(node);
        if (node.operator().equals("+") || node.operator().equals("-")) {
            if (node.left().type().isDereferable()) {
                node.setRight(multiplyPtrBaseSize(node.right(), node.left()));
            }
            else if (node.right().type().isDereferable()) {
                node.setLeft(multiplyPtrBaseSize(node.left(), node.right()));
            }
        }
        return null;
    }
    // #@@}

    protected BinaryOpNode multiplyPtrBaseSize(ExprNode expr, ExprNode ptr) {
        return new BinaryOpNode(expr, "*", ptrBaseSize(ptr));
    }

    protected IntegerLiteralNode ptrBaseSize(ExprNode ptr) {
        return integerLiteral(ptr.location(),
                              typeTable.ptrDiffTypeRef(),
                              ptr.type().baseType().size());
    }

    protected IntegerLiteralNode integerLiteral(Location loc, TypeRef ref, long n) {
        IntegerLiteralNode node = new IntegerLiteralNode(loc, ref, n);
        bindType(node.typeNode());
        return node;
    }

    protected void bindType(TypeNode t) {
        t.setType(typeTable.get(t.typeRef()));
    }

    protected void error(Node n, String msg) {
        errorHandler.error(n.location(), msg);
    }
}
