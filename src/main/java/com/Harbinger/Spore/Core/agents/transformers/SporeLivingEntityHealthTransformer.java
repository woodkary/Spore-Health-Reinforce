package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SporeLivingEntityHealthTransformer extends SporeClassFileTransformer0 {
    private static final String LIVING_ENTITY_INTERNAL = "net/minecraft/world/entity/LivingEntity";
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/SporeEntityHeeaafastthManager";
    private static final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/asmHooks/ISporeEntityHealth";
    private static final String INIT_METHOD = "initSporeEntity";
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS =
            (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeLivingEntityHealthTransformer.class
            );
    private static MethodHandle constructor;

    static {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeLivingEntityHealthTransformer.class
        );
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeLivingEntityHealthTransformer.class
        );
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeLivingEntityHealthTransformer, %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
        return new SporeLivingEntityHealthTransformer();
    }

    public SporeLivingEntityHealthTransformer() {
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
            if (!isLivingEntityOrSubclass(classNode, loader)) {
                return null;
            }
            if (transformLivingEntity(classNode)) {
                return toBytes(classNode);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform %s, %s", className, t.getMessage());
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
                input = SporeLivingEntityHealthTransformer.class.getClassLoader().getResourceAsStream(resource);
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

    private boolean transformLivingEntity(ClassNode classNode) {
        boolean modified = patchConstructors(classNode);
        List<MethodNode> methods = classNode.methods;
        for (MethodNode method : methods) {
            if (!canPatch(method) || alreadyCallsHook(method)) {
                continue;
            }
            HookKind hookKind = resolveHookKind(method);
            if (hookKind == HookKind.NONE) {
                continue;
            }
            boolean changed;
            if (hookKind == HookKind.HEALTH || hookKind == HookKind.MAX_HEALTH) {
                changed = patchFloatOrDoubleReturnMethod(method, hookKind);
            } else {
                changed = patchBooleanReturnMethod(method, hookKind);
            }
            if (changed) {
                modified = true;
                LogUtil.logf("Transformed LivingEntity health method %s.%s%s",
                        classNode.name,
                        method.name,
                        method.desc);
            }
        }
        return modified;
    }

    private boolean patchConstructors(ClassNode classNode) {
        boolean modified = false;
        List<MethodNode> methods = classNode.methods;
        for (MethodNode method : methods) {
            if (!"<init>".equals(method.name) || method.instructions == null || constructorAlreadyCallsInit(method)) {
                continue;
            }
            boolean methodModified = false;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
                AbstractInsnNode next = insn.getNext();
                if (insn.getOpcode() == Opcodes.RETURN) {
                    InsnList inject = new InsnList();
                    inject.add(new FieldInsnNode(
                            Opcodes.GETSTATIC,
                            HOOK_OWNER,
                            "INSTANCE",
                            "L" + HOOK_INTERFACE + ";"
                    ));
                    inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    inject.add(new MethodInsnNode(
                            Opcodes.INVOKEINTERFACE,
                            HOOK_INTERFACE,
                            INIT_METHOD,
                            "(L" + LIVING_ENTITY_INTERNAL + ";)V",
                            true
                    ));
                    method.instructions.insertBefore(insn, inject);
                    methodModified = true;
                }
                insn = next;
            }
            if (methodModified) {
                modified = true;
                LogUtil.logf("Transformed LivingEntity constructor %s.%s%s",
                        classNode.name,
                        method.name,
                        method.desc);
            }
        }
        return modified;
    }

    private boolean constructorAlreadyCallsInit(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode methodInsn
                    && HOOK_INTERFACE.equals(methodInsn.owner)
                    && INIT_METHOD.equals(methodInsn.name)) {
                return true;
            }
        }
        return false;
    }

    private boolean canPatch(MethodNode method) {
        if (method == null || method.instructions == null) {
            return false;
        }
        if ("<init>".equals(method.name) || "<clinit>".equals(method.name)) {
            return false;
        }
        if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
            return false;
        }
        return !method.name.contains("checkLivingFleshSize");
    }

    private boolean alreadyCallsHook(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode methodInsn && HOOK_INTERFACE.equals(methodInsn.owner)) {
                return true;
            }
            if (insn instanceof FieldInsnNode fieldInsn && HOOK_OWNER.equals(fieldInsn.owner)) {
                return true;
            }
        }
        return false;
    }

    private HookKind resolveHookKind(MethodNode method) {
        Type ret = Type.getReturnType(method.desc);
        int sort = ret.getSort();
        if (sort == Type.FLOAT || sort == Type.DOUBLE) {
            if (nameLooksLikeMaxHealth(method.name)) {
                return HookKind.MAX_HEALTH;
            }
            if (nameLooksLikeHealth(method.name)) {
                return HookKind.HEALTH;
            }
        }
        if (sort == Type.BOOLEAN) {
            if (nameLooksLikeIsDeadOrDying(method.name)) {
                return HookKind.DEAD_OR_DYING;
            }
            if (nameLooksLikeIsAlive(method.name)) {
                return HookKind.ALIVE;
            }
        }
        return HookKind.NONE;
    }

    private boolean patchBooleanReturnMethod(MethodNode method, HookKind hookKind) {
        boolean modified = false;
        int tmpIndex = allocateTempLocal(method, Type.INT_TYPE);
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
            AbstractInsnNode next = insn.getNext();
            if (insn.getOpcode() == Opcodes.IRETURN) {
                InsnList inject = new InsnList();
                inject.add(new VarInsnNode(Opcodes.ISTORE, tmpIndex));
                inject.add(new FieldInsnNode(
                        Opcodes.GETSTATIC,
                        HOOK_OWNER,
                        "INSTANCE",
                        "L" + HOOK_INTERFACE + ";"
                ));
                inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                inject.add(new VarInsnNode(Opcodes.ILOAD, tmpIndex));
                inject.add(new MethodInsnNode(
                        Opcodes.INVOKEINTERFACE,
                        HOOK_INTERFACE,
                        hookKind == HookKind.DEAD_OR_DYING ? "isDeeadfOrDyaging" : "isAlliive",
                        "(L" + LIVING_ENTITY_INTERNAL + ";Z)Z",
                        true
                ));
                method.instructions.insertBefore(insn, inject);
                modified = true;
            }
            insn = next;
        }
        return modified;
    }

    private boolean patchFloatOrDoubleReturnMethod(MethodNode method, HookKind hookKind) {
        Type ret = Type.getReturnType(method.desc);
        boolean isFloat = ret.getSort() == Type.FLOAT;
        int retLocal = allocateTempLocal(method, ret);
        int returnOpcode = isFloat ? Opcodes.FRETURN : Opcodes.DRETURN;
        int storeOpcode = isFloat ? Opcodes.FSTORE : Opcodes.DSTORE;
        int loadOpcode = isFloat ? Opcodes.FLOAD : Opcodes.DLOAD;
        String desc = isFloat
                ? "(L" + LIVING_ENTITY_INTERNAL + ";F)F"
                : "(L" + LIVING_ENTITY_INTERNAL + ";D)D";
        boolean modified = false;

        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
            AbstractInsnNode next = insn.getNext();
            if (insn.getOpcode() == returnOpcode) {
                InsnList inject = new InsnList();
                inject.add(new VarInsnNode(storeOpcode, retLocal));
                inject.add(new FieldInsnNode(
                        Opcodes.GETSTATIC,
                        HOOK_OWNER,
                        "INSTANCE",
                        "L" + HOOK_INTERFACE + ";"
                ));
                inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                inject.add(new VarInsnNode(loadOpcode, retLocal));
                inject.add(new MethodInsnNode(
                        Opcodes.INVOKEINTERFACE,
                        HOOK_INTERFACE,
                        hookKind == HookKind.MAX_HEALTH ? "getMaaxxHeaaltsh" : "getHeealth",
                        desc,
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

    private boolean nameLooksLikeHealth(String name) {
        if ("haveDiexv".equals(name)) {
            return true;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return (n.contains("heal") && !n.contains("max")) || "m_21223_".equals(name);
    }

    private boolean nameLooksLikeMaxHealth(String name) {
        if ("haveBigDiexv".equals(name)) {
            return true;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return (n.contains("max") && n.contains("heal")) || "m_21233_".equals(name);
    }

    private boolean nameLooksLikeIsDeadOrDying(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("dead")
                || n.contains("die")
                || n.contains("death")
                || n.contains("away")
                || n.contains("died")
                || (n.contains("kill") && !n.contains("skill"))
                || n.contains("weak")
                || (n.contains("end") && !n.contains("render") && !n.contains("legend"))
                || "m_21224_".equals(name);
    }

    private boolean nameLooksLikeIsAlive(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("alive") || n.contains("living") || "m_6084_".equals(name);
    }

    private byte[] toBytes(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private enum HookKind {
        NONE,
        HEALTH,
        MAX_HEALTH,
        DEAD_OR_DYING,
        ALIVE
    }
}
