package net.loveruby.cflat.compiler;
import net.loveruby.cflat.sysdep.x86.X86Linux;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.exception.*;
import java.util.*;
import java.io.*;

// package scope
class Options {
    CompilerMode mode;
    LibraryLoader loader = new LibraryLoader();
    Platform platform = new X86Linux();
    String outputFileName;
    boolean verbose = false;
    boolean debugParser = false;
    CodeGeneratorOptions genOptions = new CodeGeneratorOptions();
    List<String> asOptions = new ArrayList<String>();
    boolean generatingSharedLibrary = false;
    boolean generatingPIE = false;
    List<LdArg> ldArgs = new ArrayList<LdArg>();
    boolean noStartFiles = false;
    boolean noDefaultLibs = false;

    CompilerMode mode() {
        return mode;
    }

    boolean isAssembleRequired() {
        return mode.requires(CompilerMode.Assemble);
    }

    boolean isLinkRequired() {
        return mode.requires(CompilerMode.Link);
    }

    String outputFileNameFor(CompilerMode mode) {
        return this.mode == mode ? outputFileName : null;
    }

    String exeFileName() {
        return getOutputFileName("");
    }

    String soFileName() {
        return getOutputFileName(".so");
    }

    private String getOutputFileName(String newExt) {
        if (outputFileName != null) {
            return outputFileName;
        }
        List<SourceFile> srcs = sourceFiles();
        if (srcs.size() == 1) {
            return srcs.get(0).linkedFileName(this, newExt);
        }
        else {
            return "a.out";
        }
    }

    private List<SourceFile> sourceFiles() {
        List<SourceFile> result = new ArrayList<SourceFile>();
        for (LdArg arg : ldArgs) {
            if (arg.isSourceFile()) {
                result.add((SourceFile)arg);
            }
        }
        return result;
    }

    Platform platform() {
        return platform;
    }

    TypeTable typeTable() {
        return platform.typeTable();
    }

    CodeGenerator codeGenerator(ErrorHandler errorHandler) {
        return platform.codeGenerator(genOptions, errorHandler);
    }

    LibraryLoader loader() {
        return this.loader;
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

    List<String> asOptions() {
        return this.asOptions;
    }

    boolean isGeneratingSharedLibrary() {
        return this.generatingSharedLibrary;
    }

    boolean isGeneratingPIE() {
        return this.generatingPIE;
    }

    List<LdArg> ldArgs() {
        return this.ldArgs;
    }

    boolean noStartFiles() {
        return this.noStartFiles;
    }

    boolean noDefaultLibs() {
        return this.noDefaultLibs;
    }

    /** Returns List<SourceFile>. */
    List<SourceFile> parse(List<String> argsList) {
        List<SourceFile> srcs = new ArrayList<SourceFile>();
        ListIterator<String> args = argsList.listIterator();
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
                else if (arg.equals("--verbose-asm")) {
                    genOptions.generateVerboseAsm();
                }
                else if (arg.startsWith("-Wa,")) {
                    asOptions.addAll(parseCommaSeparatedOptions(arg));
                }
                else if (arg.equals("-Xassembler")) {
                    asOptions.add(nextArg(arg, args));
                }
                else if (arg.equals("-static")) {
                    ldArgs.add(new LdOption(arg));
                }
                else if (arg.equals("-shared")) {
                    generatingSharedLibrary = true;
                }
                else if (arg.equals("-pie")) {
                    generatingPIE = true;
                }
                else if (arg.equals("--readonly-plt")) {
                    ldArgs.add(new LdOption("-z"));
                    ldArgs.add(new LdOption("combreloc"));
                    ldArgs.add(new LdOption("-z"));
                    ldArgs.add(new LdOption("now"));
                    ldArgs.add(new LdOption("-z"));
                    ldArgs.add(new LdOption("relro"));
                }
                else if (arg.startsWith("-L")) {
                    ldArgs.add(new LdOption("-L" + getOptArg(arg, args)));
                }
                else if (arg.startsWith("-l")) {
                    ldArgs.add(new LdOption("-l" + getOptArg(arg, args)));
                }
                else if (arg.equals("-nostartfiles")) {
                    noStartFiles = true;
                }
                else if (arg.equals("-nodefaultlibs")) {
                    noDefaultLibs = true;
                }
                else if (arg.equals("-nostdlib")) {
                    noStartFiles = true;
                    noDefaultLibs = true;
                }
                else if (arg.startsWith("-Wl,")) {
                    for (String opt : parseCommaSeparatedOptions(arg)) {
                        ldArgs.add(new LdOption(opt));
                    }
                }
                else if (arg.equals("-Xlinker")) {
                    ldArgs.add(new LdOption(nextArg(arg, args)));
                }
                else if (arg.equals("-v")) {
                    verbose = true;
                }
                else if (arg.equals("--version")) {
                    System.out.printf("%s version %s",
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
                // source file
                addSourceFile(srcs, ldArgs, arg);
            }
        }
        // args has more arguments when "--" is appeared.
        while (args.hasNext()) {
            addSourceFile(srcs, ldArgs, args.next());
        }
        if (srcs.isEmpty()) parseError("no input file");
        if (mode == null) {
            mode = CompilerMode.Link;
        }
        if (! isLinkRequired() && outputFileName != null && srcs.size() > 1) {
            parseError("-o option requires only 1 input not on linking");
        }
        return srcs;
    }

    private void parseError(String msg) {
        throw new OptionParseError(msg);
    }

    private void addSourceFile(List<SourceFile> srcs, List<LdArg> ldArgs, String sourceName) {
        SourceFile src = new SourceFile(sourceName);
        srcs.add(src);
        // Original argument order does matter when linking.
        ldArgs.add(src);
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
        List<String> opts = Arrays.asList(opt.split(","));
        opts.remove(0);  // remove "-Wl" etc.
        if (opts.isEmpty()) {
            parseError("missing argument for " + opt);
        }
        return opts;
    }

    void printUsage(PrintStream out) {
        out.println("Usage: cbc [options] file...");
        out.println("Global Options:");
        out.println("  --check-syntax   Checks syntax and quit.");
        out.println("  --dump-tokens    Dumps tokens and quit.");
        // --dump-stmt is a hidden option.
        // --dump-expr is a hidden option.
        out.println("  --dump-ast       Dumps AST and quit.");
        out.println("  --dump-semantic  Dumps AST after semantic check and quit.");
        // --dump-reference is a hidden option.
        out.println("  --dump-ir        Dumps IR and quit.");
        out.println("  --dump-asm       Prints an assembly source and quit.");
        out.println("  -S               Generates an assembly file and quit.");
        out.println("  -c               Generates an object file and quit.");
        out.println("  -o PATH          Places output in file PATH.");
        out.println("  -v               Turn on verbose mode.");
        out.println("  --version        Shows compiler version and quit.");
        out.println("  --help           Prints this message and quit.");
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
        out.println("  --verbose-asm    Generate assembly with verbose comments.");
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
        out.println("  --readonly-plt   Generates read-only PLT.");
        out.println("  -nostartfiles    Do not link startup files.");
        out.println("  -nodefaultlibs   Do not link default libraries.");
        out.println("  -nostdlib        Enables -nostartfiles and -nodefaultlibs.");
        out.println("  -Wl,OPT          Passes OPT to the linker (ld).");
        out.println("  -Xlinker OPT     Passes OPT to the linker (ld).");
    }
}
