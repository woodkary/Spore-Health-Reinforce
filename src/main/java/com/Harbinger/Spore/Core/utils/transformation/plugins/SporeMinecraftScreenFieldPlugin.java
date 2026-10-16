package com.Harbinger.Spore.Core.utils.transformation.plugins;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class SporeMinecraftScreenFieldPlugin implements ILaunchPluginService {
    private final String minecraftOwner = "net/minecraft/client/Minecraft";
    private final String screenDescriptor = "Lnet/minecraft/client/gui/screens/Screen;";
    private final String hookOwner = "com/Harbinger/Spore/Core/asmHooks/MinecraftScreenHook";
    private final String hookInterface = "com/Harbinger/Spore/Core/asmHooks/IScreenHook";
    private final String hookInstanceDescriptor = "Lcom/Harbinger/Spore/Core/asmHooks/IScreenHook;";
    private final String hookDescriptor =
            "(Lnet/minecraft/client/gui/screens/Screen;)Lnet/minecraft/client/gui/screens/Screen;";

    public SporeMinecraftScreenFieldPlugin() {
    }

    @Override
    public String name() {
        return "SporeMinecraftScreenFieldPlugin";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return isEmpty ? EnumSet.noneOf(Phase.class) : EnumSet.of(Phase.BEFORE);
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

        SuperNameCache.INSTANCE.put(classNode.name, classNode.superName);
        if (classNode.name.startsWith("com/Harbinger/Spore/")) {
            return false;
        }

        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            if (method == null || method.instructions == null || method.instructions.size() == 0) {
                continue;
            }
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; ) {
                AbstractInsnNode next = instruction.getNext();
                if (instruction instanceof FieldInsnNode field
                        && field.getOpcode() == Opcodes.GETFIELD
                        && ("f_91080_".equals(field.name) || "screen".equals(field.name))
                        && screenDescriptor.equals(field.desc)
                        && isMinecraftOrSubclass(field.owner)
                        && !isAlreadyWrapped(field)) {
                    wrapFieldRead(method, field);
                    modified = true;
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
                ? ComputeFlags.COMPUTE_MAXS
                : ComputeFlags.NO_REWRITE;
    }

    private void wrapFieldRead(MethodNode method, FieldInsnNode field) {
        // The original read leaves a Screen; SWAP puts the interface receiver below it.
        InsnList after = new InsnList();
        after.add(new FieldInsnNode(Opcodes.GETSTATIC, hookOwner, "INSTANCE", hookInstanceDescriptor));
        after.add(new InsnNode(Opcodes.SWAP));
        after.add(new MethodInsnNode(
                Opcodes.INVOKEINTERFACE, hookInterface, "getScreenHook", hookDescriptor, true));
        method.instructions.insert(field, after);
    }

    private boolean isAlreadyWrapped(FieldInsnNode field) {
        AbstractInsnNode cursor = nextInstruction(field);
        if (!(cursor instanceof FieldInsnNode instanceRead)
                || instanceRead.getOpcode() != Opcodes.GETSTATIC
                || !hookOwner.equals(instanceRead.owner)
                || !"INSTANCE".equals(instanceRead.name)
                || !hookInstanceDescriptor.equals(instanceRead.desc)) {
            return false;
        }
        cursor = nextInstruction(cursor);
        if (cursor == null || cursor.getOpcode() != Opcodes.SWAP) {
            return false;
        }
        cursor = nextInstruction(cursor);
        return cursor instanceof MethodInsnNode call
                && call.getOpcode() == Opcodes.INVOKEINTERFACE
                && hookInterface.equals(call.owner)
                && "getScreenHook".equals(call.name)
                && hookDescriptor.equals(call.desc)
                && call.itf;
    }

    private AbstractInsnNode nextInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode cursor = instruction.getNext();
        while (cursor != null && cursor.getOpcode() < 0) {
            cursor = cursor.getNext();
        }
        return cursor;
    }

    private boolean isMinecraftOrSubclass(String internalName) {
        Set<String> visited = new HashSet<>();
        String current = internalName;
        while (current != null && visited.add(current)) {
            if (minecraftOwner.equals(current)) {
                return true;
            }
            if ("java/lang/Object".equals(current)) {
                return false;
            }
            String superName = SuperNameCache.INSTANCE.get(current);
            if (superName == null) {
                superName = readAndCacheSuperName(current);
            }
            current = superName;
        }
        return false;
    }

    private String readAndCacheSuperName(String internalName) {
        try (InputStream stream = openClassResource(internalName + ".class")) {
            if (stream == null) {
                return null;
            }
            ClassReader reader = new ClassReader(stream);
            String superName = reader.getSuperName();
            SuperNameCache.INSTANCE.put(reader.getClassName(), superName);
            return superName;
        } catch (Throwable ignored) {
            // Missing metadata is retried on later callbacks rather than cached as a negative result.
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
}
