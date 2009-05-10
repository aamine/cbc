package net.loveruby.cflat.compiler;
import net.loveruby.cflat.utils.CommandArg;
import java.io.File;

// package private
class SourceFile implements CommandArg {
    private String originalName;
    private String currentName;

    public SourceFile(String name) {
        this.originalName = name;
        this.currentName = name;
    }

    public String name() {
        return originalName();
    }

    public String originalName() {
        return this.originalName;
    }

    public String currentName() {
        return this.currentName;
    }

    public void setCurrentName(String name) {
        this.currentName = name;
    }

    public boolean isCflatSource() {
        return extName(currentName).equals(".cb");
    }

    public boolean isAssemblySource() {
        return extName(currentName).equals(".s");
    }

    public boolean isObjectFile() {
        return extName(currentName).equals(".o");
    }

    public boolean isSharedLibrary() {
        return extName(currentName).equals(".so");
    }

    public boolean isStaticLibrary() {
        return extName(currentName).equals(".a");
    }

    public boolean isExecutable() {
        return extName(currentName).equals("");
    }

    public String asmFileName(Options opts) {
        return or(opts.outputFileNameFor(CompilerMode.Compile), replaceExt(".s"));
    }

    public String objFileName(Options opts) {
        return or(opts.outputFileNameFor(CompilerMode.Assemble), replaceExt(".o"));
    }

    public String linkedFileName(Options opts, String newExt) {
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

    public boolean isSourceFile() {
        return true;
    }

    public String toString() {
        return currentName;
    }
}
