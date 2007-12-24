package net.loveruby.cflat.compiler;
import net.loveruby.cflat.parser.*;
import net.loveruby.cflat.ast.Declarations;
import net.loveruby.cflat.exception.*;
import java.util.*;
import java.io.*;

public class LibraryLoader {
    protected List loadPath;
    protected Map loadedLibrary;

    public LibraryLoader() {
        loadPath = new ArrayList();
        loadPath.add(".");
        loadedLibrary = new HashMap();
    }

    public LibraryLoader(List loadPath) {
        this.loadPath = loadPath;
        loadedLibrary = new HashMap();
    }

    public Declarations loadLibrary(String libid, ErrorHandler handler)
            throws CompileException {
        if (loadedLibrary.containsKey(libid)) {
            return null;
        }
        Declarations decls =
            Parser.parseDeclFile(searchLibrary(libid), this, handler);
        loadedLibrary.put(libid, decls);
        return decls;
    }

    public File searchLibrary(String libid) throws FileException {
        try {
            Iterator pathes = loadPath.iterator();
            while (pathes.hasNext()) {
                String path = (String)pathes.next();
                File file = new File(path + "/" + libid + ".hb");
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
}
