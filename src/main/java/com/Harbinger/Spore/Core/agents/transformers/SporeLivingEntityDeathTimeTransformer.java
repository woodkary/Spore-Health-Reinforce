package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;
import java.util.HashSet;
import java.util.Set;

public final class SporeLivingEntityDeathTimeTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String LIVING_ENTITY_INTERNAL = "net/minecraft/world/entity/LivingEntity";
    private static final String DEATH_TIME_NAME = "deathTime";
    private static final String DEATH_TIME_OBF_NAME = "f_20919_";
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/CustomDeathTimeManager";
    private static final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/asmHooks/IDeathTimeManager";
    private static final String HOOK_METHOD = "deathTimeGetFieldHook";
    private static final String HOOK_DESC = "(L" + LIVING_ENTITY_INTERNAL + ";I)I";
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS =
            (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeLivingEntityDeathTimeTransformer.class
            );
    private static MethodHandle constructor;

    static {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeLivingEntityDeathTimeTransformer.class
        );
    }

    public static SelfTransformer newSelfTransformer() {
        ClassFileTransformer transformer = newInstance();
        if (transformer instanceof SelfTransformer selfTransformer) {
            return selfTransformer;
        }
        return new SporeLivingEntityDeathTimeTransformer();
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeLivingEntityDeathTimeTransformer.class
        );
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeLivingEntityDeathTimeTransformer, %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
        return new SporeLivingEntityDeathTimeTransformer();
    }

    public SporeLivingEntityDeathTimeTransformer() {
    }

    @Override
    protected byte[] transformInternal(ClassLoader loader, String className, byte[] classfileBuffer) {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return null;
        }
        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            if (classNode.name == null || classNode.superName == null) {
                return null;
            }
            String effectiveClassName = className == null ? classNode.name : className;
            cacheSuperName(classNode.name, classNode.superName);
            if (!isSubClass(classNode, loader, LIVING_ENTITY_INTERNAL)) {
                return null;
            }
            int transformedReads = patchDeathTimeReads(classNode, loader);
            if (transformedReads > 0) {
                LogUtil.logf("Transformed %d LivingEntity deathTime reads in %s", transformedReads, classNode.name);
                return toBytes(loader, effectiveClassName, classfileBuffer, classNode);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform LivingEntity deathTime reads of %s, %s",
                    className == null ? "<hidden-or-anonymous>" : className,
                    t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private int patchDeathTimeReads(ClassNode classNode, ClassLoader loader) {
        int transformed = 0;
        for (MethodNode method : classNode.methods) {
            if (method == null || method.instructions == null
                    || (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
                AbstractInsnNode next = insn.getNext();
                if (insn instanceof FieldInsnNode fieldInsn
                        && isDeathTimeRead(fieldInsn)
                        && isLivingEntityFieldOwner(fieldInsn.owner, classNode, loader)
                        && !alreadyCallsHook(fieldInsn)) {
                    injectHook(method, fieldInsn);
                    transformed++;
                }
                insn = next;
            }
        }
        return transformed;
    }

    private boolean isDeathTimeRead(FieldInsnNode fieldInsn) {
        return fieldInsn.getOpcode() == Opcodes.GETFIELD
                && "I".equals(fieldInsn.desc)
                && (DEATH_TIME_OBF_NAME.equals(fieldInsn.name) || DEATH_TIME_NAME.equals(fieldInsn.name));
    }

    private boolean isLivingEntityFieldOwner(String owner, ClassNode currentClass, ClassLoader loader) {
        if (LIVING_ENTITY_INTERNAL.equals(owner)) {
            return true;
        }
        if (currentClass.name.equals(owner)) {
            return !declaresDeathTimeField(currentClass);
        }
        ClassNode ownerNode = tryLoadClassNodeFromLoader(loader, owner);
        return ownerNode != null
                && !declaresDeathTimeField(ownerNode)
                && isSubClass(ownerNode, loader, LIVING_ENTITY_INTERNAL);
    }

    private boolean declaresDeathTimeField(ClassNode classNode) {
        for (FieldNode field : classNode.fields) {
            if ("I".equals(field.desc)
                    && (DEATH_TIME_OBF_NAME.equals(field.name) || DEATH_TIME_NAME.equals(field.name))) {
                return true;
            }
        }
        return false;
    }

    private boolean alreadyCallsHook(FieldInsnNode fieldInsn) {
        AbstractInsnNode next = fieldInsn.getNext();
        return next instanceof MethodInsnNode methodInsn
                && methodInsn.getOpcode() == Opcodes.INVOKEINTERFACE
                && HOOK_INTERFACE.equals(methodInsn.owner)
                && HOOK_METHOD.equals(methodInsn.name)
                && HOOK_DESC.equals(methodInsn.desc);
    }

    private void injectHook(MethodNode method, FieldInsnNode fieldInsn) {
        InsnList prefix = new InsnList();
        prefix.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                HOOK_OWNER,
                "INSTANCE",
                "L" + HOOK_INTERFACE + ";"
        ));
        prefix.add(new InsnNode(Opcodes.SWAP));
        prefix.add(new InsnNode(Opcodes.DUP));
        method.instructions.insertBefore(fieldInsn, prefix);
        method.instructions.insert(fieldInsn, new MethodInsnNode(
                Opcodes.INVOKEINTERFACE,
                HOOK_INTERFACE,
                HOOK_METHOD,
                HOOK_DESC,
                true
        ));
    }

    private boolean isSubClass(ClassNode classNode, ClassLoader loader, String targetInternalName) {
        if (targetInternalName.equals(classNode.name)) {
            return true;
        }
        String superName = classNode.superName;
        Set<String> visited = new HashSet<>();
        while (superName != null && !"java/lang/Object".equals(superName) && visited.add(superName)) {
            if (targetInternalName.equals(superName)) {
                return true;
            }
            String cached = superNameCache.get(superName);
            if (cached != null) {
                superName = cached;
                continue;
            }
            ClassNode parent = tryLoadClassNodeFromLoader(loader, superName);
            if (parent == null) {
                return false;
            }
            if (parent.superName != null) {
                cacheSuperName(parent.name, parent.superName);
            }
            superName = parent.superName;
        }
        return false;
    }

    private ClassNode tryLoadClassNodeFromLoader(ClassLoader loader, String internalName) {
        if (internalName == null) {
            return null;
        }
        String resource = internalName + ".class";
        InputStream input = null;
        try {
            if (loader != null) {
                input = loader.getResourceAsStream(resource);
            }
            if (input == null) {
                input = ClassLoader.getSystemResourceAsStream(resource);
            }
            if (input == null) {
                input = SporeLivingEntityDeathTimeTransformer.class.getClassLoader().getResourceAsStream(resource);
            }
            if (input == null) {
                return null;
            }
            try (InputStream closeable = input) {
                ClassReader reader = new ClassReader(closeable);
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.SKIP_FRAMES);
                return node;
            }
        } catch (IOException e) {
            return null;
        } catch (Throwable t) {
            LogUtil.errorf("failed to inspect parent class %s, %s", internalName, t.getMessage());
            return null;
        }
    }

    private byte[] toBytes(ClassLoader loader,
                           String className,
                           byte[] inputBytes,
                           ClassNode classNode) {
        ClassWriter writer = new SporeFrameClassWriter(loader, classNode, ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        byte[] transformed = writer.toByteArray();
        SporeTransformerDebugDump.rememberTransformed(
                getClass().getName(),
                className,
                classNode.name,
                inputBytes,
                transformed
        );
        return transformed;
    }

    @Override
    public byte[] transformClassByte(ClassLoader loader, String className, byte[] classfileBuffer) {
        return transformInternal(loader, className, classfileBuffer);
    }
}
