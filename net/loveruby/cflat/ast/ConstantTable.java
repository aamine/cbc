package net.loveruby.cflat.ast;
import java.util.*;

public class ConstantTable {
    protected Map table;
    protected long id;

    public ConstantTable() {
        table = new LinkedHashMap();
        id = 0;
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public ConstantEntry intern(String s) {
        ConstantEntry ent = (ConstantEntry)table.get(s);
        if (ent == null) {
            ent = new ConstantEntry(id++, s);
            table.put(s, ent);
        }
        return ent;
    }

    public Iterator entries() {
        return table.values().iterator();
    }
}
