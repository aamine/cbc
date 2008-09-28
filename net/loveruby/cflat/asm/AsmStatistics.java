package net.loveruby.cflat.asm;
import java.util.*;

public class AsmStatistics {
    protected Map<Register, Integer> registerUsage;
    protected Map<String, Integer> insnUsage;
    protected Map<Label, Integer> labelUsage;

    static public AsmStatistics collect(List<Assembly> assemblies) {
        AsmStatistics stats = new AsmStatistics();
        for (Assembly asm : assemblies) {
            asm.collectStatistics(stats);
        }
        return stats;
    }

    public AsmStatistics() {
        registerUsage = new HashMap<Register, Integer>();
        insnUsage = new HashMap<String, Integer>();
        labelUsage = new HashMap<Label, Integer>();
    }

    public boolean doesRegisterUsed(Register reg) {
        return numRegisterUsed(reg) > 0;
    }

    public int numRegisterUsed(Register reg) {
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

    public boolean doesLabelUsed(Label label) {
        return numLabelUsed(label) > 0;
    }

    public int numLabelUsed(Label label) {
        return fetchCount(labelUsage, label);
    }

    public void labelUsed(Label label) {
        incrementCount(labelUsage, label);
    }

    protected <K> int fetchCount(Map<K, Integer> m, K key) {
        Integer n = m.get(key);
        if (n == null) {
            return 0;
        }
        else {
            return n;
        }
    }

    protected <K> void incrementCount(Map<K, Integer> m, K key) {
        m.put(key, fetchCount(m, key) + 1);
    }
}
