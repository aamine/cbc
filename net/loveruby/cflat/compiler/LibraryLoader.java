package net.loveruby.cflat.compiler;
import net.loveruby.cflat.parser.*;
import net.loveruby.cflat.ast.Declarations;
import net.loveruby.cflat.exception.*;
import java.util.*;
import java.io.*;

public class LibraryLoader {
    protected List loadPath;
    protected LinkedList loadingLibraries;      // LinkedList<String>
    protected Map loadedLibraries;              // Map<String, Boolean>

    static public List defaultLoadPath() {
        List pathes = new ArrayList();
        pathes.add(".");
        return pathes;
    }

    public LibraryLoader() {
        this(defaultLoadPath());
    }

    public LibraryLoader(List loadPath) {
        this.loadPath = loadPath;
        this.loadingLibraries = new LinkedList();
        this.loadedLibraries = new HashMap();
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
        Declarations decls = (Declarations)loadedLibraries.get(libid);
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
            Iterator pathes = loadPath.iterator();
            while (pathes.hasNext()) {
                String path = (String)pathes.next();
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
