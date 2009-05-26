package net.loveruby.cflat.compiler;
import java.io.File;

class SourceFile implements LdArg {
    static final String EXT_CFLAT_SOURCE = ".cb";
    static final String EXT_ASSEMBLY_SOURCE = ".s";
    static final String EXT_OBJECT_FILE = ".o";
    static final String EXT_STATIC_LIBRARY = ".a";
    static final String EXT_SHARED_LIBRARY = ".so";
    static final String EXT_EXECUTABLE_FILE = "";

    static final String[] KNOWN_EXTENSIONS = {
      EXT_CFLAT_SOURCE,
      EXT_ASSEMBLY_SOURCE,
      EXT_OBJECT_FILE,
      EXT_STATIC_LIBRARY,
      EXT_SHARED_LIBRARY,
      EXT_EXECUTABLE_FILE
    };

    private final String originalName;
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

    String path() {
        return currentName;
    }

    String currentName() {
        return currentName;
    }

    void setCurrentName(String name) {
        this.currentName = name;
    }

    boolean isKnownFileType() {
        String ext = extName(originalName);
        for (String e : KNOWN_EXTENSIONS) {
            if (e.equals(ext)) return true;
        }
        return false;
    }

    boolean isCflatSource() {
        return extName(currentName).equals(EXT_CFLAT_SOURCE);
    }

    boolean isAssemblySource() {
        return extName(currentName).equals(EXT_ASSEMBLY_SOURCE);
    }

    boolean isObjectFile() {
        return extName(currentName).equals(EXT_OBJECT_FILE);
    }

    boolean isSharedLibrary() {
        return extName(currentName).equals(EXT_SHARED_LIBRARY);
    }

    boolean isStaticLibrary() {
        return extName(currentName).equals(EXT_STATIC_LIBRARY);
    }

    boolean isExecutable() {
        return extName(currentName).equals(EXT_EXECUTABLE_FILE);
    }

    String asmFileName() {
        return replaceExt(EXT_ASSEMBLY_SOURCE);
    }

    String objFileName() {
        return replaceExt(EXT_OBJECT_FILE);
    }

    String linkedFileName(String newExt) {
        return replaceExt(newExt);
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
