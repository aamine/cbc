package net.loveruby.cflat.compiler;
import net.loveruby.cflat.parser.Parser;
import net.loveruby.cflat.ast.AST;
import net.loveruby.cflat.ast.StmtNode;
import net.loveruby.cflat.ast.ExprNode;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.ir.IR;
import net.loveruby.cflat.sysdep.CodeGenerator;
import net.loveruby.cflat.sysdep.AssemblyCode;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;
import java.io.*;

public class Compiler {
    // #@@range/main{
    static final public String ProgramName = "cbc";
    static final public String Version = "1.0.0";

    static public void main(String[] args) {
        new Compiler(ProgramName).commandMain(args);
    }

    private final ErrorHandler errorHandler;

    public Compiler(String programName) {
        this.errorHandler = new ErrorHandler(programName);
    }
    // #@@}

    public void commandMain(String[] args) {
        Options opts = parseOptions(args);
        if (opts.mode() == CompilerMode.CheckSyntax) {
            System.exit(checkSyntax(opts) ? 0 : 1);
        }
        try {
            List<SourceFile> srcs = opts.sourceFiles();
            build(srcs, opts);
            System.exit(0);
        }
        catch (CompileException ex) {
            errorHandler.error(ex.getMessage());
            System.exit(1);
        }
    }

    private Options parseOptions(String[] args) {
        try {
            return Options.parse(args);
        }
        catch (OptionParseError err) {
            errorHandler.error(err.getMessage());
            errorHandler.error("Try \"cbc --help\" for usage");
            System.exit(1);
            return null;   // never reach
        }
    }

    private boolean checkSyntax(Options opts) {
        boolean failed = false;
        for (SourceFile src : opts.sourceFiles()) {
            if (isValidSyntax(src.path(), opts)) {
                System.out.println(src.path() + ": Syntax OK");
            }
            else {
                System.out.println(src.path() + ": Syntax Error");
                failed = true;
            }
        }
        return !failed;
    }

    private boolean isValidSyntax(String path, Options opts) {
        try {
            parseFile(path, opts);
            return true;
        }
        catch (SyntaxException ex) {
            return false;
        }
        catch (FileException ex) {
            errorHandler.error(ex.getMessage());
            return false;
        }
    }

    // #@@range/build{
    public void build(List<SourceFile> srcs, Options opts)
                                        throws CompileException {
        for (SourceFile src : srcs) {
            if (src.isCflatSource()) {
                String destPath = opts.asmFileNameOf(src);
                compile(src.path(), destPath, opts);
                src.setCurrentName(destPath);
            }
            if (! opts.isAssembleRequired()) continue;
            if (src.isAssemblySource()) {
                String destPath = opts.objFileNameOf(src);
                assemble(src.path(), destPath, opts);
                src.setCurrentName(destPath);
            }
        }
        if (! opts.isLinkRequired()) return;
        link(opts);
    }
    // #@@}

    public void compile(String srcPath, String destPath,
                        Options opts) throws CompileException {
        AST ast = parseFile(srcPath, opts);
        if (dumpAST(ast, opts.mode())) return;
        TypeTable types = opts.typeTable();
        AST sem = semanticAnalyze(ast, types, opts);
        if (dumpSemant(sem, opts.mode())) return;
        IR ir = new IRGenerator(types, errorHandler).generate(sem);
        if (dumpIR(ir, opts.mode())) return;
        AssemblyCode asm = generateAssembly(ir, opts);
        if (dumpAsm(asm, opts.mode())) return;
        if (printAsm(asm, opts.mode())) return;
        writeFile(destPath, asm.toSource());
    }

    public AST parseFile(String path, Options opts)
                            throws SyntaxException, FileException {
        return Parser.parseFile(new File(path),
                opts.loader(), errorHandler, opts.doesDebugParser());
    }

    public AST semanticAnalyze(AST ast, TypeTable types,
                Options opts) throws SemanticException {
        new LocalResolver(errorHandler).resolve(ast);
        new TypeResolver(types, errorHandler).resolve(ast);
        types.semanticCheck(errorHandler);
        if (opts.mode() == CompilerMode.DumpReference) {
            ast.dump();
            return ast;
        }
        new DereferenceChecker(types, errorHandler).check(ast);
        new TypeChecker(types, errorHandler).check(ast);
        return ast;
    }

    public AssemblyCode generateAssembly(IR ir, Options opts) {
        return opts.codeGenerator(errorHandler).generate(ir);
    }

    // #@@range/assemble{
    public void assemble(String srcPath, String destPath,
                            Options opts) throws IPCException {
        opts.assembler(errorHandler)
            .assemble(srcPath, destPath, opts.asOptions());
    }
    // #@@}

    // #@@range/link{
    public void link(Options opts) throws IPCException {
        if (! opts.isGeneratingSharedLibrary()) {
            generateExecutable(opts);
        }
        else {
            generateSharedLibrary(opts);
        }
    }
    // #@@}

    // #@@range/generateExecutable{
    public void generateExecutable(Options opts) throws IPCException {
        opts.linker(errorHandler).generateExecutable(
                opts.ldArgs(), opts.exeFileName(), opts.ldOptions());
    }
    // #@@}

    // #@@range/generateSharedLibrary{
    public void generateSharedLibrary(Options opts) throws IPCException {
        opts.linker(errorHandler).generateSharedLibrary(
                opts.ldArgs(), opts.soFileName(), opts.ldOptions());
    }
    // #@@}

    private void writeFile(String path, String str) throws FileException {
        if (path.equals("-")) {
            System.out.print(str);
            return;
        }
        try {
            BufferedWriter f = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path)));
            try {
                f.write(str);
            }
            finally {
                f.close();
            }
        }
        catch (FileNotFoundException ex) {
            errorHandler.error("file not found: " + path);
            throw new FileException("file error");
        }
        catch (IOException ex) {
            errorHandler.error("IO error" + ex.getMessage());
            throw new FileException("file error");
        }
    }

    private boolean dumpAST(AST ast, CompilerMode mode) {
        switch (mode) {
        case DumpTokens:
            ast.dumpTokens(System.out);
            return true;
        case DumpAST:
            ast.dump();
            return true;
        case DumpStmt:
            findStmt(ast).dump();
            return true;
        case DumpExpr:
            findExpr(ast).dump();
            return true;
        default:
            return false;
        }
    }

    private StmtNode findStmt(AST ast) {
        StmtNode stmt = ast.getSingleMainStmt();
        if (stmt == null) {
            errorExit("source file does not contains main()");
        }
        return stmt;
    }

    private ExprNode findExpr(AST ast) {
        ExprNode expr = ast.getSingleMainExpr();
        if (expr == null) {
            errorExit("source file does not contains single expression");
        }
        return expr;
    }

    private boolean dumpSemant(AST ast, CompilerMode mode) {
        switch (mode) {
        case DumpReference:
            return true;
        case DumpSemantic:
            ast.dump();
            return true;
        default:
            return false;
        }
    }

    private boolean dumpIR(IR ir, CompilerMode mode) {
        if (mode == CompilerMode.DumpIR) {
            ir.dump();
            return true;
        }
        else {
            return false;
        }
    }

    private boolean dumpAsm(AssemblyCode asm, CompilerMode mode) {
        if (mode == CompilerMode.DumpAsm) {
            asm.dump(System.out);
            return true;
        }
        else {
            return false;
        }
    }

    private boolean printAsm(AssemblyCode asm, CompilerMode mode) {
        if (mode == CompilerMode.PrintAsm) {
            System.out.print(asm.toSource());
            return true;
        }
        else {
            return false;
        }
    }

    private void errorExit(String msg) {
        errorHandler.error(msg);
        System.exit(1);
    }
}
