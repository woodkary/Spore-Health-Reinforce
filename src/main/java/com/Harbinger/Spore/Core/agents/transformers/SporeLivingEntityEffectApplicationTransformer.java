package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SporeLivingEntityEffectApplicationTransformer extends SporeClassFileTransformer0 {
    private static final String LIVING_ENTITY_INTERNAL = "net/minecraft/world/entity/LivingEntity";
    private static final String MOB_EFFECT_INSTANCE_INTERNAL = "net/minecraft/world/effect/MobEffectInstance";
    private static final String ENTITY_INTERNAL = "net/minecraft/world/entity/Entity";
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/utils/effects/SporeEffectsUtil";
    private static final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/utils/effects/IEffectManager";
    private static final String CHECK_AND_ADD_EFFECT = "checkAndAddEffect";
    private static final String CHECK_AND_ADD_DESC = "(L" + LIVING_ENTITY_INTERNAL + ";L" + MOB_EFFECT_INSTANCE_INTERNAL + ";L" + ENTITY_INTERNAL + ";)Z";
    private static final String ADD_EFFECT_DESC = "(L" + MOB_EFFECT_INSTANCE_INTERNAL + ";L" + ENTITY_INTERNAL + ";)Z";
    private static final String FORCE_ADD_EFFECT_DESC = "(L" + MOB_EFFECT_INSTANCE_INTERNAL + ";L" + ENTITY_INTERNAL + ";)V";
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS =
            (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeLivingEntityEffectApplicationTransformer.class
            );
    private static MethodHandle constructor;

    static {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeLivingEntityEffectApplicationTransformer.class
        );
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeLivingEntityEffectApplicationTransformer.class
        );
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeLivingEntityEffectApplicationTransformer, %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
        return new SporeLivingEntityEffectApplicationTransformer();
    }

    public SporeLivingEntityEffectApplicationTransformer() {
    }

    @Override
    protected byte[] transformInternal(ClassLoader loader, String className, byte[] classfileBuffer) {
        if (className == null || classfileBuffer == null || classfileBuffer.length == 0) {
            return null;
        }
        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            if (classNode.name == null || classNode.superName == null) {
                return null;
            }
            superNameCache.putIfAbsent(classNode.name, classNode.superName);
            if (!StackTraceUtil.isBadModName(classNode.name) || !isLivingEntityOrSubclass(classNode, loader)) {
                return null;
            }
            if (transformEffectApplicationMethods(classNode)) {
                return toBytes(classNode);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform LivingEntity effect application method of %s, %s", className, t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private boolean isLivingEntityOrSubclass(ClassNode classNode, ClassLoader loader) {
        return isSubClass(classNode, loader, LIVING_ENTITY_INTERNAL);
    }

    private boolean isSubClass(ClassNode classNode, ClassLoader loader, String targetInternalName) {
        String current = classNode.name;
        if (targetInternalName.equals(current)) {
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
                superNameCache.putIfAbsent(parent.name, parent.superName);
            }
            superName = parent.superName;
        }
        return false;
    }

    private ClassNode tryLoadClassNodeFromLoader(ClassLoader loader, String internalName) {
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
                input = SporeLivingEntityEffectApplicationTransformer.class.getClassLoader().getResourceAsStream(resource);
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

    private boolean transformEffectApplicationMethods(ClassNode classNode) {
        boolean modified = false;
        List<MethodNode> methods = classNode.methods;
        for (MethodNode method : methods) {
            EffectMethodKind kind = resolveEffectMethodKind(method);
            if (kind == EffectMethodKind.NONE || !canPatch(method) || alreadyCallsHook(method)) {
                continue;
            }
            if (patchMethodStart(method, kind)) {
                modified = true;
                LogUtil.logf("Transformed LivingEntity effect application method %s.%s%s",
                        classNode.name,
                        method.name,
                        method.desc);
            }
        }
        return modified;
    }

    private boolean canPatch(MethodNode method) {
        if (method == null || method.instructions == null) {
            return false;
        }
        if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
            return false;
        }
        return !"<init>".equals(method.name) && !"<clinit>".equals(method.name);
    }

    private boolean alreadyCallsHook(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode fieldInsn && HOOK_OWNER.equals(fieldInsn.owner)) {
                return true;
            }
            if (insn instanceof MethodInsnNode methodInsn
                    && HOOK_INTERFACE.equals(methodInsn.owner)
                    && CHECK_AND_ADD_EFFECT.equals(methodInsn.name)) {
                return true;
            }
        }
        return false;
    }

    private EffectMethodKind resolveEffectMethodKind(MethodNode method) {
        if (method == null) {
            return EffectMethodKind.NONE;
        }
        if (ADD_EFFECT_DESC.equals(method.desc) && ("m_147207_".equals(method.name) || "addEffect".equals(method.name))) {
            return EffectMethodKind.ADD_EFFECT;
        }
        if (FORCE_ADD_EFFECT_DESC.equals(method.desc) && ("m_147215_".equals(method.name) || "forceAddEffect".equals(method.name))) {
            return EffectMethodKind.FORCE_ADD_EFFECT;
        }
        return EffectMethodKind.NONE;
    }

    private boolean patchMethodStart(MethodNode method, EffectMethodKind kind) {
        AbstractInsnNode firstRealInsn = findFirstRealInstruction(method);
        if (firstRealInsn == null) {
            return false;
        }
        LabelNode continueLabel = new LabelNode();
        InsnList inject = new InsnList();
        inject.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                HOOK_OWNER,
                "INSTANCE",
                "L" + HOOK_INTERFACE + ";"
        ));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 2));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEINTERFACE,
                HOOK_INTERFACE,
                CHECK_AND_ADD_EFFECT,
                CHECK_AND_ADD_DESC,
                true
        ));
        inject.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        if (kind == EffectMethodKind.ADD_EFFECT) {
            inject.add(new InsnNode(Opcodes.ICONST_1));
            inject.add(new InsnNode(Opcodes.IRETURN));
        } else {
            inject.add(new InsnNode(Opcodes.RETURN));
        }
        inject.add(continueLabel);
        inject.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insertBefore(firstRealInsn, inject);
        return true;
    }

    private AbstractInsnNode findFirstRealInstruction(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode || insn instanceof LineNumberNode || insn instanceof FrameNode) {
                continue;
            }
            return insn;
        }
        return null;
    }

    private byte[] toBytes(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private enum EffectMethodKind {
        NONE,
        ADD_EFFECT,
        FORCE_ADD_EFFECT
    }
}
