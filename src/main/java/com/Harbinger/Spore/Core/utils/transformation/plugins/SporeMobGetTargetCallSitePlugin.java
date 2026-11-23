package com.Harbinger.Spore.Core.utils.transformation.plugins;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumSet;

public final class SporeMobGetTargetCallSitePlugin implements ILaunchPluginService {
    private final String mobOwner = "net/minecraft/world/entity/Mob";
    private final String livingDescriptor = "Lnet/minecraft/world/entity/LivingEntity;";
    private final String hookOwner = "com/Harbinger/Spore/Core/asmHooks/MobGetTargetHook";
    private final String hookInterface = "com/Harbinger/Spore/Core/asmHooks/IGetTargetHook";
    private final String instanceDescriptor = "L" + hookInterface + ";";
    private final String hookMethod = "getTargetCallSiteHook";
    private final String hookDescriptor = "(L" + mobOwner + ";" + livingDescriptor + ")" + livingDescriptor;

    @Override
    public String name() {
        return "SporeMobGetTargetCallSitePlugin";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return isEmpty ? EnumSet.noneOf(Phase.class) : EnumSet.of(Phase.BEFORE);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        if (phase != Phase.BEFORE || classNode == null || classNode.name == null || classNode.name.isEmpty()) {
            return false;
        }
        SuperNameCache.INSTANCE.put(classNode.name, classNode.superName);
        if (classNode.name.startsWith("com/Harbinger/Spore/")) {
            return false;
        }

        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            if (method == null || method.instructions == null) {
                continue;
            }
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; ) {
                AbstractInsnNode next = instruction.getNext();
                if (instruction instanceof MethodInsnNode call && isTargetGetter(call) && !isWrapped(call)) {
                    wrap(method.instructions, call);
                    modified = true;
                }
                instruction = next;
            }
        }
        return modified;
    }

    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        return processClass(phase, classNode, classType) ? ComputeFlags.COMPUTE_MAXS : ComputeFlags.NO_REWRITE;
    }

    private boolean isTargetGetter(MethodInsnNode call) {
        return (call.getOpcode() == Opcodes.INVOKEVIRTUAL || call.getOpcode() == Opcodes.INVOKESPECIAL)
                && !call.itf
                && ("getTarget".equals(call.name) || "m_5448_".equals(call.name))
                && ("()" + livingDescriptor).equals(call.desc)
                && SuperNameCache.isSameOrSubclass(call.owner, mobOwner);
    }

    private void wrap(InsnList instructions, MethodInsnNode call) {
        instructions.insertBefore(call, new InsnNode(Opcodes.DUP));
        InsnList after = new InsnList();
        after.add(new FieldInsnNode(Opcodes.GETSTATIC, hookOwner, "INSTANCE", instanceDescriptor));
        // [mob, result, hook] -> [hook, mob, result]
        after.add(new InsnNode(Opcodes.DUP_X2));
        after.add(new InsnNode(Opcodes.POP));
        after.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, hookInterface, hookMethod, hookDescriptor, true));
        instructions.insert(call, after);
    }

    private boolean isWrapped(MethodInsnNode call) {
        AbstractInsnNode previous = call.getPrevious();
        while (previous != null && previous.getOpcode() < 0) {
            previous = previous.getPrevious();
        }
        if (previous == null || previous.getOpcode() != Opcodes.DUP) {
            return false;
        }
        if (!(nextInstruction(call) instanceof FieldInsnNode field) || field.getOpcode() != Opcodes.GETSTATIC
                || !hookOwner.equals(field.owner) || !"INSTANCE".equals(field.name)
                || !instanceDescriptor.equals(field.desc)) {
            return false;
        }
        AbstractInsnNode dup = nextInstruction(field);
        if (dup == null || dup.getOpcode() != Opcodes.DUP_X2) {
            return false;
        }
        AbstractInsnNode pop = nextInstruction(dup);
        if (pop == null || pop.getOpcode() != Opcodes.POP) {
            return false;
        }
        return nextInstruction(pop) instanceof MethodInsnNode hook && hook.getOpcode() == Opcodes.INVOKEINTERFACE
                && hook.itf && hookInterface.equals(hook.owner) && hookMethod.equals(hook.name)
                && hookDescriptor.equals(hook.desc);
    }

    private AbstractInsnNode nextInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode next = instruction.getNext();
        while (next != null && next.getOpcode() < 0) {
            next = next.getNext();
        }
        return next;
    }
}
