package net.loveruby.cflat.compiler;
import net.loveruby.cflat.parser.LibraryLoader;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.sysdep.*;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.PrintStream;

class Options {
    static Options parse(String[] args) {
        Options opts = new Options();
        opts.parseArgs(args);
        return opts;
    }

    private CompilerMode mode;
    private Platform platform = new X86Linux();
    private String outputFileName;
    private boolean verbose = false;
    private LibraryLoader loader = new LibraryLoader();
    private boolean debugParser = false;
    private CodeGeneratorOptions genOptions = new CodeGeneratorOptions();
    private AssemblerOptions asOptions = new AssemblerOptions();
    private LinkerOptions ldOptions = new LinkerOptions();
    private List<LdArg> ldArgs;
    private List<SourceFile> sourceFiles;

    CompilerMode mode() {
        return mode;
    }

    boolean isAssembleRequired() {
        return mode.requires(CompilerMode.Assemble);
    }

    boolean isLinkRequired() {
        return mode.requires(CompilerMode.Link);
    }

    List<SourceFile> sourceFiles() {
        return sourceFiles;
    }

    String asmFileNameOf(SourceFile src) {
        if (outputFileName != null && mode == CompilerMode.Compile) {
            return outputFileName;
        }
        return src.asmFileName();
    }

    String objFileNameOf(SourceFile src) {
        if (outputFileName != null && mode == CompilerMode.Assemble) {
            return outputFileName;
        }
        return src.objFileName();
    }

    String exeFileName() {
        return linkedFileName("");
    }

    String soFileName() {
        return linkedFileName(".so");
    }

    static private final String DEFAULT_LINKER_OUTPUT = "a.out";

    private String linkedFileName(String newExt) {
        if (outputFileName != null) {
            return outputFileName;
        }
        if (sourceFiles.size() == 1) {
            return sourceFiles.get(0).linkedFileName(newExt);
        }
        else {
            return DEFAULT_LINKER_OUTPUT;
        }
    }

    String outputFileName() {
        return this.outputFileName;
    }

    boolean isVerboseMode() {
        return this.verbose;
    }

    boolean doesDebugParser() {
        return this.debugParser;
    }

    LibraryLoader loader() {
        return this.loader;
    }

    TypeTable typeTable() {
        return platform.typeTable();
    }

    CodeGenerator codeGenerator(ErrorHandler h) {
        return platform.codeGenerator(genOptions, h);
    }

    Assembler assembler(ErrorHandler h) {
        return platform.assembler(h);
    }

    AssemblerOptions asOptions() {
        return asOptions;
    }

    Linker linker(ErrorHandler h) {
        return platform.linker(h);
    }

    LinkerOptions ldOptions() {
        return ldOptions;
    }

    List<String> ldArgs() {
        List<String> result = new ArrayList<String>();
        for (LdArg arg : ldArgs) {
            result.add(arg.toString());
        }
        return result;
    }

    boolean isGeneratingSharedLibrary() {
        return ldOptions.generatingSharedLibrary;
    }

    void parseArgs(String[] origArgs) {
        sourceFiles = new ArrayList<SourceFile>();
        ldArgs = new ArrayList<LdArg>();
        ListIterator<String> args = Arrays.asList(origArgs).listIterator();
        while (args.hasNext()) {
            String arg = args.next();
            if (arg.equals("--")) {
                // "--" Stops command line processing
                break;
            }
            else if (arg.startsWith("-")) {
                if (CompilerMode.isModeOption(arg)) {
                    if (mode != null) {
                        parseError(mode.toOption() + " option and "
                                   + arg + " option is exclusive");
                    }
                    mode = CompilerMode.fromOption(arg);
                }
                else if (arg.startsWith("-I")) {
                    loader.addLoadPath(getOptArg(arg, args));
                }
                else if (arg.equals("--debug-parser")) {
                    debugParser = true;
                }
                else if (arg.startsWith("-o")) {
                    outputFileName = getOptArg(arg, args);
                }
                else if (arg.equals("-fpic")
                        || arg.equals("-fPIC")) {
                    genOptions.generatePIC();
                }
                else if (arg.equals("-fpie")
                        || arg.equals("-fPIE")) {
                    genOptions.generatePIE();
                }
                else if (arg.startsWith("-O")) {
                    String type = arg.substring(2);
                    if (! type.matches("^([0123s]|)$")) {
                        parseError("unknown optimization switch: " + arg);
                    }
                    genOptions.setOptimizationLevel(type.equals("0") ? 0 : 1);
                }
                else if (arg.equals("-fverbose-asm")
                        || arg.equals("--verbose-asm")) {
                    genOptions.generateVerboseAsm();
                }
                else if (arg.startsWith("-Wa,")) {
                    for (String a : parseCommaSeparatedOptions(arg)) {
                        asOptions.addArg(a);
                    }
                }
                else if (arg.equals("-Xassembler")) {
                    asOptions.addArg(nextArg(arg, args));
                }
                else if (arg.equals("-static")) {
                    addLdArg(arg);
                }
                else if (arg.equals("-shared")) {
                    ldOptions.generatingSharedLibrary = true;
                }
                else if (arg.equals("-pie")) {
                    ldOptions.generatingPIE = true;
                }
                else if (arg.equals("--readonly-got")) {
                    addLdArg("-z");
                    addLdArg("combreloc");
                    addLdArg("-z");
                    addLdArg("now");
                    addLdArg("-z");
                    addLdArg("relro");
                }
                else if (arg.startsWith("-L")) {
                    addLdArg("-L" + getOptArg(arg, args));
                }
                else if (arg.startsWith("-l")) {
                    addLdArg("-l" + getOptArg(arg, args));
                }
                else if (arg.equals("-nostartfiles")) {
                    ldOptions.noStartFiles = true;
                }
                else if (arg.equals("-nodefaultlibs")) {
                    ldOptions.noDefaultLibs = true;
                }
                else if (arg.equals("-nostdlib")) {
                    ldOptions.noStartFiles = true;
                    ldOptions.noDefaultLibs = true;
                }
                else if (arg.startsWith("-Wl,")) {
                    for (String opt : parseCommaSeparatedOptions(arg)) {
                        addLdArg(opt);
                    }
                }
                else if (arg.equals("-Xlinker")) {
                    addLdArg(nextArg(arg, args));
                }
                else if (arg.equals("-v")) {
                    verbose = true;
                    asOptions.verbose = true;
                    ldOptions.verbose = true;
                }
                else if (arg.equals("--version")) {
                    System.out.printf("%s version %s\n",
                        Compiler.ProgramName, Compiler.Version);
                    System.exit(0);
                }
                else if (arg.equals("--help")) {
                    printUsage(System.out);
                    System.exit(0);
                }
                else {
                    parseError("unknown option: " + arg);
                }
            }
            else {
                ldArgs.add(new SourceFile(arg));
            }
        }
        // args has more arguments when "--" is appeared.
        while (args.hasNext()) {
            ldArgs.add(new SourceFile(args.next()));
        }

        if (mode == null) {
            mode = CompilerMode.Link;
        }
        sourceFiles = selectSourceFiles(ldArgs);
        if (sourceFiles.isEmpty()) {
            parseError("no input file");
        }
        for (SourceFile src : sourceFiles) {
            if (! src.isKnownFileType()) {
                parseError("unknown file type: " + src.path());
            }
        }
        if (outputFileName != null
                && sourceFiles.size() > 1
                && ! isLinkRequired()) {
            parseError("-o option requires only 1 input (except linking)");
        }
    }

    private void parseError(String msg) {
        throw new OptionParseError(msg);
    }

    private void addLdArg(String arg) {
        ldArgs.add(new LdOption(arg));
    }

    private List<SourceFile> selectSourceFiles(List<LdArg> args) {
        List<SourceFile> result = new ArrayList<SourceFile>();
        for (LdArg arg : args) {
            if (arg.isSourceFile()) {
                result.add((SourceFile)arg);
            }
        }
        return result;
    }

    private String getOptArg(String opt, ListIterator<String> args) {
        String path = opt.substring(2);
        if (path.length() != 0) {       // -Ipath
            return path;
        }
        else {                          // -I path
            return nextArg(opt, args);
        }
    }

    private String nextArg(String opt, ListIterator<String> args) {
        if (! args.hasNext()) {
            parseError("missing argument for " + opt);
        }
        return args.next();
    }

    /** "-Wl,-rpath,/usr/local/lib" -> ["-rpath", "/usr/local/lib"] */
    private List<String> parseCommaSeparatedOptions(String opt) {
        String[] opts = opt.split(",");
        if (opts.length <= 1) {
            parseError("missing argument for " + opt);
        }
        List<String> result = new ArrayList<String>();
        // move items into result, except first item ("-Wl", etc).
        for (int i = 1; i < opts.length; i++) {
            result.add(opts[i]);
        }
        return result;
    }

    void printUsage(PrintStream out) {
        out.println("Usage: cbc [options] file...");
        out.println("Global Options:");
        out.println("  --check-syntax   Checks syntax and quit.");
        out.println("  --dump-tokens    Dumps tokens and quit.");
        // --dump-stmt is a hidden option.
        // --dump-expr is a hidden option.
        out.println("  --dump-ast       Dumps AST and quit.");
        out.println("  --dump-semantic  Dumps AST after semantic checks and quit.");
        // --dump-reference is a hidden option.
        out.println("  --dump-ir        Dumps IR and quit.");
        out.println("  --dump-asm       Dumps AssemblyCode and quit.");
        out.println("  --print-asm      Prints assembly code and quit.");
        out.println("  -S               Generates an assembly file and quit.");
        out.println("  -c               Generates an object file and quit.");
        out.println("  -o PATH          Places output in file PATH.");
        out.println("  -v               Turn on verbose mode.");
        out.println("  --version        Shows compiler version and quit.");
        out.println("  --help           Prints this message and quit.");
        out.println("");
        out.println("Optimization Options:");
        out.println("  -O               Enables optimization.");
        out.println("  -O1, -O2, -O3    Equivalent to -O.");
        out.println("  -Os              Equivalent to -O.");
        out.println("  -O0              Disables optimization (default).");
        out.println("");
        out.println("Parser Options:");
        out.println("  -I PATH          Adds PATH as import file directory.");
        out.println("  --debug-parser   Dumps parsing process.");
        out.println("");
        out.println("Code Generator Options:");
        out.println("  -O               Enables optimization.");
        out.println("  -O1, -O2, -O3    Equivalent to -O.");
        out.println("  -Os              Equivalent to -O.");
        out.println("  -O0              Disables optimization (default).");
        out.println("  -fPIC            Generates PIC assembly.");
        out.println("  -fpic            Equivalent to -fPIC.");
        out.println("  -fPIE            Generates PIE assembly.");
        out.println("  -fpie            Equivalent to -fPIE.");
        out.println("  -fverbose-asm    Generate assembly with verbose comments.");
        out.println("");
        out.println("Assembler Options:");
        out.println("  -Wa,OPT          Passes OPT to the assembler (as).");
        out.println("  -Xassembler OPT  Passes OPT to the assembler (as).");
        out.println("");
        out.println("Linker Options:");
        out.println("  -l LIB           Links the library LIB.");
        out.println("  -L PATH          Adds PATH as library directory.");
        out.println("  -shared          Generates shared library rather than executable.");
        out.println("  -static          Linkes only with static libraries.");
        out.println("  -pie             Generates PIE.");
        out.println("  --readonly-got   Generates read-only GOT (ld -z combreloc -z now -z relro).");
        out.println("  -nostartfiles    Do not link startup files.");
        out.println("  -nodefaultlibs   Do not link default libraries.");
        out.println("  -nostdlib        Enables -nostartfiles and -nodefaultlibs.");
        out.println("  -Wl,OPT          Passes OPT to the linker (ld).");
        out.println("  -Xlinker OPT     Passes OPT to the linker (ld).");
    }
}
