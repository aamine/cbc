package net.loveruby.cflat.compiler;
import net.loveruby.cflat.parser.*;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.utils.*;
import net.loveruby.cflat.exception.*;
import java.util.*;
import java.io.*;

public class Compiler {
    static final private String programId = "cbc";

    static public void main(String[] args) {
        new Compiler(programId).commandMain(args);
    }

    protected ErrorHandler errorHandler;

    public Compiler(String programName) {
        this.errorHandler = new ErrorHandler(programName);
    }

    private TypeTable defaultTypeTable() {
        return TypeTable.ilp32();
    }

    public void commandMain(String[] origArgs) {
        Options opts = parseOptions(listFromArray(origArgs));
        if (opts.isMode("--check-syntax")) {
            if (isValidSyntax(opts)) {
                System.out.println("Syntax OK");
                System.exit(0);
            }
            else {
                System.exit(1);
            }
        }
        else {
            try {
                compileFile(opts);
            }
            catch (CompileException ex) {
                errorHandler.error(ex.getMessage());
                System.exit(1);
            }
        }
    }

    class Options {
        public String mode;
        public String inputFile;
        //public String outputFile;
        public boolean verbose;
        public boolean debugParser;
        //public boolean debugBuild;
        public TypeTable typeTable;
        public LibraryLoader loader;
        //public List ldArgs;   // List<LdArg>

        public boolean isMode(String m) {
            return mode == null ? false : mode.equals(m);
        }
    }

    protected Options parseOptions(List args) {
        ListIterator it = args.listIterator();
        Options opts = new Options();
        opts.typeTable = defaultTypeTable();
        opts.loader = new LibraryLoader();
        while (it.hasNext()) {
            String arg = (String)it.next();
            if (arg.startsWith("-")) {
                if (arg.equals("--check-syntax")
                        || arg.equals("--dump-tokens")
                        || arg.equals("--dump-ast")
                        || arg.equals("--dump-reference")
                        || arg.equals("--dump-semantic")
                        || arg.equals("-S")
                        || arg.equals("-c")) {
                    if (opts.mode != null) {
                        errorExit(opts.mode + " option and "
                                  + arg + " option is exclusive");
                    }
                    opts.mode = arg;
                }
                else if (arg.startsWith("-I")) {
                    opts.loader.addLoadPath(getOptArg(arg, it));
                }
                else if (arg.equals("--debug-parser")) {
                    opts.debugParser = true;
                }
                // FIXME: generic options
                //else if (arg.startsWith("-o"))
                //else if (arg.equals("--type-model"))
                // FIXME: compile options
                //else if (arg.equals("-g"))
                //else if (arg.equals("-fPIC"))
                //else if (arg.startsWith("-O"))
                // FIXME: assemble options
                //else if (arg.startsWith("-Wa,"))
                //else if (arg.equals("-Xassembler"))
                // FIXME: link options
                //else if (arg.equals("-static"))
                //else if (arg.equals("-shared"))
                //else if (arg.startsWith("-L"))
                //else if (arg.startsWith("-l"))
                //else if (arg.startsWith("-Wl,"))
                //else if (arg.equals("-Xlinker"))
                else if (arg.equals("-v")) {
                    opts.verbose = true;
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

        // FIXME: handle many input files
        if (args.size() == 0) errorExit("no input file");
        if (args.size() > 1) errorExit("too many input files");
        opts.inputFile = (String)args.get(0);

        return opts;
    }

    protected void printUsage(PrintStream out) {
        // --dump-reference is hidden option
        out.println("Usage: cbc [option] file");
        out.println("  --check-syntax   Syntax check only.");
        out.println("  --debug-parser   Dump parsing process.");
        out.println("  --dump-tokens    Parses source file and dumps tokens.");
        out.println("  --dump-ast       Parses source file and dumps AST.");
        out.println("  --dump-semantic  Check semantics and dumps AST.");
        out.println("  -S               Generate assembly source only.");
        out.println("  -c               Compile, Assemble and stop.");
        out.println("  -I PATH          Add import file loading path.");
        out.println("  --help           Prints this message and quit.");
    }

    private List listFromArray(Object[] a) {
        List list = new ArrayList();
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
        return list;
    }

    private String getOptArg(String arg, ListIterator it) {
        String path = arg.substring(2);
        if (path.length() != 0) {       // -Ipath
            return path;
        }
        else {                          // -I path
            if (! it.hasNext()) {
                errorExit("-I option missing argument");
            }
            it.remove();
            return (String)it.next();
        }
    }

    private void errorExit(String msg) {
        errorHandler.error(msg);
        System.exit(1);
    }

    protected boolean isValidSyntax(Options opts) {
        try {
            parseFile(opts);
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

    protected void compileFile(Options opts) throws CompileException {
        AST ast = parseFile(opts);
        if (opts.isMode("--dump-tokens")) {
            dumpTokenList(ast.firstToken(), System.out);
            return;
        }
        if (opts.isMode("--dump-ast")) {
            ast.dump();
            return;
        }
        semanticAnalysis(ast, opts);
        if (opts.isMode("--dump-semantic")) {
            ast.dump();
            return;
        }
        String asm = CodeGenerator.generate(ast, opts.typeTable, errorHandler);
        writeFile(asmFileName(opts.inputFile), asm);
        if (opts.isMode("-S")) {
            return;
        }
        assemble(asmFileName(opts.inputFile),
                 objFileName(opts.inputFile),
                 opts);
        if (opts.isMode("-c")) {
            return;
        }
        link(objFileName(opts.inputFile),
             exeFileName(opts.inputFile),
             opts);
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

    protected AST parseFile(Options opts)
                            throws SyntaxException, FileException {
        return Parser.parseFile(new File(opts.inputFile),
                                opts.loader,
                                errorHandler,
                                opts.debugParser);
    }

    protected void semanticAnalysis(AST ast, Options opts)
                                        throws SemanticException {
        JumpResolver.resolve(ast, errorHandler);
        LocalReferenceResolver.resolve(ast, errorHandler);
        TypeResolver.resolve(ast, opts.typeTable, errorHandler);
        opts.typeTable.semanticCheck(errorHandler);
        DereferenceChecker.check(ast, errorHandler);
        if (opts.isMode("--dump-reference")) {
            ast.dump();
            System.exit(1);
        }
        TypeChecker.check(ast, opts.typeTable, errorHandler);
    }

    protected void assemble(String srcPath,
                            String destPath,
                            Options opts) throws IPCException {
        String[] cmd = {
            "as",
            "-o", destPath,
            srcPath
        };
        invoke(cmd, opts.verbose);
    }

    protected void link(String srcPath,
                        String destPath,
                        Options opts) throws IPCException {
        String[] cmd = {
            "ld",
            "-dynamic-linker", "/lib/ld-linux.so.2",
            "/usr/lib/crt1.o",
            "/usr/lib/crti.o",
            srcPath,
            "/usr/lib/libc_nonshared.a",
            "-lc",
            "/usr/lib/crtn.o",
            "-o", destPath
        };
        invoke(cmd, opts.verbose);
    }

    public void invoke(String[] cmd, boolean debug) throws IPCException {
        if (debug) {
            dumpCommand(cmd);
        }
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
            passThrough(proc.getInputStream());
            passThrough(proc.getErrorStream());
            if (proc.exitValue() != 0) {
                errorHandler.error(cmd[0] + " failed."
                                   + " (status " + proc.exitValue() + ")");
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

    protected void dumpCommand(String[] cmd) {
        String sep = "";
        for (int i = 0; i < cmd.length; i++) {
            System.out.print(sep); sep = " ";
            System.out.print(cmd[i]);
        }
        System.out.println("");
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

    protected String objFileName(String orgPath) {
        return baseName(orgPath, true) + ".o";
    }

    protected String exeFileName(String orgPath) {
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
