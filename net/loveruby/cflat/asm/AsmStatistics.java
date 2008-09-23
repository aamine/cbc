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

    public boolean doesRegisterUsed(Register reg) {
        return numRegisterUsage(reg) > 0;
    }

    public int numRegisterUsage(Register reg) {
        return fetchCount(registerUsage, reg);
    }

    public void registerUsed(Register reg) {
        incrementCount(registerUsage, reg);
    }

    public int numInstructionUsage(String insn) {
        return fetchCount(insnUsage, insn);
    }

    public void instructionUsed(String insn) {
        incrementCount(insnUsage, insn);
    }

    protected int fetchCount(Map m, Object key) {
        Integer n = (Integer)m.get(key);
        if (n == null) {
            return 0;
        }
        else {
            return n.intValue();
        }
    }

    protected void incrementCount(Map m, Object key) {
        m.put(key, new Integer(fetchCount(m, key) + 1));
    }
}
