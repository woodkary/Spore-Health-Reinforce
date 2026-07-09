package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SporeLivingEntityEffectApplicationTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String LIVING_ENTITY_INTERNAL = "net/minecraft/world/entity/LivingEntity";
    private static final String MOB_EFFECT_INSTANCE_INTERNAL = "net/minecraft/world/effect/MobEffectInstance";
    private static final String ENTITY_INTERNAL = "net/minecraft/world/entity/Entity";
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/utils/effects/SporeEffectsUtil";
    private static final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/utils/effects/IEffectManager";
    private static final String CHECK_AND_ADD_EFFECT = "checkAndAddEffect";
    private static final String CHECK_AND_ADD_DESC = "(L" + LIVING_ENTITY_INTERNAL + ";L" + MOB_EFFECT_INSTANCE_INTERNAL + ";L" + ENTITY_INTERNAL + ";)Z";
    private static final String MOB_EFFECT_INTERNAL = "net/minecraft/world/effect/MobEffect";
    private static final String COLLECTION_INTERNAL = "java/util/Collection";
    private static final String MAP_INTERNAL = "java/util/Map";
    private static final String ADD_EFFECT_DESC = "(L" + MOB_EFFECT_INSTANCE_INTERNAL + ";L" + ENTITY_INTERNAL + ";)Z";
    private static final String FORCE_ADD_EFFECT_DESC = "(L" + MOB_EFFECT_INSTANCE_INTERNAL + ";L" + ENTITY_INTERNAL + ";)V";
    private static final String GET_ACTIVE_EFFECTS_DESC = "()L" + COLLECTION_INTERNAL + ";";
    private static final String GET_ACTIVE_EFFECTS_MAP_DESC = "()L" + MAP_INTERNAL + ";";
    private static final String HAS_EFFECT_DESC = "(L" + MOB_EFFECT_INTERNAL + ";)Z";
    private static final String GET_EFFECT_DESC = "(L" + MOB_EFFECT_INTERNAL + ";)L" + MOB_EFFECT_INSTANCE_INTERNAL + ";";
    private static final String CAN_BE_AFFECTED_DESC = "(L" + MOB_EFFECT_INSTANCE_INTERNAL + ";)Z";
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
    public static SelfTransformer newSelfTransformer(){
        ClassFileTransformer res=newInstance();
        if(res instanceof SelfTransformer selfTransformer){
            return selfTransformer;
        }
        return new SporeLivingEntityEffectApplicationTransformer();
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
            if (kind == EffectMethodKind.NONE || !canPatch(method) || alreadyCallsHook(method, kind)) {
                continue;
            }
            if (patchMethod(classNode, method, kind)) {
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

    private boolean alreadyCallsHook(MethodNode method, EffectMethodKind kind) {
        String hookMethodName = kind.hookMethodName;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode fieldInsn && HOOK_OWNER.equals(fieldInsn.owner)) {
                return true;
            }
            if (insn instanceof MethodInsnNode methodInsn
                    && HOOK_INTERFACE.equals(methodInsn.owner)
                    && hookMethodName.equals(methodInsn.name)) {
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
        if (GET_ACTIVE_EFFECTS_DESC.equals(method.desc) && ("m_21220_".equals(method.name) || "getActiveEffects".equals(method.name))) {
            return EffectMethodKind.GET_ACTIVE_EFFECTS;
        }
        if (GET_ACTIVE_EFFECTS_MAP_DESC.equals(method.desc) && ("m_21221_".equals(method.name) || "getActiveEffectsMap".equals(method.name))) {
            return EffectMethodKind.GET_ACTIVE_EFFECTS_MAP;
        }
        if (HAS_EFFECT_DESC.equals(method.desc) && ("m_21023_".equals(method.name) || "hasEffect".equals(method.name))) {
            return EffectMethodKind.HAS_EFFECT;
        }
        if (GET_EFFECT_DESC.equals(method.desc) && ("m_21124_".equals(method.name) || "getEffect".equals(method.name))) {
            return EffectMethodKind.GET_EFFECT;
        }
        if (CAN_BE_AFFECTED_DESC.equals(method.desc) && ("m_7301_".equals(method.name) || "canBeAffected".equals(method.name))) {
            return EffectMethodKind.CAN_BE_AFFECTED;
        }
        return EffectMethodKind.NONE;
    }

    private boolean patchMethod(ClassNode classNode, MethodNode method, EffectMethodKind kind) {
        if (kind == EffectMethodKind.ADD_EFFECT || kind == EffectMethodKind.FORCE_ADD_EFFECT) {
            return patchMethodStart(classNode.name, method, kind);
        }
        return patchReturnHook(method, kind);
    }

    private boolean patchMethodStart(String classInternalName, MethodNode method, EffectMethodKind kind) {
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
        List<Object> locals = createInitialFrameLocals(classInternalName, method);
        inject.add(new FrameNode(Opcodes.F_NEW, locals.size(), locals.toArray(), 0, null));
        method.instructions.insertBefore(firstRealInsn, inject);
        return true;
    }

    private boolean patchReturnHook(MethodNode method, EffectMethodKind kind) {
        boolean modified = false;
        int retLocal = allocateTempLocal(method, kind.returnType);
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
            AbstractInsnNode next = insn.getNext();
            if (insn.getOpcode() == kind.returnOpcode) {
                InsnList inject = new InsnList();
                inject.add(new VarInsnNode(kind.storeOpcode, retLocal));
                inject.add(new FieldInsnNode(
                        Opcodes.GETSTATIC,
                        HOOK_OWNER,
                        "INSTANCE",
                        "L" + HOOK_INTERFACE + ";"
                ));
                inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                if (kind.loadsEffectArgument) {
                    inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                }
                inject.add(new VarInsnNode(kind.loadOpcode, retLocal));
                inject.add(new MethodInsnNode(
                        Opcodes.INVOKEINTERFACE,
                        HOOK_INTERFACE,
                        kind.hookMethodName,
                        kind.hookDesc,
                        true
                ));
                method.instructions.insertBefore(insn, inject);
                modified = true;
            }
            insn = next;
        }
        return modified;
    }

    private int allocateTempLocal(MethodNode method, Type type) {
        int index = Math.max(method.maxLocals, minLocalsFromDesc(method));
        method.maxLocals = index + type.getSize();
        return index;
    }

    private int minLocalsFromDesc(MethodNode method) {
        int locals = (method.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        Type[] args = Type.getArgumentTypes(method.desc);
        for (Type arg : args) {
            locals += arg.getSize();
        }
        return locals;
    }

    private List<Object> createInitialFrameLocals(String classInternalName, MethodNode method) {
        List<Object> locals = new ArrayList<>();
        if ((method.access & Opcodes.ACC_STATIC) == 0) {
            locals.add(classInternalName);
        }
        for (Type arg : Type.getArgumentTypes(method.desc)) {
            switch (arg.getSort()) {
                case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> locals.add(Opcodes.INTEGER);
                case Type.FLOAT -> locals.add(Opcodes.FLOAT);
                case Type.LONG -> locals.add(Opcodes.LONG);
                case Type.DOUBLE -> locals.add(Opcodes.DOUBLE);
                case Type.ARRAY -> locals.add(arg.getDescriptor());
                case Type.OBJECT -> locals.add(arg.getInternalName());
                default -> {
                }
            }
        }
        return locals;
    }

    private AbstractInsnNode findFirstRealInstruction(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode || insn instanceof LineNumberNode || insn.getType() == AbstractInsnNode.FRAME) {
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

    @Override
    public byte[] transformClassByte(ClassLoader loader, String className, byte[] classfileBuffer) {
        return transformInternal(loader,className,classfileBuffer);
    }

    private enum EffectMethodKind {
        NONE(null, null, null, -1, -1, -1, false),
        ADD_EFFECT(CHECK_AND_ADD_EFFECT, CHECK_AND_ADD_DESC, Type.BOOLEAN_TYPE, Opcodes.IRETURN, Opcodes.ISTORE, Opcodes.ILOAD, false),
        FORCE_ADD_EFFECT(CHECK_AND_ADD_EFFECT, CHECK_AND_ADD_DESC, Type.VOID_TYPE, Opcodes.RETURN, -1, -1, false),
        GET_ACTIVE_EFFECTS(
                "getActiveEffectsHook",
                "(L" + LIVING_ENTITY_INTERNAL + ";L" + COLLECTION_INTERNAL + ";)L" + COLLECTION_INTERNAL + ";",
                Type.getObjectType(COLLECTION_INTERNAL),
                Opcodes.ARETURN,
                Opcodes.ASTORE,
                Opcodes.ALOAD,
                false
        ),
        GET_ACTIVE_EFFECTS_MAP(
                "getActiveEffectsMapHook",
                "(L" + LIVING_ENTITY_INTERNAL + ";L" + MAP_INTERNAL + ";)L" + MAP_INTERNAL + ";",
                Type.getObjectType(MAP_INTERNAL),
                Opcodes.ARETURN,
                Opcodes.ASTORE,
                Opcodes.ALOAD,
                false
        ),
        HAS_EFFECT(
                "hasEffectHook",
                "(L" + LIVING_ENTITY_INTERNAL + ";L" + MOB_EFFECT_INTERNAL + ";Z)Z",
                Type.BOOLEAN_TYPE,
                Opcodes.IRETURN,
                Opcodes.ISTORE,
                Opcodes.ILOAD,
                true
        ),
        GET_EFFECT(
                "getEffectHook",
                "(L" + LIVING_ENTITY_INTERNAL + ";L" + MOB_EFFECT_INTERNAL + ";L" + MOB_EFFECT_INSTANCE_INTERNAL + ";)L" + MOB_EFFECT_INSTANCE_INTERNAL + ";",
                Type.getObjectType(MOB_EFFECT_INSTANCE_INTERNAL),
                Opcodes.ARETURN,
                Opcodes.ASTORE,
                Opcodes.ALOAD,
                true
        ),
        CAN_BE_AFFECTED(
                "canBeAffectedHook",
                "(L" + LIVING_ENTITY_INTERNAL + ";L" + MOB_EFFECT_INSTANCE_INTERNAL + ";Z)Z",
                Type.BOOLEAN_TYPE,
                Opcodes.IRETURN,
                Opcodes.ISTORE,
                Opcodes.ILOAD,
                true
        );

        private final String hookMethodName;
        private final String hookDesc;
        private final Type returnType;
        private final int returnOpcode;
        private final int storeOpcode;
        private final int loadOpcode;
        private final boolean loadsEffectArgument;

        EffectMethodKind(String hookMethodName,
                         String hookDesc,
                         Type returnType,
                         int returnOpcode,
                         int storeOpcode,
                         int loadOpcode,
                         boolean loadsEffectArgument) {
            this.hookMethodName = hookMethodName;
            this.hookDesc = hookDesc;
            this.returnType = returnType;
            this.returnOpcode = returnOpcode;
            this.storeOpcode = storeOpcode;
            this.loadOpcode = loadOpcode;
            this.loadsEffectArgument = loadsEffectArgument;
        }
    }
}
