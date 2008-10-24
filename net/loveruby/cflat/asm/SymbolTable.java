package net.loveruby.cflat.asm;
import java.util.*;

public class SymbolTable {
    protected String base;
    protected Map<UnnamedSymbol, String> map;
    protected long seq = 0;

    static private final String DUMMY_SYMBOL_BASE = "L";
    static private final SymbolTable dummy = new SymbolTable(DUMMY_SYMBOL_BASE);

    static public SymbolTable dummy() {
        return dummy;
    }

    public SymbolTable(String base) {
        this.base = base;
        this.map = new HashMap<UnnamedSymbol, String>();
    }

    public Symbol newSymbol() {
        return new NamedSymbol(newString());
    }

    public String symbolString(UnnamedSymbol sym) {
        String str = map.get(sym);
        if (str != null) {
            return str;
        }
        else {
            String newStr = newString();
            map.put(sym, newStr);
            return newStr;
        }
    }

    protected String newString() {
        return base + seq++;
    }
}
