package net.loveruby.cflat.asm;
import java.util.*;

public class PeepholeOptimizer implements AsmOptimizer {
    protected List filters;

    public PeepholeOptimizer() {
        this.filters = defaultFilterSet();
    }

    // List<Filter>
    protected List defaultFilterSet() {
        List set = new ArrayList();

        // mov
        set.add(new Filter(
            new InsnPattern("mov", imm(0), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("xor", insn.operand2(), insn.operand2());
                }
            }
        ));

        // add
        set.add(new Filter(
            new InsnPattern("add", imm(-1), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("dec", insn.operand2());
                }
            }
        ));
        set.add(new Filter(
            new InsnPattern("add", imm(0), reg()),
            null
        ));
        set.add(new Filter(
            new InsnPattern("add", imm(1), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("inc", insn.operand2());
                }
            }
        ));

        // sub
        set.add(new Filter(
            new InsnPattern("sub", imm(-1), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("inc", insn.operand2());
                }
            }
        ));
        set.add(new Filter(
            new InsnPattern("sub", imm(0), reg()),
            null
        ));
        set.add(new Filter(
            new InsnPattern("sub", imm(1), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("dec", insn.operand2());
                }
            }
        ));

        // imul
        set.add(new Filter(
            new InsnPattern("imul", imm(0), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("xorl", insn.operand2(), insn.operand2());
                }
            }
        ));
        set.add(new Filter(
            new InsnPattern("imul", imm(1), reg()),
            null
        ));
        set.add(new Filter(
            new InsnPattern("imul", imm(2), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("sal", imm(1), insn.operand2());
                }
            }
        ));
        set.add(new Filter(
            new InsnPattern("imul", imm(4), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("sal", imm(2), insn.operand2());
                }
            }
        ));
        set.add(new Filter(
            new InsnPattern("imul", imm(8), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("sal", imm(3), insn.operand2());
                }
            }
        ));
        set.add(new Filter(
            new InsnPattern("imul", imm(16), reg()),
            new InsnTemplate() {
                public Instruction apply(Instruction insn) {
                    return insn.build("sal", imm(4), insn.operand2());
                }
            }
        ));

        return set;
    }

    protected ImmediateValue imm(long n) {
        return new ImmediateValue(n);
    }

    protected OperandPattern reg() {
        return new AnyRegisterPattern();
    }

    public List optimize(List assemblies) {
        List result = new ArrayList();
        Iterator asms = assemblies.iterator();
        while (asms.hasNext()) {
            Assembly asm = (Assembly)asms.next();
            if (! asm.isInstruction()) {
                result.add(asm);
            }
            else {
                Assembly optAsm = optimizeInstruction((Instruction)asm);
                if (optAsm == null) {
                    // remove instruction
                }
                else {
                    result.add(optAsm);
                }
            }
        }
        return result;
    }

    protected Assembly optimizeInstruction(Instruction insn) {
        Iterator it = filters.iterator();
        while (it.hasNext()) {
            Filter filter = (Filter)it.next();
            if (filter.match(insn)) {
                return filter.optimize(insn);
            }
        }
        return insn;
    }

    class Filter {
        protected InsnPattern pattern;
        protected InsnTemplate template;

        public Filter(InsnPattern pattern, InsnTemplate template) {
            this.pattern = pattern;
            this.template = template;
        }

        public boolean match(Instruction insn) {
            return pattern.match(insn);
        }

        public Instruction optimize(Instruction insn) {
            if (template == null) {
                return null;
            }
            else {
                return template.apply(insn);
            }
        }
    }

    class InsnPattern {
        protected String name;
        protected OperandPattern pattern1;
        protected OperandPattern pattern2;

        InsnPattern(String name, OperandPattern pat1, OperandPattern pat2) {
            this.name = name;
            this.pattern1 = pat1;
            this.pattern2 = pat2;
        }

        public boolean match(Instruction insn) {
            return name.equals(insn.mnemonic())
                && (pattern1 == null || pattern1.match(insn.operand1()))
                && (pattern2 == null || pattern2.match(insn.operand2()));
        }
    }

    class AnyRegisterPattern implements OperandPattern {
        public boolean match(AsmOperand operand) {
            return operand.isRegister();
        }
    }

    interface InsnTemplate {
        abstract public Instruction apply(Instruction insn);
    }
}
