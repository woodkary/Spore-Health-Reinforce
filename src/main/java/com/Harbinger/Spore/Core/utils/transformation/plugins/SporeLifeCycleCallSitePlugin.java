package com.Harbinger.Spore.Core.utils.transformation.plugins;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SporeLifeCycleCallSitePlugin implements ILaunchPluginService {
    private final String SPORE_CLASS_PREFIX = "com/Harbinger/Spore/";
    private final String LIVING_ENTITY_INTERNAL = "net/minecraft/world/entity/LivingEntity";
    private final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/EntityHeealuthManager";
    private final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/asmHooks/IEntityHealth";
    private final String HOOK_INSTANCE_DESC = "L" + HOOK_INTERFACE + ";";

    private final Map<String, String> superNameCache = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "SporeLifeCycleCallSitePlugin";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty, String reason) {
        return handlesClass(classType, isEmpty);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        if (phase != Phase.BEFORE || classNode == null || classNode.name == null) {
            return false;
        }

        cacheSuperName(classNode.name, classNode.superName);
        if (classNode.name.startsWith(SPORE_CLASS_PREFIX)) {
            return false;
        }

        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            if (method == null || method.instructions == null) {
                continue;
            }
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; ) {
                AbstractInsnNode next = instruction.getNext();
                if (instruction instanceof MethodInsnNode call && canWrap(call)) {
                    LifeCycleCallSiteHookSpec hook =
                            LifeCycleCallSiteHookResolver.resolve(call.name, call.desc);
                    if (hook != null
                            && isLivingEntityOrSubclass(call.owner)
                            && !isAlreadyWrapped(call, hook)) {
                        wrapCallSite(method, call, hook);
                        modified = true;
                    }
                }
                instruction = next;
            }
        }
        return modified;
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        return processClass(phase, classNode, classType);
    }

    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        return processClass(phase, classNode, classType, reason)
                ? ComputeFlags.COMPUTE_FRAMES
                : ComputeFlags.NO_REWRITE;
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        if (isEmpty) {
            return EnumSet.noneOf(Phase.class);
        }
        return EnumSet.of(Phase.BEFORE);
    }

    private boolean canWrap(MethodInsnNode call) {
        int opcode = call.getOpcode();
        return opcode == Opcodes.INVOKEVIRTUAL
                || opcode == Opcodes.INVOKEINTERFACE
                || opcode == Opcodes.INVOKESPECIAL;
    }

    private void wrapCallSite(MethodNode method, MethodInsnNode call, LifeCycleCallSiteHookSpec hook) {
        Type[] argumentTypes = Type.getArgumentTypes(call.desc);
        Type returnType = Type.getReturnType(call.desc);
        int nextLocal = Math.max(method.maxLocals, minimumLocals(method));

        int ownerLocal = nextLocal++;
        int[] argumentLocals = new int[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            argumentLocals[i] = nextLocal;
            nextLocal += argumentTypes[i].getSize();
        }
        int resultLocal = nextLocal;
        nextLocal += returnType.getSize();
        method.maxLocals = nextLocal;

        InsnList before = new InsnList();
        for (int i = argumentTypes.length - 1; i >= 0; i--) {
            before.add(new VarInsnNode(argumentTypes[i].getOpcode(Opcodes.ISTORE), argumentLocals[i]));
        }
        before.add(new VarInsnNode(Opcodes.ASTORE, ownerLocal));
        before.add(new VarInsnNode(Opcodes.ALOAD, ownerLocal));
        for (int i = 0; i < argumentTypes.length; i++) {
            before.add(new VarInsnNode(argumentTypes[i].getOpcode(Opcodes.ILOAD), argumentLocals[i]));
        }
        method.instructions.insertBefore(call, before);

        InsnList after = new InsnList();
        after.add(new VarInsnNode(returnType.getOpcode(Opcodes.ISTORE), resultLocal));
        after.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                HOOK_OWNER,
                "INSTANCE",
                HOOK_INSTANCE_DESC
        ));
        after.add(new VarInsnNode(Opcodes.ALOAD, ownerLocal));
        after.add(new VarInsnNode(returnType.getOpcode(Opcodes.ILOAD), resultLocal));
        after.add(new MethodInsnNode(
                Opcodes.INVOKEINTERFACE,
                HOOK_INTERFACE,
                hook.hookMethodName(),
                hook.hookMethodDescriptor(),
                true
        ));
        method.instructions.insert(call, after);
    }

    private int minimumLocals(MethodNode method) {
        int locals = (method.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        for (Type argumentType : Type.getArgumentTypes(method.desc)) {
            locals += argumentType.getSize();
        }
        return locals;
    }

    private boolean isAlreadyWrapped(MethodInsnNode call, LifeCycleCallSiteHookSpec hook) {
        Type returnType = Type.getReturnType(call.desc);
        AbstractInsnNode cursor = nextInstruction(call);
        if (!(cursor instanceof VarInsnNode resultStore)
                || resultStore.getOpcode() != returnType.getOpcode(Opcodes.ISTORE)) {
            return false;
        }

        cursor = nextInstruction(cursor);
        if (!(cursor instanceof FieldInsnNode instanceRead)
                || instanceRead.getOpcode() != Opcodes.GETSTATIC
                || !HOOK_OWNER.equals(instanceRead.owner)
                || !"INSTANCE".equals(instanceRead.name)
                || !HOOK_INSTANCE_DESC.equals(instanceRead.desc)) {
            return false;
        }

        cursor = nextInstruction(cursor);
        if (!(cursor instanceof VarInsnNode ownerLoad) || ownerLoad.getOpcode() != Opcodes.ALOAD) {
            return false;
        }

        cursor = nextInstruction(cursor);
        if (!(cursor instanceof VarInsnNode resultLoad)
                || resultLoad.getOpcode() != returnType.getOpcode(Opcodes.ILOAD)
                || resultLoad.var != resultStore.var) {
            return false;
        }

        cursor = nextInstruction(cursor);
        return cursor instanceof MethodInsnNode hookCall
                && hookCall.getOpcode() == Opcodes.INVOKEINTERFACE
                && HOOK_INTERFACE.equals(hookCall.owner)
                && hook.hookMethodName().equals(hookCall.name)
                && hook.hookMethodDescriptor().equals(hookCall.desc);
    }

    private AbstractInsnNode nextInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode cursor = instruction.getNext();
        while (cursor != null && cursor.getOpcode() < 0) {
            cursor = cursor.getNext();
        }
        return cursor;
    }

    private boolean isLivingEntityOrSubclass(String internalName) {
        if (LIVING_ENTITY_INTERNAL.equals(internalName)) {
            return true;
        }

        Set<String> visited = new HashSet<>();
        String current = internalName;
        while (current != null
                && !"java/lang/Object".equals(current)
                && visited.add(current)) {
            String superName = superNameCache.get(current);
            if (superName == null) {
                superName = readAndCacheSuperName(current);
            }
            if (LIVING_ENTITY_INTERNAL.equals(superName)) {
                return true;
            }
            current = superName;
        }
        return false;
    }

    private String readAndCacheSuperName(String internalName) {
        String resourceName = internalName + ".class";
        try (InputStream stream = openClassResource(resourceName)) {
            if (stream == null) {
                return null;
            }
            ClassReader reader = new ClassReader(stream);
            String superName = reader.getSuperName();
            cacheSuperName(reader.getClassName(), superName);
            return superName;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private InputStream openClassResource(String resourceName) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = getResource(contextLoader, resourceName);
        if (stream != null) {
            return stream;
        }

        ClassLoader pluginLoader = getClass().getClassLoader();
        if (pluginLoader != contextLoader) {
            stream = getResource(pluginLoader, resourceName);
            if (stream != null) {
                return stream;
            }
        }
        return ClassLoader.getSystemResourceAsStream(resourceName);
    }

    private InputStream getResource(ClassLoader loader, String resourceName) {
        return loader == null ? null : loader.getResourceAsStream(resourceName);
    }

    private void cacheSuperName(String className, String superName) {
        if (className != null && superName != null) {
            superNameCache.put(className, superName);
        }
    }
}
