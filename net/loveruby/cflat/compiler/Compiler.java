package net.loveruby.cflat.compiler;
import net.loveruby.cflat.parser.*;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.utils.*;
import net.loveruby.cflat.exception.*;
import java.util.*;
import java.io.*;

public class Compiler {
    static public void main(String[] args) {
        new Compiler().commandMain(args);
    }

    static final private String programId = "cbc";
    protected TypeTable typeTable;
    protected LibraryLoader loader;
    protected ErrorHandler errorHandler;

    public Compiler() {
        typeTable = TypeTable.ilp32();
        loader = new LibraryLoader();
        errorHandler = new ErrorHandler(programId);
    }

    public Compiler(TypeTable table, LibraryLoader ld, ErrorHandler h) {
        typeTable = table;
        loader = ld;
        errorHandler = h;
    }

    public void commandMain(String[] args) {
        try {
            if (args.length == 0) errorExit("no argument given");
            if (args[0].equals("--dump-tokens")) {
                if (args.length != 2) {
                    errorExit("no file input or too many files");
                }
                dumpTokensFromFile(args[1]);
            }
            else if (args[0].equals("--dump-ast")) {
                if (args.length != 2) {
                    errorExit("no file input or too many files");
                }
                dumpASTFromFile(args[1]);
            }
            else if (args[0].equals("--check-syntax")) {
                if (args.length != 2) {
                    errorExit("no file input or too many files");
                }
                if (isValidSyntax(args[1])) {
                    System.out.println("Syntax OK");
                    System.exit(0);
                } else {
                    System.exit(1);
                }
            }
            else {
                if (args.length != 1)
                    errorExit("no file input or too many files");
                compileFile(args[0]);
            }
        }
        catch (CompileException ex) {
            System.exit(1);
        }
    }

    private void errorExit(String msg) {
        errorHandler.error(msg);
        System.exit(1);
    }

    public boolean isValidSyntax(String path) {
        try {
            parseFile(path);
            return true;
        }
        catch (CompileException ex) {
            return false;
        }
    }

    public void dumpTokensFromFile(String path) throws CompileException {
        AST ast = parseFile(path);
        Token t = ast.firstToken();
        dumpTokenList(t, System.out);
    }

    protected void dumpTokenList(Token t, PrintStream s) {
        while (t != null) {
            dumpTokenList(t.specialToken, s);
            dumpToken(t, s);
            t = t.next;
        }
    }

    static final protected int typeLen = 24;

    protected void dumpToken(Token t, PrintStream s) {
        String type = ParserConstants.tokenImage[t.kind];
        s.print(type);
        for (int n = typeLen - type.length(); n > 0; n--) {
            s.print(" ");
        }
        s.println(TextUtils.escapeString(t.image));
    }

    public void dumpASTFromFile(String path) throws CompileException {
        parseFile(path).dump();
    }

    public void compileFile(String path) throws CompileException {
        AST ast = parseFile(path);
        TypeTable typeTable = TypeTable.ilp32();
        semanticAnalysis(ast, typeTable);
        String asm = CodeGenerator.generate(ast, typeTable, errorHandler);
        writeFile(asmFileName(path), asm);
        assemble(asmFileName(path));
    }

    public void semanticAnalysis(AST ast, TypeTable typeTable)
                                            throws SemanticException {
        JumpResolver.resolve(ast, errorHandler);
        LocalReferenceResolver.resolve(ast, errorHandler);
        TypeResolver.resolve(ast, typeTable, errorHandler);
        typeTable.semanticCheck(errorHandler);
        TypeChecker.check(ast, typeTable, errorHandler);
    }

    public AST parseFile(String path) throws CompileException {
        return Parser.parseFile(new File(path), loader, errorHandler);
    }

    public void assemble(String path) throws IPCException {
        String[] cmd = {"gcc", path, "-o", cmdFileName(path)};
        invoke(cmd);
    }

    public void invoke(String[] cmd) throws IPCException {
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
            if (proc.exitValue() != 0) {
                errorHandler.error("gcc failed (assemble); status " +
                              proc.exitValue());
                throw new IPCException("compile error");
            }
        }
        catch (InterruptedException ex) {
            errorHandler.error("gcc interrupted: " + ex.getMessage());
            throw new IPCException("compile error");
        }
        catch (IOException ex) {
            errorHandler.error(ex.getMessage());
            throw new IPCException("compile error");
        }
    }

    protected void writeFile(String path, String str)
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

    protected String asmFileName(String orgPath) {
        return baseName(orgPath, true) + ".s";
    }

    protected String cmdFileName(String orgPath) {
        return baseName(orgPath, true);
    }

    protected String baseName(String path) {
        return new File(path).getName();
    }

    protected String baseName(String path, boolean stripExt) {
        if (stripExt) {
            return new File(path).getName().replaceFirst("\\.[^.]*$", "");
        }
        else {
            return baseName(path);
        }
    }
}
