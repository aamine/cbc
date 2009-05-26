package net.loveruby.cflat.compiler;

import java.util.Map;
import java.util.HashMap;

enum CompilerMode {
    CheckSyntax ("--check-syntax"),
    DumpTokens ("--dump-tokens"),
    DumpAST ("--dump-ast"),
    DumpStmt ("--dump-stmt"),
    DumpExpr ("--dump-expr"),
    DumpSemantic ("--dump-semantic"),
    DumpReference ("--dump-reference"),
    DumpIR ("--dump-ir"),
    DumpAsm ("--dump-asm"),
    PrintAsm ("--print-asm"),
    Compile ("-S"),
    Assemble ("-c"),
    Link ("--link");

    static private Map<String, CompilerMode> modes;
    static {
        modes = new HashMap<String, CompilerMode>();
        modes.put("--check-syntax", CheckSyntax);
        modes.put("--dump-tokens", DumpTokens);
        modes.put("--dump-ast", DumpAST);
        modes.put("--dump-stmt", DumpStmt);
        modes.put("--dump-expr", DumpExpr);
        modes.put("--dump-semantic", DumpSemantic);
        modes.put("--dump-reference", DumpReference);
        modes.put("--dump-ir", DumpIR);
        modes.put("--dump-asm", DumpAsm);
        modes.put("--print-asm", PrintAsm);
        modes.put("-S", Compile);
        modes.put("-c", Assemble);
    }

    static public boolean isModeOption(String opt) {
        return modes.containsKey(opt);
    }

    static public CompilerMode fromOption(String opt) {
        CompilerMode m = modes.get(opt);
        if (m == null) {
            throw new Error("must not happen: unknown mode option: " + opt);
        }
        return m;
    }

    private final String option;

    CompilerMode(String option) {
        this.option = option;
    }

    public String toOption() {
        return option;
    }

    boolean requires(CompilerMode m) {
        return ordinal() >= m.ordinal();
    }
}
