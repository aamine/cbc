package net.loveruby.cflat.parser;
import net.loveruby.cflat.ast.Declarations;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;
import java.io.*;

public class LibraryLoader {
    protected List<String> loadPath;
    protected LinkedList<String> loadingLibraries;
    protected Map<String, Declarations> loadedLibraries;

    static public List<String> defaultLoadPath() {
        List<String> pathes = new ArrayList<String>();
        pathes.add(".");
        return pathes;
    }

    public LibraryLoader() {
        this(defaultLoadPath());
    }

    public LibraryLoader(List<String> loadPath) {
        this.loadPath = loadPath;
        this.loadingLibraries = new LinkedList<String>();
        this.loadedLibraries = new HashMap<String, Declarations>();
    }

    public void addLoadPath(String path) {
        loadPath.add(path);
    }

    public Declarations loadLibrary(String libid, ErrorHandler handler)
            throws CompileException {
        if (loadingLibraries.contains(libid)) {
            throw new SemanticException("recursive import from "
                                        + loadingLibraries.getLast()
                                        + ": " + libid);
        }
        loadingLibraries.addLast(libid);   // stop recursive import
        Declarations decls = loadedLibraries.get(libid);
        if (decls != null) {
            // Already loaded import file.  Returns cached declarations.
            return decls;
        }
        decls = Parser.parseDeclFile(searchLibrary(libid), this, handler);
        loadedLibraries.put(libid, decls);
        loadingLibraries.removeLast();
        return decls;
    }

    public File searchLibrary(String libid) throws FileException {
        try {
            for (String path : loadPath) {
                File file = new File(path + "/" + libPath(libid) + ".hb");
                if (file.exists()) {
                    return file;
                }
            }
            throw new FileException(
                "no such library header file: " + libid);
        }
        catch (SecurityException ex) {
            throw new FileException(ex.getMessage());
        }
    }

    protected String libPath(String id) {
        return id.replace('.', '/');
    }
}
