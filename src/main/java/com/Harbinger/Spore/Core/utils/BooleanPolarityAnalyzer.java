package com.Harbinger.Spore.Core.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Conservatively proves how one boolean invocation contributes to normal boolean returns. */
public final class BooleanPolarityAnalyzer {
    private BooleanPolarityAnalyzer() {
    }

    public static BooleanPolarity analyze(MethodNode method,
                                          MethodInsnNode invocation,
                                          Frame<BasicValue> invocationFrame) {
        if (method == null || invocation == null || invocationFrame == null
                || method.instructions == null || method.instructions.size() == 0
                || (method.tryCatchBlocks != null && !method.tryCatchBlocks.isEmpty())) {
            return BooleanPolarity.UNKNOWN;
        }
        try {
            return analyzeInternal(method, invocation, invocationFrame);
        } catch (AnalyzerException | RuntimeException e) {
            return BooleanPolarity.UNKNOWN;
        }
    }

    private static BooleanPolarity analyzeInternal(MethodNode method,
                                                    MethodInsnNode invocation,
                                                    Frame<BasicValue> invocationFrame)
            throws AnalyzerException {
        AbstractInsnNode[] instructions = method.instructions.toArray();
        int invocationIndex = method.instructions.indexOf(invocation);
        if (invocationIndex < 0 || invocationIndex + 1 >= instructions.length) {
            return BooleanPolarity.UNKNOWN;
        }

        Map<LabelNode, Integer> labelIndexes = new IdentityHashMap<>();
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i] instanceof LabelNode label) {
                labelIndexes.put(label, i);
            }
        }

        PolarityInterpreter interpreter = new PolarityInterpreter(invocation);
        Frame<BasicValue> initialFrame = initialFrame(method, invocation, invocationFrame);
        Map<StateKey, Frame<BasicValue>> states = new HashMap<>();
        Deque<StateKey> work = new ArrayDeque<>();
        ResultAccumulator result = new ResultAccumulator();
        enqueue(invocationIndex + 1, KnownBoolean.UNKNOWN, initialFrame, states, work, interpreter, result,
                instructions.length);

        while (!work.isEmpty()) {
            StateKey stateKey = work.removeFirst();
            Frame<BasicValue> frame = states.get(stateKey);
            if (frame == null) {
                continue;
            }
            int instructionIndex = stateKey.instructionIndex();
            AbstractInsnNode instruction = instructions[instructionIndex];
            int opcode = instruction.getOpcode();
            if (opcode < 0) {
                enqueue(instructionIndex + 1, stateKey.knownBoolean(), frame,
                        states, work, interpreter, result, instructions.length);
                continue;
            }
            if (opcode == Opcodes.IRETURN) {
                if (frame.getStackSize() == 0) {
                    result.add(BooleanPolarity.UNKNOWN);
                } else {
                    result.add(classifyReturn(
                            frame.getStack(frame.getStackSize() - 1),
                            stateKey.knownBoolean()
                    ));
                }
                continue;
            }
            if (opcode == Opcodes.ATHROW) {
                continue;
            }
            if (opcode == Opcodes.RETURN
                    || opcode == Opcodes.FRETURN
                    || opcode == Opcodes.DRETURN
                    || opcode == Opcodes.LRETURN
                    || opcode == Opcodes.ARETURN
                    || opcode == Opcodes.RET
                    || opcode == Opcodes.JSR) {
                result.add(BooleanPolarity.UNKNOWN);
                continue;
            }

            BasicValue branchValue = null;
            if ((opcode == Opcodes.IFEQ || opcode == Opcodes.IFNE) && frame.getStackSize() > 0) {
                branchValue = frame.getStack(frame.getStackSize() - 1);
            }
            Frame<BasicValue> nextFrame = new Frame<>(frame);
            nextFrame.execute(instruction, interpreter);

            if (instruction instanceof JumpInsnNode jump) {
                Integer targetIndex = labelIndexes.get(jump.label);
                if (targetIndex == null) {
                    result.add(BooleanPolarity.UNKNOWN);
                    continue;
                }
                if (opcode == Opcodes.GOTO) {
                    enqueue(targetIndex, stateKey.knownBoolean(), nextFrame,
                            states, work, interpreter, result, instructions.length);
                } else if (opcode == Opcodes.IFEQ || opcode == Opcodes.IFNE) {
                    boolean targetCondition = opcode == Opcodes.IFNE;
                    enqueueBooleanEdge(targetIndex, targetCondition, branchValue, stateKey.knownBoolean(), nextFrame,
                            states, work, interpreter, result, instructions.length);
                    enqueueBooleanEdge(instructionIndex + 1, !targetCondition, branchValue,
                            stateKey.knownBoolean(), nextFrame,
                            states, work, interpreter, result, instructions.length);
                } else {
                    enqueue(targetIndex, stateKey.knownBoolean(), nextFrame,
                            states, work, interpreter, result, instructions.length);
                    enqueue(instructionIndex + 1, stateKey.knownBoolean(), nextFrame,
                            states, work, interpreter, result, instructions.length);
                }
                continue;
            }
            if (instruction instanceof TableSwitchInsnNode tableSwitch) {
                enqueueSwitchTargets(tableSwitch.dflt, tableSwitch.labels, stateKey.knownBoolean(), nextFrame,
                        labelIndexes, states, work, interpreter, result, instructions.length);
                continue;
            }
            if (instruction instanceof LookupSwitchInsnNode lookupSwitch) {
                enqueueSwitchTargets(lookupSwitch.dflt, lookupSwitch.labels, stateKey.knownBoolean(), nextFrame,
                        labelIndexes, states, work, interpreter, result, instructions.length);
                continue;
            }
            enqueue(instructionIndex + 1, stateKey.knownBoolean(), nextFrame,
                    states, work, interpreter, result, instructions.length);
        }
        return result.finish();
    }

    private static Frame<BasicValue> initialFrame(MethodNode method,
                                                  MethodInsnNode invocation,
                                                  Frame<BasicValue> invocationFrame)
            throws AnalyzerException {
        Type[] arguments = Type.getArgumentTypes(invocation.desc);
        int consumedValues = arguments.length + (invocation.getOpcode() == Opcodes.INVOKESTATIC ? 0 : 1);
        int remainingStack = invocationFrame.getStackSize() - consumedValues;
        if (remainingStack < 0) {
            throw new AnalyzerException(invocation, "Invocation stack underflow");
        }
        int maxStack = Math.max(method.maxStack, invocationFrame.getStackSize() + 4);
        Frame<BasicValue> result = new Frame<>(invocationFrame.getLocals(), maxStack);
        for (int i = 0; i < invocationFrame.getLocals(); i++) {
            result.setLocal(i, PolarityValue.fromBasic(invocationFrame.getLocal(i)));
        }
        for (int i = 0; i < remainingStack; i++) {
            result.push(PolarityValue.fromBasic(invocationFrame.getStack(i)));
        }
        result.push(new PolarityValue(Type.INT_TYPE, Relation.DIRECT));
        return result;
    }

    private static void enqueueBooleanEdge(int instructionIndex,
                                           boolean conditionIsTrue,
                                           BasicValue condition,
                                           KnownBoolean currentKnown,
                                           Frame<BasicValue> frame,
                                           Map<StateKey, Frame<BasicValue>> states,
                                           Deque<StateKey> work,
                                           PolarityInterpreter interpreter,
                                           ResultAccumulator result,
                                           int instructionCount) throws AnalyzerException {
        Relation relation = relationOf(condition);
        if ((relation == Relation.CONST_TRUE && !conditionIsTrue)
                || (relation == Relation.CONST_FALSE && conditionIsTrue)) {
            return;
        }
        KnownBoolean edgeKnown = knownFromCondition(relation, conditionIsTrue);
        KnownBoolean combined = combineKnown(currentKnown, edgeKnown);
        if (combined != null) {
            enqueue(instructionIndex, combined, frame, states, work, interpreter, result, instructionCount);
        }
    }

    private static KnownBoolean knownFromCondition(Relation relation, boolean conditionIsTrue) {
        if (relation == Relation.DIRECT) {
            return conditionIsTrue ? KnownBoolean.TRUE : KnownBoolean.FALSE;
        }
        if (relation == Relation.NEGATED) {
            return conditionIsTrue ? KnownBoolean.FALSE : KnownBoolean.TRUE;
        }
        return KnownBoolean.UNKNOWN;
    }

    private static KnownBoolean combineKnown(KnownBoolean current, KnownBoolean edge) {
        if (edge == KnownBoolean.UNKNOWN) {
            return current;
        }
        if (current == KnownBoolean.UNKNOWN || current == edge) {
            return edge;
        }
        return null;
    }

    private static void enqueueSwitchTargets(LabelNode defaultLabel,
                                             List<LabelNode> labels,
                                             KnownBoolean known,
                                             Frame<BasicValue> frame,
                                             Map<LabelNode, Integer> labelIndexes,
                                             Map<StateKey, Frame<BasicValue>> states,
                                             Deque<StateKey> work,
                                             PolarityInterpreter interpreter,
                                             ResultAccumulator result,
                                             int instructionCount) throws AnalyzerException {
        Integer defaultIndex = labelIndexes.get(defaultLabel);
        if (defaultIndex == null) {
            result.add(BooleanPolarity.UNKNOWN);
            return;
        }
        enqueue(defaultIndex, known, frame, states, work, interpreter, result, instructionCount);
        for (LabelNode label : labels) {
            Integer targetIndex = labelIndexes.get(label);
            if (targetIndex == null) {
                result.add(BooleanPolarity.UNKNOWN);
            } else {
                enqueue(targetIndex, known, frame, states, work, interpreter, result, instructionCount);
            }
        }
    }

    private static void enqueue(int instructionIndex,
                                KnownBoolean known,
                                Frame<BasicValue> frame,
                                Map<StateKey, Frame<BasicValue>> states,
                                Deque<StateKey> work,
                                PolarityInterpreter interpreter,
                                ResultAccumulator result,
                                int instructionCount) throws AnalyzerException {
        if (instructionIndex < 0 || instructionIndex >= instructionCount) {
            result.add(BooleanPolarity.UNKNOWN);
            return;
        }
        StateKey key = new StateKey(instructionIndex, known);
        Frame<BasicValue> existing = states.get(key);
        if (existing == null) {
            states.put(key, new Frame<>(frame));
            work.addLast(key);
            return;
        }
        if (mergeFrames(existing, frame, known, interpreter)) {
            work.addLast(key);
        }
    }

    private static boolean mergeFrames(Frame<BasicValue> target,
                                       Frame<BasicValue> source,
                                       KnownBoolean known,
                                       PolarityInterpreter interpreter) throws AnalyzerException {
        if (target.getLocals() != source.getLocals() || target.getStackSize() != source.getStackSize()) {
            throw new AnalyzerException(null, "Incompatible boolean polarity frames");
        }
        boolean changed = false;
        for (int i = 0; i < target.getLocals(); i++) {
            BasicValue merged = interpreter.mergeWithKnown(target.getLocal(i), source.getLocal(i), known);
            if (!merged.equals(target.getLocal(i))) {
                target.setLocal(i, merged);
                changed = true;
            }
        }
        for (int i = 0; i < target.getStackSize(); i++) {
            BasicValue merged = interpreter.mergeWithKnown(target.getStack(i), source.getStack(i), known);
            if (!merged.equals(target.getStack(i))) {
                target.setStack(i, merged);
                changed = true;
            }
        }
        return changed;
    }

    private static BooleanPolarity classifyReturn(BasicValue value, KnownBoolean known) {
        if (value == null) {
            return BooleanPolarity.UNKNOWN;
        }
        Relation relation = relationOf(value);
        if (relation == Relation.DIRECT) {
            return BooleanPolarity.DIRECT;
        }
        if (relation == Relation.NEGATED) {
            return BooleanPolarity.NEGATED;
        }
        if (known != KnownBoolean.UNKNOWN
                && (relation == Relation.CONST_TRUE || relation == Relation.CONST_FALSE)) {
            boolean returned = relation == Relation.CONST_TRUE;
            boolean invocation = known == KnownBoolean.TRUE;
            return returned == invocation ? BooleanPolarity.DIRECT : BooleanPolarity.NEGATED;
        }
        return BooleanPolarity.UNKNOWN;
    }

    private static Relation relationOf(BasicValue value) {
        return value instanceof PolarityValue polarityValue
                ? polarityValue.relation()
                : Relation.NONE;
    }

    private enum KnownBoolean {
        UNKNOWN,
        FALSE,
        TRUE
    }

    private enum Relation {
        NONE,
        DIRECT,
        NEGATED,
        CONST_FALSE,
        CONST_TRUE,
        UNKNOWN
    }

    private record StateKey(int instructionIndex, KnownBoolean knownBoolean) {
    }

    private static final class ResultAccumulator {
        private BooleanPolarity polarity;
        private boolean sawReturn;

        private void add(BooleanPolarity candidate) {
            sawReturn = true;
            if (candidate == BooleanPolarity.UNKNOWN || polarity == BooleanPolarity.UNKNOWN) {
                polarity = BooleanPolarity.UNKNOWN;
            } else if (polarity == null) {
                polarity = candidate;
            } else if (polarity != candidate) {
                polarity = BooleanPolarity.UNKNOWN;
            }
        }

        private BooleanPolarity finish() {
            return sawReturn && polarity != null ? polarity : BooleanPolarity.UNKNOWN;
        }
    }

    private static final class PolarityValue extends BasicValue {
        private final Relation relation;

        private PolarityValue(Type type, Relation relation) {
            super(type);
            this.relation = relation;
        }

        private static PolarityValue fromBasic(BasicValue value) {
            return value == null
                    ? new PolarityValue(null, Relation.NONE)
                    : new PolarityValue(value.getType(), Relation.NONE);
        }

        private Relation relation() {
            return relation;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof PolarityValue value
                    && relation == value.relation
                    && super.equals(other);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + relation.hashCode();
        }
    }

    private static final class PolarityInterpreter extends BasicInterpreter {
        private final MethodInsnNode targetInvocation;

        private PolarityInterpreter(MethodInsnNode targetInvocation) {
            super(Opcodes.ASM9);
            this.targetInvocation = targetInvocation;
        }

        @Override
        public BasicValue newValue(Type type) {
            return wrap(super.newValue(type), Relation.NONE);
        }

        @Override
        public BasicValue newOperation(AbstractInsnNode instruction) throws AnalyzerException {
            BasicValue value = super.newOperation(instruction);
            Relation relation = constantRelation(instruction);
            return wrap(value, relation);
        }

        @Override
        public BasicValue copyOperation(AbstractInsnNode instruction, BasicValue value) throws AnalyzerException {
            super.copyOperation(instruction, value);
            return value;
        }

        @Override
        public BasicValue unaryOperation(AbstractInsnNode instruction, BasicValue value) throws AnalyzerException {
            BasicValue result = super.unaryOperation(instruction, value);
            Relation relation = relationOf(value);
            return wrap(result, dependsOnInvocation(relation) ? Relation.UNKNOWN : Relation.NONE);
        }

        @Override
        public BasicValue binaryOperation(AbstractInsnNode instruction,
                                          BasicValue first,
                                          BasicValue second) throws AnalyzerException {
            BasicValue result = super.binaryOperation(instruction, first, second);
            Relation relation = instruction.getOpcode() == Opcodes.IXOR
                    ? xorRelation(relationOf(first), relationOf(second))
                    : combinedUnknown(relationOf(first), relationOf(second));
            return wrap(result, relation);
        }

        @Override
        public BasicValue ternaryOperation(AbstractInsnNode instruction,
                                           BasicValue first,
                                           BasicValue second,
                                           BasicValue third) throws AnalyzerException {
            return super.ternaryOperation(instruction, first, second, third);
        }

        @Override
        public BasicValue naryOperation(AbstractInsnNode instruction,
                                        List<? extends BasicValue> values) throws AnalyzerException {
            BasicValue result = super.naryOperation(instruction, values);
            if (result == null) {
                return null;
            }
            if (instruction == targetInvocation) {
                return wrap(result, Relation.UNKNOWN);
            }
            for (BasicValue value : values) {
                if (dependsOnInvocation(relationOf(value))) {
                    return wrap(result, Relation.UNKNOWN);
                }
            }
            return wrap(result, Relation.NONE);
        }

        @Override
        public void returnOperation(AbstractInsnNode instruction,
                                    BasicValue value,
                                    BasicValue expected) throws AnalyzerException {
            super.returnOperation(instruction, value, expected);
        }

        @Override
        public BasicValue merge(BasicValue first, BasicValue second) {
            return mergeWithKnown(first, second, KnownBoolean.UNKNOWN);
        }

        private PolarityValue mergeWithKnown(BasicValue first,
                                             BasicValue second,
                                             KnownBoolean known) {
            BasicValue mergedType = mergeBasicTypes(first, second);
            Relation firstRelation = canonical(relationOf(first), known);
            Relation secondRelation = canonical(relationOf(second), known);
            Relation mergedRelation = firstRelation == secondRelation
                    ? firstRelation
                    : (firstRelation == Relation.NONE && secondRelation == Relation.NONE
                    ? Relation.NONE
                    : Relation.UNKNOWN);
            return new PolarityValue(mergedType.getType(), mergedRelation);
        }

        private BasicValue mergeBasicTypes(BasicValue first, BasicValue second) {
            BasicValue plainFirst = new BasicValue(first.getType());
            BasicValue plainSecond = new BasicValue(second.getType());
            return super.merge(plainFirst, plainSecond);
        }

        private Relation canonical(Relation relation, KnownBoolean known) {
            if (known == KnownBoolean.UNKNOWN) {
                return relation;
            }
            if (relation == Relation.CONST_TRUE) {
                return known == KnownBoolean.TRUE ? Relation.DIRECT : Relation.NEGATED;
            }
            if (relation == Relation.CONST_FALSE) {
                return known == KnownBoolean.FALSE ? Relation.DIRECT : Relation.NEGATED;
            }
            return relation;
        }

        private BasicValue wrap(BasicValue value, Relation relation) {
            return value == null ? null : new PolarityValue(value.getType(), relation);
        }

        private Relation relationOf(BasicValue value) {
            return value instanceof PolarityValue polarityValue
                    ? polarityValue.relation()
                    : Relation.NONE;
        }

        private Relation constantRelation(AbstractInsnNode instruction) {
            if (instruction.getOpcode() == Opcodes.ICONST_0) {
                return Relation.CONST_FALSE;
            }
            if (instruction.getOpcode() == Opcodes.ICONST_1) {
                return Relation.CONST_TRUE;
            }
            if (instruction instanceof IntInsnNode intInstruction) {
                if (intInstruction.operand == 0) {
                    return Relation.CONST_FALSE;
                }
                if (intInstruction.operand == 1) {
                    return Relation.CONST_TRUE;
                }
            }
            if (instruction instanceof LdcInsnNode ldc && ldc.cst instanceof Integer integer) {
                if (integer == 0) {
                    return Relation.CONST_FALSE;
                }
                if (integer == 1) {
                    return Relation.CONST_TRUE;
                }
            }
            return Relation.NONE;
        }

        private Relation xorRelation(Relation first, Relation second) {
            if (first == Relation.CONST_FALSE) {
                return second;
            }
            if (second == Relation.CONST_FALSE) {
                return first;
            }
            if (first == Relation.CONST_TRUE) {
                return negate(second);
            }
            if (second == Relation.CONST_TRUE) {
                return negate(first);
            }
            return combinedUnknown(first, second);
        }

        private Relation negate(Relation relation) {
            if (relation == Relation.DIRECT) {
                return Relation.NEGATED;
            }
            if (relation == Relation.NEGATED) {
                return Relation.DIRECT;
            }
            if (relation == Relation.CONST_TRUE) {
                return Relation.CONST_FALSE;
            }
            if (relation == Relation.CONST_FALSE) {
                return Relation.CONST_TRUE;
            }
            return relation == Relation.NONE ? Relation.NONE : Relation.UNKNOWN;
        }

        private Relation combinedUnknown(Relation first, Relation second) {
            return dependsOnInvocation(first) || dependsOnInvocation(second)
                    ? Relation.UNKNOWN
                    : Relation.NONE;
        }

        private boolean dependsOnInvocation(Relation relation) {
            return relation == Relation.DIRECT
                    || relation == Relation.NEGATED
                    || relation == Relation.UNKNOWN;
        }
    }
}
