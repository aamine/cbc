package net.loveruby.cflat.asm;
import java.util.List;

public interface AsmOptimizer {
    public List<Assembly> optimize(List<Assembly> assemblies);
}
