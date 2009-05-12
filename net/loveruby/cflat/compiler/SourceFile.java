package net.loveruby.cflat.compiler;
import java.io.File;

class SourceFile implements LdArg {
    private String originalName;
    private String currentName;

    SourceFile(String name) {
        this.originalName = name;
        this.currentName = name;
    }

    public boolean isSourceFile() {
        return true;
    }

    public String toString() {
        return currentName;
    }

    String name() {
        return originalName();
    }

    String originalName() {
        return this.originalName;
    }

    String currentName() {
        return this.currentName;
    }

    void setCurrentName(String name) {
        this.currentName = name;
    }

    boolean isCflatSource() {
        return extName(currentName).equals(".cb");
    }

    boolean isAssemblySource() {
        return extName(currentName).equals(".s");
    }

    boolean isObjectFile() {
        return extName(currentName).equals(".o");
    }

    boolean isSharedLibrary() {
        return extName(currentName).equals(".so");
    }

    boolean isStaticLibrary() {
        return extName(currentName).equals(".a");
    }

    boolean isExecutable() {
        return extName(currentName).equals("");
    }

    String asmFileName(Options opts) {
        return or(opts.outputFileNameFor(CompilerMode.Compile), replaceExt(".s"));
    }

    String objFileName(Options opts) {
        return or(opts.outputFileNameFor(CompilerMode.Assemble), replaceExt(".o"));
    }

    String linkedFileName(Options opts, String newExt) {
        return or(opts.outputFileName, replaceExt(newExt));
    }

    private String or(String x, String y) {
        return x != null ? x : y;
    }

    private String replaceExt(String ext) {
        return baseName(originalName, true) + ext;
    }

    private String baseName(String path) {
        return new File(path).getName();
    }

    private String baseName(String path, boolean stripExt) {
        if (stripExt) {
            return new File(path).getName().replaceFirst("\\.[^.]*$", "");
        }
        else {
            return baseName(path);
        }
    }

    private String extName(String path) {
        int idx = path.lastIndexOf(".");
        if (idx < 0) return "";
        return path.substring(idx);
    }
}
