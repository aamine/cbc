package net.loveruby.cflat.asm;
import net.loveruby.cflat.utils.Cursor;
import java.util.*;

public class PeepholeOptimizer implements AsmOptimizer {
    protected Map filterSet;   // Map<String, List<Filter>>

    public PeepholeOptimizer() {
        this.filterSet = new HashMap();
    }

    public void add(Filter filter) {
        String[] heads = filter.patternHeads();
        for (int i = 0; i < heads.length; i++) {
            String head = heads[i];
            List list = (List)filterSet.get(head);
            if (list == null) {
                list = new ArrayList();
                list.add(filter);
                filterSet.put(head, list);
            }
            else {
                list.add(filter);
            }
        }
    }

    public List optimize(List assemblies) {
        List result = new ArrayList();
        Cursor cursor = new Cursor(assemblies);
        while (cursor.hasNext()) {
            Assembly asm = (Assembly)cursor.next();
            if (asm.isInstruction()) {
                Filter matched = matchFilter(cursor);
                if (matched != null) {
                    matched.optimize(cursor, result);
                    continue;
                }
            }
            result.add(asm);
        }
        return result;
    }

    protected Filter matchFilter(Cursor asms) {
        Instruction insn = (Instruction)asms.current();
        List filters = (List)filterSet.get(insn.mnemonic());
        if (filters == null) return null;
        if (filters.isEmpty()) return null;
        Iterator it = filters.iterator();
        while (it.hasNext()) {
            Filter filter = (Filter)it.next();
            if (filter.match(asms)) {
                return filter;
            }
        }
        return null;
    }

    static public PeepholeOptimizer defaultSet() {
        PeepholeOptimizer set = new PeepholeOptimizer();
        set.loadDefaultFilters();
        return set;
    }

    protected void loadDefaultFilters() {
        PeepholeOptimizer set = this;

        // mov
        set.add(new SingleInsnFilter(
            new InsnPattern("mov", imm(0), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("xor", insn.operand2(), insn.operand2());
                }
            }
        ));

        // add
        set.add(new SingleInsnFilter(
            new InsnPattern("add", imm(-1), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("dec", insn.operand2());
                }
            }
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("add", imm(0), reg()),
            null
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("add", imm(1), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("inc", insn.operand2());
                }
            }
        ));

        // sub
        set.add(new SingleInsnFilter(
            new InsnPattern("sub", imm(-1), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("inc", insn.operand2());
                }
            }
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("sub", imm(0), reg()),
            null
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("sub", imm(1), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("dec", insn.operand2());
                }
            }
        ));

        // imul
        set.add(new SingleInsnFilter(
            new InsnPattern("imul", imm(0), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("xorl", insn.operand2(), insn.operand2());
                }
            }
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("imul", imm(1), reg()),
            null
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("imul", imm(2), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("sal", imm(1), insn.operand2());
                }
            }
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("imul", imm(4), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("sal", imm(2), insn.operand2());
                }
            }
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("imul", imm(8), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("sal", imm(3), insn.operand2());
                }
            }
        ));
        set.add(new SingleInsnFilter(
            new InsnPattern("imul", imm(16), reg()),
            new InsnTransform() {
                public Instruction apply(Instruction insn) {
                    return insn.build("sal", imm(4), insn.operand2());
                }
            }
        ));

        // jmp
        set.add(new JumpEliminationFilter());
    }

    protected ImmediateValue imm(long n) {
        return new ImmediateValue(n);
    }

    protected OperandPattern reg() {
        return new AnyRegisterPattern();
    }

    abstract class Filter {
        abstract public String[] patternHeads();
        abstract public boolean match(Cursor asms);
        abstract public void optimize(Cursor src, List dest);
    }

    //
    // single instruction optimization
    //

    class SingleInsnFilter extends Filter {
        protected InsnPattern pattern;
        protected InsnTransform transform;

        public SingleInsnFilter(InsnPattern pattern, InsnTransform transform) {
            this.pattern = pattern;
            this.transform = transform;
        }

        /** Matching mnemonic of InstructionPattern */
        public String[] patternHeads() {
            return new String[] { pattern.name };
        }

        public boolean match(Cursor asms) {
            return pattern.match((Instruction)asms.current());
        }

        public void optimize(Cursor src, List dest) {
            if (transform == null) {
                ;   // remove instruction
            }
            else {
                dest.add(transform.apply((Instruction)src.current()));
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

    interface InsnTransform {
        abstract public Instruction apply(Instruction insn);
    }

    //
    // jumpElimination
    //

    class JumpEliminationFilter extends Filter {
        public JumpEliminationFilter() {
        }

        protected String[] jmpInsns() {
            return new String[] { "jmp", "jz", "jne", "je", "jne" };
        }

        public String[] patternHeads() {
            return jmpInsns();
        }

        public void optimize(Cursor src, List dest) {
            ;   // remove jump
        }

        public boolean match(Cursor asms) {
            Instruction insn = (Instruction)asms.current();
            return doesLabelFollows(asms.dup(), insn.jmpDestination());
        }

        /**
         * Returns true if jmpDest is found in asms before any instruction
         * or directives.  For example, this method returns true if contents
         * of asms are:
         *
         *    if_end3:
         *          # comment
         *    jmpDest:
         *          mov
         *          mov
         *          add
         */
        protected boolean doesLabelFollows(Cursor asms, Label jmpDest) {
            while (asms.hasNext()) {
                Assembly asm = (Assembly)asms.next();
                if (asm.isLabel()) {
                    Label label = (Label)asm;
                    if (label.equals(jmpDest)) {
                        return true;
                    }
                    else {
                        continue;
                    }
                }
                else if (asm.isComment()) {
                    continue;
                }
                else {
                    // instructions or directives
                    return false;
                }
            }
            return false;
        }
    }
}
