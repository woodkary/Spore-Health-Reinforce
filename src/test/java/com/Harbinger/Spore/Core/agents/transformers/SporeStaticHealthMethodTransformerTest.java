package com.Harbinger.Spore.Core.agents.transformers;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SporeStaticHealthMethodTransformerTest {
    private static final String FIXTURE = "test/StaticLifecycleHealthFixture";
    private static final String WHITE_LISTED_FIXTURE =
            "net/minecraft/world/entity/WhitelistedStaticHealthFixture";
    private static final String MONSTER_MANAGER =
            "seraphina/astralrail_cube/util/entity/MonsterManager";
    private static final String OBTAIN_SERA = "_obtainSera";
    private static final String OBTAIN_SERA_DESC = "(Ljava/lang/Object;)F";
    private static final String HOOK_INTERFACE =
            "com/Harbinger/Spore/Core/asmHooks/IEntityHealth";
    private static final String HOOK_OWNER =
            "com/Harbinger/Spore/Core/asmHooks/EntityHeealuthManager";
    private static final String FLOAT_OBJECT_HOOK_DESC = "(FLjava/lang/Object;)F";
    private static final String DOUBLE_OBJECT_HOOK_DESC = "(DLjava/lang/Object;)D";
    private static final String LIVING_ENTITY_INTERNAL = "net/minecraft/world/entity/LivingEntity";
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
        assertHookDescriptors(transformed, "readFloat", FLOAT_DESC, List.of(
                FLOAT_OBJECT_HOOK_DESC,
                FLOAT_OBJECT_HOOK_DESC
        ));
        assertHookDescriptors(transformed, "readDouble", DOUBLE_DESC, List.of(
                DOUBLE_OBJECT_HOOK_DESC
        ));
        assertNoLivingEntityTypeInstructions(transformed, "readFloat", FLOAT_DESC);
        assertNoLivingEntityTypeInstructions(transformed, "readDouble", DOUBLE_DESC);

        Class<?> fixture = new FixtureClassLoader(getClass().getClassLoader()).define(transformed);
        assertEquals(4.0f, fixture.getMethod("readFloat", Object.class, float.class)
                .invoke(null, new Object(), 4.0f));
        assertEquals(3.0d, fixture.getMethod("readDouble", Object.class, double.class)
                .invoke(null, new Object(), 3.0d));
        assertNull(transformer.transformClassByte(getClass().getClassLoader(), FIXTURE, transformed));
    }

    @Test
    void appliesAllValidReferenceArgumentsInRegistryOrder() {
        String owner = "test/ReferenceArgumentStaticHealthFixture";
        String descriptor = "(JLjava/lang/Object;[IDLtest/CustomParent;)D";
        assertTrue(SporeStaticHealthMethodRegistry.register(
                owner,
                "readMixed",
                descriptor,
                List.of(4, 3, 2, 1, 0, 99)
        ));

        byte[] transformed = new SporeStaticHealthMethodTransformer().transformClassByte(
                getClass().getClassLoader(),
                owner,
                singleReturnFixture(owner, "readMixed", descriptor, Opcodes.DCONST_1, Opcodes.DRETURN)
        );

        assertNotNull(transformed);
        MethodNode method = methodNode(transformed, "readMixed", descriptor);
        List<MethodInsnNode> hooks = objectHooks(method);
        assertEquals(3, hooks.size());
        assertEquals(List.of(2, 3, 6), candidateSlots(hooks));
        for (MethodInsnNode hook : hooks) {
            assertEquals(DOUBLE_OBJECT_HOOK_DESC, hook.desc);
            assertHookOperandOrder(hook, Opcodes.DLOAD);
        }
        assertNoLivingEntityTypeInstructions(method);
    }

    @Test
    void ignoresOldLivingEntityHookForObjectHookIdempotence() {
        String owner = "test/LegacyStaticHealthHookFixture";
        assertTrue(SporeStaticHealthMethodRegistry.register(
                owner,
                "readLegacy",
                OBTAIN_SERA_DESC,
                List.of(0)
        ));

        byte[] transformed = new SporeStaticHealthMethodTransformer().transformClassByte(
                getClass().getClassLoader(),
                owner,
                legacyHookFixture(owner)
        );

        assertNotNull(transformed);
        MethodNode method = methodNode(transformed, "readLegacy", OBTAIN_SERA_DESC);
        assertEquals(1, objectHooks(method).size());
        assertEquals(1, hookCount(method, "(L" + LIVING_ENTITY_INTERNAL + ";F)F"));
        assertNull(new SporeStaticHealthMethodTransformer().transformClassByte(
                getClass().getClassLoader(),
                owner,
                transformed
        ));
    }

    @Test
    void onlyTransformsRegisteredEligibleStaticMethods() {
        String owner = "test/RegistryEligibilityStaticHealthFixture";
        String objectFloat = "(Ljava/lang/Object;)F";
        assertTrue(SporeStaticHealthMethodRegistry.register(owner, "target", objectFloat, List.of(0)));
        assertTrue(SporeStaticHealthMethodRegistry.register(owner, "instanceTarget", objectFloat, List.of(0)));
        assertTrue(SporeStaticHealthMethodRegistry.register(owner, "abstractTarget", objectFloat, List.of(0)));
        assertTrue(SporeStaticHealthMethodRegistry.register(owner, "nativeTarget", objectFloat, List.of(0)));
        assertTrue(SporeStaticHealthMethodRegistry.register(owner, "primitiveTarget", "(I)F", List.of(0)));
        assertTrue(SporeStaticHealthMethodRegistry.register(owner, "outOfRangeTarget", objectFloat, List.of(1)));
        assertTrue(SporeStaticHealthMethodRegistry.register(owner, "wrongReturn", "(Ljava/lang/Object;)I", List.of(0)));

        byte[] transformed = new SporeStaticHealthMethodTransformer().transformClassByte(
                getClass().getClassLoader(),
                owner,
                eligibilityFixture(owner)
        );

        assertNotNull(transformed);
        assertEquals(1, objectHooks(methodNode(transformed, "target", objectFloat)).size());
        assertEquals(0, objectHooks(methodNode(transformed, "unregistered", objectFloat)).size());
        assertEquals(0, objectHooks(methodNode(transformed, "instanceTarget", objectFloat)).size());
        assertEquals(0, objectHooks(methodNode(transformed, "abstractTarget", objectFloat)).size());
        assertEquals(0, objectHooks(methodNode(transformed, "nativeTarget", objectFloat)).size());
        assertEquals(0, objectHooks(methodNode(transformed, "primitiveTarget", "(I)F")).size());
        assertEquals(0, objectHooks(methodNode(transformed, "outOfRangeTarget", objectFloat)).size());
        assertEquals(0, objectHooks(methodNode(transformed, "wrongReturn", "(Ljava/lang/Object;)I")).size());
    }

    @Test
    void transformsRealMonsterManagerObtainSeraObjectMethod() throws Exception {
        Path jarPath = Path.of("libs", "astralrail_cube-2.1.jar");
        assumeTrue(Files.isRegularFile(jarPath), "Missing verification jar " + jarPath.toAbsolutePath());
        assertTrue(SporeStaticHealthMethodRegistry.register(
                MONSTER_MANAGER,
                OBTAIN_SERA,
                OBTAIN_SERA_DESC,
                List.of(0)
        ));

        byte[] original = classBytes(jarPath, MONSTER_MANAGER);
        try (URLClassLoader loader = new URLClassLoader(
                new java.net.URL[]{jarPath.toUri().toURL()},
                getClass().getClassLoader())) {
            SporeStaticHealthMethodTransformer transformer = new SporeStaticHealthMethodTransformer();
            byte[] transformed = transformer.transformClassByte(loader, MONSTER_MANAGER, original);

            assertNotNull(transformed);
            MethodNode method = methodNode(transformed, OBTAIN_SERA, OBTAIN_SERA_DESC);
            List<MethodInsnNode> hooks = objectHooks(method);
            assertEquals(1, hooks.size());
            MethodInsnNode hook = hooks.get(0);
            assertEquals(FLOAT_OBJECT_HOOK_DESC, hook.desc);
            assertHookOperandOrder(hook, Opcodes.FLOAD);
            assertEquals(0, ((VarInsnNode) previousReal(hook)).var);
            assertNoLivingEntityTypeInstructions(method);
            assertNull(transformer.transformClassByte(loader, MONSTER_MANAGER, transformed));

            Path dump = Path.of("build", "test-results", "spore-static-health")
                    .resolve(MONSTER_MANAGER + ".class");
            Files.createDirectories(dump.getParent());
            Files.write(dump, transformed);
        }
    }

    @Test
    void transformsRegisteredMethodRegardlessOfOwnerWhitelist() {
        assertTrue(SporeStaticHealthMethodRegistry.register(
                WHITE_LISTED_FIXTURE,
                "readFloat",
                FLOAT_DESC,
                List.of(0)
        ));

        SporeStaticHealthMethodTransformer transformer = new SporeStaticHealthMethodTransformer();
        byte[] transformed = transformer.transformClassByte(
                getClass().getClassLoader(),
                "bad/mod/DisguisedRequestName",
                fixtureBytes(WHITE_LISTED_FIXTURE)
        );
        assertNotNull(transformed);
        assertEquals(2, objectHooks(methodNode(transformed, "readFloat", FLOAT_DESC)).size());
        assertEquals(0, objectHooks(methodNode(transformed, "readDouble", DOUBLE_DESC)).size());
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

    private byte[] singleReturnFixture(String owner,
                                       String methodName,
                                       String descriptor,
                                       int valueOpcode,
                                       int returnOpcode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, owner, null, "java/lang/Object", null);
        MethodVisitor method = writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                methodName,
                descriptor,
                null,
                null
        );
        method.visitCode();
        method.visitInsn(valueOpcode);
        method.visitInsn(returnOpcode);
        method.visitMaxs(0, 0);
        method.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private byte[] legacyHookFixture(String owner) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, owner, null, "java/lang/Object", null);
        MethodVisitor method = writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "readLegacy",
                OBTAIN_SERA_DESC,
                null,
                null
        );
        method.visitCode();
        method.visitFieldInsn(Opcodes.GETSTATIC, HOOK_OWNER, "INSTANCE", "L" + HOOK_INTERFACE + ";");
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.FCONST_0);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                HOOK_INTERFACE,
                "getHeealth",
                "(L" + LIVING_ENTITY_INTERNAL + ";F)F",
                true
        );
        method.visitInsn(Opcodes.POP);
        method.visitInsn(Opcodes.FCONST_1);
        method.visitInsn(Opcodes.FRETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private byte[] eligibilityFixture(String owner) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, owner, null, "java/lang/Object", null);
        emitConstantMethod(writer, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "target", "(Ljava/lang/Object;)F", Opcodes.FCONST_1, Opcodes.FRETURN);
        emitConstantMethod(writer, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "unregistered", "(Ljava/lang/Object;)F", Opcodes.FCONST_1, Opcodes.FRETURN);
        emitConstantMethod(writer, Opcodes.ACC_PUBLIC, "instanceTarget", "(Ljava/lang/Object;)F", Opcodes.FCONST_1, Opcodes.FRETURN);
        writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT,
                "abstractTarget",
                "(Ljava/lang/Object;)F",
                null,
                null
        ).visitEnd();
        writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
                "nativeTarget",
                "(Ljava/lang/Object;)F",
                null,
                null
        ).visitEnd();
        emitConstantMethod(writer, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "primitiveTarget", "(I)F", Opcodes.FCONST_1, Opcodes.FRETURN);
        emitConstantMethod(writer, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "outOfRangeTarget", "(Ljava/lang/Object;)F", Opcodes.FCONST_1, Opcodes.FRETURN);
        emitConstantMethod(writer, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "wrongReturn", "(Ljava/lang/Object;)I", Opcodes.ICONST_1, Opcodes.IRETURN);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private void emitConstantMethod(ClassWriter writer,
                                    int access,
                                    String name,
                                    String descriptor,
                                    int valueOpcode,
                                    int returnOpcode) {
        MethodVisitor method = writer.visitMethod(access, name, descriptor, null, null);
        method.visitCode();
        method.visitInsn(valueOpcode);
        method.visitInsn(returnOpcode);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private byte[] classBytes(Path jarPath, String owner) throws Exception {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry entry = jar.getJarEntry(owner + ".class");
            assertNotNull(entry, "Missing class " + owner + " in " + jarPath);
            try (InputStream input = jar.getInputStream(entry)) {
                return input.readAllBytes();
            }
        }
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
                        && HOOK_INTERFACE.equals(call.owner)
                        && "getHeealth".equals(call.name)) {
                    count++;
                }
            }
            result.put(method.name + method.desc, count);
        }
        return result;
    }

    private void assertHookDescriptors(byte[] bytes,
                                       String methodName,
                                       String descriptor,
                                       List<String> expected) {
        List<String> actual = objectHooks(methodNode(bytes, methodName, descriptor)).stream()
                .map(call -> call.desc)
                .toList();
        assertEquals(expected, actual);
    }

    private MethodNode methodNode(byte[] bytes, String name, String descriptor) {
        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode.methods.stream()
                .filter(method -> name.equals(method.name) && descriptor.equals(method.desc))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing method " + name + descriptor));
    }

    private List<MethodInsnNode> objectHooks(MethodNode method) {
        List<MethodInsnNode> result = new ArrayList<>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode call
                    && call.getOpcode() == Opcodes.INVOKEINTERFACE
                    && HOOK_INTERFACE.equals(call.owner)
                    && "getHeealth".equals(call.name)
                    && (FLOAT_OBJECT_HOOK_DESC.equals(call.desc)
                    || DOUBLE_OBJECT_HOOK_DESC.equals(call.desc))) {
                result.add(call);
            }
        }
        return result;
    }

    private int hookCount(MethodNode method, String descriptor) {
        int count = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode call
                    && HOOK_INTERFACE.equals(call.owner)
                    && "getHeealth".equals(call.name)
                    && descriptor.equals(call.desc)) {
                count++;
            }
        }
        return count;
    }

    private List<Integer> candidateSlots(List<MethodInsnNode> hooks) {
        return hooks.stream()
                .map(hook -> ((VarInsnNode) previousReal(hook)).var)
                .toList();
    }

    private void assertHookOperandOrder(MethodInsnNode hook, int resultLoadOpcode) {
        AbstractInsnNode candidate = previousReal(hook);
        assertTrue(candidate instanceof VarInsnNode);
        assertEquals(Opcodes.ALOAD, candidate.getOpcode());

        AbstractInsnNode result = previousReal(candidate);
        assertTrue(result instanceof VarInsnNode);
        assertEquals(resultLoadOpcode, result.getOpcode());
        int resultLocal = ((VarInsnNode) result).var;

        AbstractInsnNode manager = previousReal(result);
        assertTrue(manager instanceof FieldInsnNode);
        assertEquals(Opcodes.GETSTATIC, manager.getOpcode());
        FieldInsnNode field = (FieldInsnNode) manager;
        assertEquals(HOOK_OWNER, field.owner);
        assertEquals("INSTANCE", field.name);

        AbstractInsnNode storedResult = nextReal(hook);
        assertTrue(storedResult instanceof VarInsnNode);
        assertEquals(resultLoadOpcode == Opcodes.FLOAD ? Opcodes.FSTORE : Opcodes.DSTORE,
                storedResult.getOpcode());
        assertEquals(resultLocal, ((VarInsnNode) storedResult).var);
    }

    private AbstractInsnNode previousReal(AbstractInsnNode instruction) {
        AbstractInsnNode current = instruction.getPrevious();
        while (current != null && current.getOpcode() < 0) {
            current = current.getPrevious();
        }
        return current;
    }

    private AbstractInsnNode nextReal(AbstractInsnNode instruction) {
        AbstractInsnNode current = instruction.getNext();
        while (current != null && current.getOpcode() < 0) {
            current = current.getNext();
        }
        return current;
    }

    private void assertNoLivingEntityTypeInstructions(byte[] bytes, String name, String descriptor) {
        assertNoLivingEntityTypeInstructions(methodNode(bytes, name, descriptor));
    }

    private void assertNoLivingEntityTypeInstructions(MethodNode method) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof TypeInsnNode typeInstruction
                    && (instruction.getOpcode() == Opcodes.INSTANCEOF
                    || instruction.getOpcode() == Opcodes.CHECKCAST)) {
                assertTrue(!LIVING_ENTITY_INTERNAL.equals(typeInstruction.desc),
                        "Unexpected LivingEntity type instruction in " + method.name + method.desc);
            }
        }
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
