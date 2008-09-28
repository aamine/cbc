package net.loveruby.cflat.ast;
import java.util.*;

public class ConstantTable implements Iterable<ConstantEntry> {
    protected Map<String, ConstantEntry> table;
    protected long id;

    public ConstantTable() {
        table = new LinkedHashMap<String, ConstantEntry>();
        id = 0;
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public ConstantEntry intern(String s) {
        ConstantEntry ent = table.get(s);
        if (ent == null) {
            ent = new ConstantEntry(id++, s);
            table.put(s, ent);
        }
        return ent;
    }

    public Collection<ConstantEntry> entries() {
        return table.values();
    }

    public Iterator<ConstantEntry> iterator() {
        return table.values().iterator();
    }
}
