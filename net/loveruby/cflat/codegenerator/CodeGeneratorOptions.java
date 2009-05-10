package net.loveruby.cflat.codegenerator;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CodeGeneratorOptions {
    protected int optimizeLevel;
    protected boolean generatePIC;
    protected boolean generatePIE;
    protected boolean verboseAsm;

    public CodeGeneratorOptions() {
        optimizeLevel = 0;
        generatePIC = false;
        generatePIE = false;
        verboseAsm = false;
    }

    public void setOptimizationLevel(int level) {
        this.optimizeLevel = level;
    }

    public AsmOptimizer optimizer() {
        if (optimizeLevel > 0) {
            return PeepholeOptimizer.defaultSet();
        }
        else {
            return new NullOptimizer();
        }
    }

    class NullOptimizer implements AsmOptimizer {
        public List<Assembly> optimize(List<Assembly> asms) {
            return asms;
        }
    }

    public void generateVerboseAsm() {
        this.verboseAsm = true;
    }

    public boolean isVerboseAsm() {
        return verboseAsm;
    }

    public boolean isPositionIndependent() {
        return generatePIC || generatePIE;
    }

    public void generatePIC() {
        this.generatePIC = true;
    }

    public boolean isPICRequired() {
        return generatePIC;
    }

    public void generatePIE() {
        this.generatePIE = true;
    }

    public boolean isPIERequired() {
        return generatePIE;
    }
}
