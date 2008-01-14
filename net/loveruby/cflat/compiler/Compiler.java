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
    protected boolean debugParser;
    protected TypeTable typeTable;
    protected LibraryLoader loader;
    protected ErrorHandler errorHandler;

    public Compiler() {
        debugParser = false;
        typeTable = TypeTable.ilp32();
        loader = new LibraryLoader();
        errorHandler = new ErrorHandler(programId);
    }

    public Compiler(TypeTable table, LibraryLoader ld, ErrorHandler h) {
        debugParser = false;
        typeTable = table;
        loader = ld;
        errorHandler = h;
    }

    public void commandMain(String[] origArgs) {
        // parse options
        String mode = null;
        List args = listFromArray(origArgs);
        ListIterator it = args.listIterator();
        while (it.hasNext()) {
            String arg = (String)it.next();
            if (arg.startsWith("-")) {
                if (arg.startsWith("-I")) {
                    String path = arg.substring(2);
                    if (path.length() == 0) {
                        if (! it.hasNext()) {
                            errorExit("-I option missing argument");
                        }
                        it.remove();
                        path = (String)it.next();
                    }
                    loader.addLoadPath(path);
                }
                else if (arg.equals("--check-syntax")
                        || arg.equals("--dump-tokens")
                        || arg.equals("--dump-ast")
                        || arg.equals("--dump-reference")
                        || arg.equals("--dump-semantic")) {
                    if (mode != null) {
                        errorExit(mode + " option and " +
                                  arg + " option is exclusive");
                    }
                    mode = arg;
                }
                else if (arg.equals("--debug-parser")) {
                    debugParser = true;
                }
                else if (arg.equals("--help")) {
                    printUsage(System.out);
                    System.exit(0);
                }
                else {
                    System.err.println("unknown option: " + arg);
                    printUsage(System.err);
                    System.exit(1);
                }
                it.remove();
            }
        }
        if (mode == null) {
            mode = "compile";
        }
        if (args.size() == 0) errorExit("no input file");
        if (args.size() > 1) errorExit("too many input files");
        String inputFile = (String)args.get(0);

        // execute
        try {
            if (mode.equals("compile")) {
                compileFile(inputFile);
            }
            else if (mode.equals("--check-syntax")) {
                if (isValidSyntax(inputFile)) {
                    System.out.println("Syntax OK");
                    System.exit(0);
                } else {
                    System.exit(1);
                }
            }
            else if (mode.equals("--dump-tokens")) {
                dumpTokensFromFile(inputFile);
            }
            else if (mode.equals("--dump-ast")) {
                dumpASTFromFile(inputFile);
            }
            else if (mode.equals("--dump-reference")) {
                dumpReferenceFromFile(inputFile);
            }
            else if (mode.equals("--dump-semantic")) {
                dumpSemanticFromFile(inputFile);
            }
            else {
                throw new Error("unknown mode: " + mode);
            }
        }
        catch (CompileException ex) {
            errorHandler.error(ex.getMessage());
            System.exit(1);
        }
    }

    protected void printUsage(PrintStream out) {
        // --dump-reference is hidden option
        out.println("Usage: cbc [option] file");
        out.println("  --check-syntax   Syntax check only.");
        out.println("  --debug-parser   Dump parsing process.");
        out.println("  --dump-tokens    Parses source file and dumps tokens.");
        out.println("  --dump-ast       Parses source file and dumps AST.");
        out.println("  --dump-semantic  Check semantics and dumps AST.");
        out.println("  --help           Prints this message and quit.");
    }

    private List listFromArray(Object[] a) {
        List list = new ArrayList();
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
        return list;
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
        catch (SyntaxException ex) {
            return false;
        }
        catch (FileException ex) {
            errorHandler.error(ex.getMessage());
            return false;
        }
    }

    public AST parseFile(String path) throws SyntaxException, FileException {
        return Parser.parseFile(new File(path), loader,
                                errorHandler, debugParser);
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

    public void dumpReferenceFromFile(String path) throws CompileException {
        AST ast = parseFile(path);
        TypeTable typeTable = defaultTypeTable();
        JumpResolver.resolve(ast, errorHandler);
        LocalReferenceResolver.resolve(ast, errorHandler);
        TypeResolver.resolve(ast, typeTable, errorHandler);
        typeTable.semanticCheck(errorHandler);
        DereferenceChecker.check(ast, errorHandler);
        ast.dump();
    }

    public void dumpSemanticFromFile(String path) throws CompileException {
        AST ast = parseFile(path);
        TypeTable typeTable = defaultTypeTable();
        semanticAnalysis(ast, typeTable);
        ast.dump();
    }

    public void compileFile(String path) throws CompileException {
        AST ast = parseFile(path);
        TypeTable typeTable = defaultTypeTable();
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
        DereferenceChecker.check(ast, errorHandler);
        TypeChecker.check(ast, typeTable, errorHandler);
    }

    private TypeTable defaultTypeTable() {
        return TypeTable.ilp32();
    }

    public void assemble(String path) throws IPCException {
        String[] cmd = {"gcc", path, "-o", cmdFileName(path)};
        invoke(cmd);
    }

    public void invoke(String[] cmd) throws IPCException {
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
            passThrough(proc.getInputStream());
            passThrough(proc.getErrorStream());
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

    protected void passThrough(InputStream s) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(s));
        String line;
        while ((line = r.readLine()) != null) {
            System.err.println(line);
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
