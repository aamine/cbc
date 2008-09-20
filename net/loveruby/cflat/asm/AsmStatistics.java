package net.loveruby.cflat.asm;
import java.util.*;

public class AsmStatistics {
    protected Map registerUsage;
    protected Map insnUsage;

    static public AsmStatistics collect(List assemblies) {
        AsmStatistics stats = new AsmStatistics();
        Iterator asms = assemblies.iterator();
        while (asms.hasNext()) {
            Assembly asm = (Assembly)asms.next();
            asm.collectStatistics(stats);
        }
        return stats;
    }

    public AsmStatistics() {
        registerUsage = new HashMap();
        insnUsage = new HashMap();
    }

    public int numRegisterUsage(String name) {
        return fetchCount(registerUsage, name);
    }

    public void registerUsed(String name) {
        incrementCount(registerUsage, name);
    }

    public int numInstructionUsage(String insn) {
        return fetchCount(insnUsage, insn);
    }

    public void instructionUsed(String insn) {
        incrementCount(insnUsage, insn);
    }

    protected int fetchCount(Map m, String name) {
        Integer n = (Integer)m.get(name);
        if (n == null) {
            return 0;
        }
        else {
            return n.intValue();
        }
    }

    protected void incrementCount(Map m, String name) {
        m.put(name, new Integer(fetchCount(m, name) + 1));
    }
}
