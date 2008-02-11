package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class TypeResolver extends Visitor {
    static public void resolve(AST ast, TypeTable typeTable,
                               ErrorHandler errorHandler) {
        new TypeResolver(typeTable, errorHandler).resolveProgram(ast);
    }

    // #@@range/ctor{
    protected TypeTable typeTable;
    protected ErrorHandler errorHandler;

    public TypeResolver(TypeTable typeTable, ErrorHandler errorHandler) {
        this.typeTable = typeTable;
        this.errorHandler = errorHandler;
    }
    // #@@}

    // #@@range/resolveNodeList{
    protected void resolveNodeList(Iterator nodes) {
        visitNodeList(nodes);
    }
    // #@@}

    // #@@range/resolveProgram{
    public void resolveProgram(AST ast) {
        defineTypes(ast.types());
        resolveNodeList(ast.types());
        resolveNodeList(ast.declarations());
        resolveNodeList(ast.entities());
    }
    // #@@}

    // #@@range/defineTypes{
    private void defineTypes(Iterator deftypes) {
        while (deftypes.hasNext()) {
            TypeDefinition def = (TypeDefinition)deftypes.next();
            typeTable.put(def.typeRef(), def.definingType());
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
        resolveComplexType(struct);
    }
    // #@@}

    // #@@range/UnionNode{
    public void visit(UnionNode union) {
        resolveComplexType(union);
    }
    // #@@}

    // #@@range/resolveComplexType{
    public void resolveComplexType(ComplexTypeDefinition def) {
        ComplexType ct = (ComplexType)typeTable.get(def.typeNode().typeRef());
        if (ct == null) {
            throw new Error("cannot intern struct/union: " + def.name());
        }
        Iterator membs = ct.members();
        while (membs.hasNext()) {
            Slot slot = (Slot)membs.next();
            bindType(slot.typeNode());
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
        Iterator params = func.parameters();
        while (params.hasNext()) {
            Parameter param = (Parameter)params.next();
            bindType(param.typeNode());
        }
    }
    // #@@}

    public void visit(AddressNode node) {
        super.visit(node);
        // to avoid SemanticError which occurs when getting type of
        // expr which is not assignable.
        try {
            Type t = typeTable.pointerTo(node.expr().type());
            node.setType(t);
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

    public void visit(IntegerLiteralNode node) {
        bindType(node.typeNode());
    }

    public void visit(StringLiteralNode node) {
        bindType(node.typeNode());
    }
}
