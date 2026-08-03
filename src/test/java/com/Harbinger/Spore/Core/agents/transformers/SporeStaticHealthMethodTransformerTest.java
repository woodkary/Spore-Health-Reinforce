package com.Harbinger.Spore.Core.agents.transformers;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SporeStaticHealthMethodTransformerTest {
    private static final String FIXTURE = "test/StaticLifecycleHealthFixture";
    private static final String WHITE_LISTED_FIXTURE =
            "net/minecraft/world/entity/WhitelistedStaticHealthFixture";
    private static final String FLOAT_DESC = "(Ljava/lang/Object;F)F";
    private static final String DOUBLE_DESC = "(Ljava/lang/Object;D)D";

    @Test
    void wrapsEveryFloatAndDoubleReturnAndRemainsIdempotent() throws Exception {
        assertTrue(SporeStaticHealthMethodRegistry.register(
                FIXTURE,
                "readFloat",
                FLOAT_DESC,
                List.of(0)
        ));
        assertTrue(SporeStaticHealthMethodRegistry.register(
                FIXTURE,
                "readDouble",
                DOUBLE_DESC,
                List.of(0)
        ));

        byte[] original = fixtureBytes(FIXTURE);
        SporeStaticHealthMethodTransformer transformer = new SporeStaticHealthMethodTransformer();
        byte[] transformed = transformer.transformClassByte(
                getClass().getClassLoader(),
                FIXTURE,
                original
        );

        assertNotNull(transformed);
        Map<String, Integer> hookCounts = hookCounts(transformed);
        assertEquals(2, hookCounts.get("readFloat" + FLOAT_DESC));
        assertEquals(1, hookCounts.get("readDouble" + DOUBLE_DESC));

        Class<?> fixture = new FixtureClassLoader(getClass().getClassLoader()).define(transformed);
        assertEquals(4.0f, fixture.getMethod("readFloat", Object.class, float.class)
                .invoke(null, new Object(), 4.0f));
        assertEquals(3.0d, fixture.getMethod("readDouble", Object.class, double.class)
                .invoke(null, new Object(), 3.0d));
        assertNull(transformer.transformClassByte(getClass().getClassLoader(), FIXTURE, transformed));
    }

    @Test
    void doesNotTransformWhiteListedClasses() {
        assertTrue(SporeStaticHealthMethodRegistry.register(
                WHITE_LISTED_FIXTURE,
                "readFloat",
                FLOAT_DESC,
                List.of(0)
        ));

        SporeStaticHealthMethodTransformer transformer = new SporeStaticHealthMethodTransformer();
        assertNull(transformer.transformClassByte(
                getClass().getClassLoader(),
                "bad/mod/DisguisedRequestName",
                fixtureBytes(WHITE_LISTED_FIXTURE)
        ));
    }

    @Test
    void mergesArgumentIndexesAndNormalizesHiddenNames() {
        String hiddenOwner = "test/RegistryFixture/0x0000000000000001";
        assertTrue(SporeStaticHealthMethodRegistry.register(
                hiddenOwner,
                "read",
                "(Ljava/lang/Object;Ljava/lang/Object;)F",
                List.of(1)
        ));
        assertTrue(SporeStaticHealthMethodRegistry.register(
                "test.RegistryFixture+0x0000000000000002",
                "read",
                "(Ljava/lang/Object;Ljava/lang/Object;)F",
                List.of(0, 1)
        ));

        int[] indexes = SporeStaticHealthMethodRegistry
                .targetsForOwner("test/RegistryFixture")
                .get("read(Ljava/lang/Object;Ljava/lang/Object;)F");
        assertArrayEquals(new int[]{0, 1}, indexes);
    }

    private byte[] fixtureBytes(String fixtureName) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, fixtureName, null, "java/lang/Object", null);

        MethodVisitor floatMethod = writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "readFloat",
                FLOAT_DESC,
                null,
                null
        );
        floatMethod.visitCode();
        floatMethod.visitVarInsn(Opcodes.FLOAD, 1);
        floatMethod.visitInsn(Opcodes.FCONST_0);
        floatMethod.visitInsn(Opcodes.FCMPG);
        Label nonPositive = new Label();
        floatMethod.visitJumpInsn(Opcodes.IFLE, nonPositive);
        floatMethod.visitVarInsn(Opcodes.FLOAD, 1);
        floatMethod.visitInsn(Opcodes.FRETURN);
        floatMethod.visitLabel(nonPositive);
        floatMethod.visitInsn(Opcodes.FCONST_0);
        floatMethod.visitInsn(Opcodes.FRETURN);
        floatMethod.visitMaxs(0, 0);
        floatMethod.visitEnd();

        MethodVisitor doubleMethod = writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "readDouble",
                DOUBLE_DESC,
                null,
                null
        );
        doubleMethod.visitCode();
        doubleMethod.visitVarInsn(Opcodes.DLOAD, 1);
        doubleMethod.visitInsn(Opcodes.DRETURN);
        doubleMethod.visitMaxs(0, 0);
        doubleMethod.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private Map<String, Integer> hookCounts(byte[] transformed) {
        ClassNode classNode = new ClassNode();
        new ClassReader(transformed).accept(classNode, ClassReader.EXPAND_FRAMES);
        java.util.HashMap<String, Integer> result = new java.util.HashMap<>();
        for (MethodNode method : classNode.methods) {
            int count = 0;
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null;
                 instruction = instruction.getNext()) {
                if (instruction instanceof MethodInsnNode call
                        && "com/Harbinger/Spore/Core/asmHooks/IEntityHealth".equals(call.owner)
                        && "getHeealth".equals(call.name)) {
                    count++;
                }
            }
            result.put(method.name + method.desc, count);
        }
        return result;
    }

    private static final class FixtureClassLoader extends ClassLoader {
        private FixtureClassLoader(ClassLoader parent) {
            super(parent);
        }

        private Class<?> define(byte[] bytes) {
            return defineClass("test.StaticLifecycleHealthFixture", bytes, 0, bytes.length);
        }
    }
}
