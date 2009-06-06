package net.loveruby.cflat.asm;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Statistics {
    protected Map<Register, Integer> registerUsage;
    protected Map<String, Integer> insnUsage;
    protected Map<Symbol, Integer> symbolUsage;

    static public Statistics collect(List<Assembly> assemblies) {
        Statistics stats = new Statistics();
        for (Assembly asm : assemblies) {
            asm.collectStatistics(stats);
        }
        return stats;
    }

    public Statistics() {
        registerUsage = new HashMap<Register, Integer>();
        insnUsage = new HashMap<String, Integer>();
        symbolUsage = new HashMap<Symbol, Integer>();
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

    public boolean doesSymbolUsed(Label label) {
        return doesSymbolUsed(label.symbol());
    }

    public boolean doesSymbolUsed(Symbol sym) {
        return numSymbolUsed(sym) > 0;
    }

    public int numSymbolUsed(Symbol sym) {
        return fetchCount(symbolUsage, sym);
    }

    public void symbolUsed(Symbol sym) {
        incrementCount(symbolUsage, sym);
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
