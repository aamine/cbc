package net.loveruby.cflat.compiler;
import net.loveruby.cflat.parser.Parser;
import net.loveruby.cflat.ast.AST;
import net.loveruby.cflat.ast.StmtNode;
import net.loveruby.cflat.ast.ExprNode;
import net.loveruby.cflat.ir.IR;
import net.loveruby.cflat.sysdep.CodeGenerator;
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

    private ErrorHandler errorHandler;

    public Compiler(String programName) {
        this.errorHandler = new ErrorHandler(programName);
    }
    // #@@}

    public void commandMain(String[] origArgs) {
        Options opts = new Options();
        List<SourceFile> srcs = null;
        try {
            srcs = opts.parse(Arrays.asList(origArgs));
        }
        catch (OptionParseError err) {
            errorHandler.error(err.getMessage());
            errorHandler.error("Try cbc --help for option usage");
            System.exit(1);
        }
        if (opts.mode() == CompilerMode.CheckSyntax) {
            boolean failed = false;
            for (SourceFile src : srcs) {
                if (isValidSyntax(src, opts)) {
                    System.out.println(src.name() + ": Syntax OK");
                }
                else {
                    System.out.println(src.name() + ": Syntax Error");
                    failed = true;
                }
            }
            System.exit(failed ? 1 : 0);
        }
        else {
            try {
                buildTarget(srcs, opts);
                System.exit(0);
            }
            catch (CompileException ex) {
                errorHandler.error(ex.getMessage());
                System.exit(1);
            }
        }
    }

    private void errorExit(String msg) {
        errorHandler.error(msg);
        System.exit(1);
    }

    private boolean isValidSyntax(SourceFile src, Options opts) {
        try {
            parseFile(src, opts);
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

    // #@@range/buildTarget{
    public void buildTarget(List<SourceFile> srcs, Options opts)
                                        throws CompileException {
        for (SourceFile src : srcs) {
            compileFile(src, opts);
        }
        if (! opts.isLinkRequired()) System.exit(0);
        if (! opts.isGeneratingSharedLibrary()) {
            generateExecutable(opts);
        }
        else {
            generateSharedLibrary(opts);
        }
    }
    // #@@}

    public void compileFile(SourceFile src, Options opts)
                                        throws CompileException {
        if (src.isCflatSource()) {
            AST ast = parseFile(src, opts);
            switch (opts.mode()) {
            case DumpTokens:
                ast.dumpTokens(System.out);
                return;
            case DumpAST:
                ast.dump();
                return;
            case DumpStmt:
                findStmt(ast).dump();
                return;
            case DumpExpr:
                findExpr(ast).dump();
                return;
            }
            ast.setTypeTable(opts.typeTable());
            semanticAnalysis(ast, opts);
            switch (opts.mode()) {
            case DumpReference:
                return;
            case DumpSemantic:
                ast.dump();
                return;
            }
            IR ir = new IRGenerator(errorHandler).generate(ast);
            if (opts.mode() == CompilerMode.DumpIR) {
                ir.dump();
                return;
            }
            String asm = generateAssembly(ir, opts);
            if (opts.mode() == CompilerMode.DumpAsm) {
                System.out.println(asm);
                return;
            }
            writeFile(src.asmFileName(opts), asm);
            src.setCurrentName(src.asmFileName(opts));
            if (opts.mode() == CompilerMode.Compile) {
                return;
            }
        }
        if (! opts.isAssembleRequired()) return;
        if (src.isAssemblySource()) {
            assemble(src.asmFileName(opts), src.objFileName(opts), opts);
            src.setCurrentName(src.objFileName(opts));
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

    public AST parseFile(SourceFile src, Options opts)
                            throws SyntaxException, FileException {
        return Parser.parseFile(new File(src.currentName()),
                                opts.loader(),
                                errorHandler,
                                opts.doesDebugParser());
    }

    public void semanticAnalysis(AST ast, Options opts)
                                        throws SemanticException {
        new LocalResolver(errorHandler).resolve(ast);
        new TypeResolver(errorHandler).resolve(ast);
        ast.typeTable().semanticCheck(errorHandler);
        new DereferenceChecker(errorHandler).check(ast);
        if (opts.mode() == CompilerMode.DumpReference) {
            ast.dump();
            return;
        }
        new TypeChecker(errorHandler).check(ast);
    }

    public String generateAssembly(IR ir, Options opts) {
        CodeGenerator gen = opts.codeGenerator(errorHandler);
        return gen.generate(ir);
    }

    private void assemble(String srcPath,
                            String destPath,
                            Options opts) throws IPCException {
        opts.assembler(errorHandler)
            .assemble(srcPath, destPath, opts.asOptions());
    }

    private void generateExecutable(Options opts) throws IPCException {
        opts.linker(errorHandler)
            .generateExecutable(opts.exeFileName(), opts.ldOptions());
    }

    private void generateSharedLibrary(Options opts) throws IPCException {
        opts.linker(errorHandler)
            .generateSharedLibrary(opts.soFileName(), opts.ldOptions());
    }

    private void writeFile(String path, String str)
                                    throws FileException {
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
}
