package com.Harbinger.Spore.Core.utils;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BooleanPolarityAnalyzerTest {
    private static final String OWNER = "test/BooleanPolarityFixture";
    private static final String HELPER_OWNER = "test/BooleanHelper";
    private static final String HELPER_NAME = "value";
    private static final String HELPER_DESC = "()Z";

    @Test
    void provesDirectReturnAcrossLabelsFramesAndLines() throws Exception {
        MethodNode method = method("direct");
        Label start = new Label();
        method.visitLabel(start);
        method.visitLineNumber(10, start);
        method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        appendHelperCall(method);
        method.visitInsn(Opcodes.IRETURN);
        finish(method, 1, 1);

        assertEquals(BooleanPolarity.DIRECT, analyze(method));
    }

    @Test
    void provesStoreLoadAndXorNegation() throws Exception {
        MethodNode direct = method("storedDirect");
        appendHelperCall(direct);
        direct.visitVarInsn(Opcodes.ISTORE, 1);
        direct.visitVarInsn(Opcodes.ILOAD, 1);
        direct.visitInsn(Opcodes.IRETURN);
        finish(direct, 1, 2);

        MethodNode negated = method("storedNegated");
        appendHelperCall(negated);
        negated.visitVarInsn(Opcodes.ISTORE, 1);
        negated.visitVarInsn(Opcodes.ILOAD, 1);
        negated.visitInsn(Opcodes.ICONST_1);
        negated.visitInsn(Opcodes.IXOR);
        negated.visitInsn(Opcodes.IRETURN);
        finish(negated, 2, 2);

        assertEquals(BooleanPolarity.DIRECT, analyze(direct));
        assertEquals(BooleanPolarity.NEGATED, analyze(negated));
    }

    @Test
    void provesJavacBooleanMaterializationForBothPolarities() throws Exception {
        assertEquals(BooleanPolarity.DIRECT, analyze(materialized("materializedDirect", false, false)));
        assertEquals(BooleanPolarity.NEGATED, analyze(materialized("materializedNegated", true, false)));
        assertEquals(BooleanPolarity.DIRECT, analyze(materialized("materializedIfne", false, true)));
    }

    @Test
    void provesMultipleNormalReturnPathsWhenTheyAgree() throws Exception {
        MethodNode method = method("multipleReturns");
        appendHelperCall(method);
        Label falseResult = new Label();
        method.visitJumpInsn(Opcodes.IFEQ, falseResult);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitLabel(falseResult);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        finish(method, 1, 1);

        assertEquals(BooleanPolarity.DIRECT, analyze(method));
    }

    @Test
    void returnsUnknownForComplexAndMixedExpressions() throws Exception {
        MethodNode complex = method("complex");
        appendHelperCall(complex);
        complex.visitInsn(Opcodes.ICONST_1);
        complex.visitInsn(Opcodes.IAND);
        complex.visitInsn(Opcodes.IRETURN);
        finish(complex, 2, 1);

        MethodNode mixed = method("mixed");
        appendHelperCall(mixed);
        mixed.visitVarInsn(Opcodes.ISTORE, 1);
        mixed.visitMethodInsn(Opcodes.INVOKESTATIC, HELPER_OWNER, "unrelated", HELPER_DESC, false);
        Label negated = new Label();
        mixed.visitJumpInsn(Opcodes.IFEQ, negated);
        mixed.visitVarInsn(Opcodes.ILOAD, 1);
        mixed.visitInsn(Opcodes.IRETURN);
        mixed.visitLabel(negated);
        mixed.visitVarInsn(Opcodes.ILOAD, 1);
        mixed.visitInsn(Opcodes.ICONST_1);
        mixed.visitInsn(Opcodes.IXOR);
        mixed.visitInsn(Opcodes.IRETURN);
        finish(mixed, 2, 2);

        assertEquals(BooleanPolarity.UNKNOWN, analyze(complex));
        assertEquals(BooleanPolarity.UNKNOWN, analyze(mixed));
    }

    private MethodNode materialized(String name, boolean negated, boolean useIfne) {
        MethodNode method = method(name);
        appendHelperCall(method);
        Label branch = new Label();
        Label result = new Label();
        method.visitJumpInsn(useIfne ? Opcodes.IFNE : Opcodes.IFEQ, branch);
        boolean fallthroughInput = !useIfne;
        boolean fallthroughResult = negated ? !fallthroughInput : fallthroughInput;
        method.visitInsn(fallthroughResult ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        method.visitJumpInsn(Opcodes.GOTO, result);
        method.visitLabel(branch);
        boolean branchInput = useIfne;
        boolean branchResult = negated ? !branchInput : branchInput;
        method.visitInsn(branchResult ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        method.visitLabel(result);
        method.visitInsn(Opcodes.IRETURN);
        finish(method, 1, 1);
        return method;
    }

    private BooleanPolarity analyze(MethodNode method) throws Exception {
        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
        Frame<BasicValue>[] frames = analyzer.analyze(OWNER, method);
        int index = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext(), index++) {
            if (instruction instanceof MethodInsnNode call
                    && HELPER_OWNER.equals(call.owner)
                    && HELPER_NAME.equals(call.name)) {
                return BooleanPolarityAnalyzer.analyze(method, call, frames[index]);
            }
        }
        throw new AssertionError("Missing target helper call");
    }

    private MethodNode method(String name) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, "()Z", null, null);
        method.visitCode();
        return method;
    }

    private void appendHelperCall(MethodNode method) {
        method.visitMethodInsn(Opcodes.INVOKESTATIC, HELPER_OWNER, HELPER_NAME, HELPER_DESC, false);
    }

    private void finish(MethodNode method, int maxStack, int maxLocals) {
        method.visitMaxs(maxStack, maxLocals);
        method.visitEnd();
    }
}
