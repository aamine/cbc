package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class TypeResolver extends Visitor
        implements EntityVisitor<Void>, DeclarationVisitor<Void> {
    // #@@range/ctor{
    protected TypeTable typeTable;
    protected ErrorHandler errorHandler;

    public TypeResolver(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    // #@@}

    // #@@range/resolveProgram{
    public void resolve(AST ast) {
        this.typeTable = ast.typeTable();
        defineTypes(ast.types());
        // #@@range/resolveProgram_core{
        for (TypeDefinition t : ast.types()) {
            t.accept(this);
        }
        for (Entity d : ast.declarations()) {
            d.accept(this);
        }
        for (Entity e : ast.entities()) {
            e.accept(this);
        }
        // #@@}
    }
    // #@@}

    // #@@range/defineTypes{
    private void defineTypes(List<TypeDefinition> deftypes) {
        for (TypeDefinition def : deftypes) {
            if (typeTable.isDefined(def.typeRef())) {
                error(def, "duplicated type definition: " + def.typeRef());
            }
            else {
                typeTable.put(def.typeRef(), def.definingType());
            }
        }
    }
    // #@@}

    // #@@range/bindType{
    private void bindType(TypeNode n) {
        if (n.isResolved()) return;
        n.setType(typeTable.get(n.typeRef()));
    }
    // #@@}

    //
    // Declarations
    //

    // #@@range/StructNode{
    public Void visit(StructNode struct) {
        resolveCompositeType(struct);
        return null;
    }
    // #@@}

    // #@@range/UnionNode{
    public Void visit(UnionNode union) {
        resolveCompositeType(union);
        return null;
    }
    // #@@}

    // #@@range/resolveCompositeType{
    public void resolveCompositeType(CompositeTypeDefinition def) {
        CompositeType ct = (CompositeType)typeTable.get(def.typeNode().typeRef());
        if (ct == null) {
            throw new Error("cannot intern struct/union: " + def.name());
        }
        for (Slot s : ct.members()) {
            bindType(s.typeNode());
        }
    }
    // #@@}

    // #@@range/TypedefNode{
    public Void visit(TypedefNode typedef) {
        bindType(typedef.typeNode());
        bindType(typedef.realTypeNode());
        return null;
    }
    // #@@}

    //
    // Entities
    //

    // #@@range/DefinedVariable{
    public Void visit(DefinedVariable var) {
        bindType(var.typeNode());
        if (var.hasInitializer()) {
            visitExpr(var.initializer());
        }
        return null;
    }
    // #@@}

    public Void visit(UndefinedVariable var) {
        bindType(var.typeNode());
        return null;
    }

    // #@@range/DefinedFunction{
    public Void visit(DefinedFunction func) {
        resolveFunctionHeader(func);
        visitStmt(func.body());
        return null;
    }
    // #@@}

    public Void visit(UndefinedFunction func) {
        resolveFunctionHeader(func);
        return null;
    }

    // #@@range/resolveFunctionHeader{
    protected void resolveFunctionHeader(Function func) {
        bindType(func.typeNode());
        for (Parameter param : func.parameters()) {
            Type t = typeTable.get(param.typeNode().typeRef());
            // arrays must be converted to pointers in a function parameter.
            if (t.isArray()) {
                t = typeTable.pointerTo(t.getArrayType().baseType());
            }
            param.typeNode().setType(t);
        }
    }
    // #@@}

    //
    // Expressions
    //

    public Void visit(BlockNode node) {
        for (DefinedVariable var : node.variables()) {
            var.accept(this);
        }
        visitStmts(node.stmts());
        return null;
    }

    public Void visit(AddressNode node) {
        super.visit(node);
        // to avoid SemanticError which occurs when getting type of
        // expr which is not assignable.
        try {
            Type base = node.expr().type();
            if (node.expr().shouldEvaluatedToAddress()) {
                node.setType(base);
            }
            else {
                node.setType(typeTable.pointerTo(base));
            }
        }
        catch (SemanticError err) {
            Type t = typeTable.pointerTo(typeTable.voidType());
            node.setType(t);
        }
        return null;
    }

    public Void visit(CastNode node) {
        bindType(node.typeNode());
        super.visit(node);
        return null;
    }

    public Void visit(SizeofExprNode node) {
        bindType(node.typeNode());
        super.visit(node);
        return null;
    }

    public Void visit(SizeofTypeNode node) {
        bindType(node.operandTypeNode());
        bindType(node.typeNode());
        super.visit(node);
        return null;
    }

    public Void visit(IntegerLiteralNode node) {
        bindType(node.typeNode());
        return null;
    }

    public Void visit(StringLiteralNode node) {
        bindType(node.typeNode());
        return null;
    }

    protected void error(Node node, String msg) {
        errorHandler.error(node.location(), msg);
    }
}
