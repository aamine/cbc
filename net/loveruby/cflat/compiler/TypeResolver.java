package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class TypeResolver extends Visitor {
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
        resolveNodeList(ast.types());
        resolveNodeList(ast.declarations());
        resolveNodeList(ast.entities());
    }
    // #@@}

    // #@@range/resolveNodeList{
    protected void resolveNodeList(List<? extends Node> nodes) {
        visitNodeList(nodes);
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

    // #@@range/StructNode{
    public void visit(StructNode struct) {
        resolveCompositeType(struct);
    }
    // #@@}

    // #@@range/UnionNode{
    public void visit(UnionNode union) {
        resolveCompositeType(union);
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
    public void visit(TypedefNode typedef) {
        bindType(typedef.typeNode());
        bindType(typedef.realTypeNode());
    }
    // #@@}

    // #@@range/DefinedVariable{
    public void visit(DefinedVariable var) {
        bindType(var.typeNode());
        super.visit(var);       // resolve initializer
    }
    // #@@}

    public void visit(UndefinedVariable var) {
        bindType(var.typeNode());
    }

    // #@@range/DefinedFunction{
    public void visit(DefinedFunction func) {
        resolveFunctionHeader(func);
        visitNode(func.body());
    }
    // #@@}

    public void visit(UndefinedFunction func) {
        resolveFunctionHeader(func);
    }

    // #@@range/resolveFunctionHeader{
    protected void resolveFunctionHeader(Function func) {
        bindType(func.typeNode());
        for (Parameter param : func.parameters()) {
            bindType(param.typeNode());
        }
    }
    // #@@}

    public void visit(AddressNode node) {
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
    }

    public void visit(CastNode node) {
        bindType(node.typeNode());
        super.visit(node);
    }

    public void visit(SizeofExprNode node) {
        bindType(node.typeNode());
        super.visit(node);
    }

    public void visit(SizeofTypeNode node) {
        bindType(node.operandTypeNode());
        bindType(node.typeNode());
        super.visit(node);
    }

    public void visit(IntegerLiteralNode node) {
        bindType(node.typeNode());
    }

    public void visit(StringLiteralNode node) {
        bindType(node.typeNode());
    }

    protected void error(Node node, String msg) {
        errorHandler.error(node.location(), msg);
    }
}
