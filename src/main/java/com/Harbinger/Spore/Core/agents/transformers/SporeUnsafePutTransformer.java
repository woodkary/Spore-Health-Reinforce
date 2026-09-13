package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.asmHooks.UnsafePutHook;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.agents.IJVNTIPointer;
import com.Harbinger.Spore.Core.agents.JVMTIPointerUtil;
import java.util.Locale;

/** Guards Unsafe put/putVolatile calls made by mod classes. */
public final class SporeUnsafePutTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/IUnsafePutHook";
    private static final String UNSAFE_PUT_HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/UnsafePutHook";
    private static final String METHOD_HANDLE_OWNER = "java/lang/invoke/MethodHandle";
    private static final String[] UNSAFE_OWNERS = {"sun/misc/Unsafe", "jdk/internal/misc/Unsafe"};
    private static final String HOOK_DESC = "L" + HOOK_OWNER + ";";
    private static final String IS_TARGET_DESC = "(Ljava/lang/Object;)Z";
    private static final String IS_HANDLE_DESC = "(Ljava/lang/invoke/MethodHandle;)I";
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS =
            (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(SporeUnsafePutTransformer.class);

    private static MethodHandle constructor;
    private static volatile boolean jvmtiInstalled;
    private static volatile boolean instrumentationInstalled;

    static {
        try {
            constructor = ClassUtil.getLookup().findConstructor(
                    TRANSFORM_CLASS, java.lang.invoke.MethodType.methodType(void.class));
        } catch (Throwable ignored) {
        }
    }

    public static SelfTransformer newSelfTransformer() {
        ClassFileTransformer transformer = newInstance();
        return transformer instanceof SelfTransformer self ? self : new SporeUnsafePutTransformer();
    }

    public static ClassFileTransformer newInstance() {
        if(constructor==null) {
            try {
                constructor = ClassUtil.getLookup().findConstructor(
                        TRANSFORM_CLASS, java.lang.invoke.MethodType.methodType(void.class));
            } catch (Throwable ignored) {
            }
        }
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeUnsafePutTransformer, %s", t.getMessage());
            }
        }
        return new SporeUnsafePutTransformer();
    }

    public static synchronized void install() {
        IJVNTIPointer jvmti = JVMTIPointerUtil.newInstance();
        if (jvmti != null && !jvmtiInstalled) {
            jvmti.addTransformer(newSelfTransformer());
            jvmtiInstalled = jvmti.isTransformerHookInstalled();
        }
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        if (instrumentation != null && !instrumentationInstalled) {
            instrumentation.addTransformer(newInstance());
            instrumentationInstalled = true;
        }
    }

    @Override
    protected byte[] transformInternal(ClassLoader loader, String className, byte[] classfileBuffer) {
        if (classfileBuffer == null || classfileBuffer.length == 0) return null;
        String normalized = className == null ? null : className.replace('/', '.');
        if (normalized != null && !isBadModNameCopy(normalized)) return null;
        if (normalized != null && shouldSkip(normalized)) return null;
        try {
            ClassNode node = new ClassNode();
            new ClassReader(classfileBuffer).accept(node, ClassReader.EXPAND_FRAMES);
            String effective = (node.name == null ? normalized : node.name.replace('/', '.'));
            if (effective == null || !isBadModNameCopy(effective) || shouldSkip(effective)) return null;
            boolean modified = false;
            for (MethodNode method : node.methods) {
                if (method == null || method.instructions == null
                        || (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) continue;
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
                    AbstractInsnNode next = insn.getNext();
                    if (insn instanceof MethodInsnNode call) {
                        if (isUnsafePut(call)) modified |= patchUnsafePut(method, call);
                        else if (isMethodHandleInvoke(call)) modified |= patchMethodHandleInvoke(method, call);
                    }
                    insn = next;
                }
            }
            if (!modified) return null;
            ClassWriter writer = new SporeFrameClassWriter(loader, node,
                    ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            byte[] transformed = writer.toByteArray();
            SporeTransformerDebugDump.rememberTransformed(getClass().getName(), className, node.name,
                    classfileBuffer, transformed);
            return transformed;
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform Unsafe put call sites of %s, %s", className, t.getMessage());
            return null;
        }
    }

    private boolean patchUnsafePut(MethodNode method, MethodInsnNode call) {
        Type[] args = Type.getArgumentTypes(call.desc);
        if (args.length == 0 || !isExactObject(args[0]) || Type.getReturnType(call.desc).getSort() != Type.VOID) return false;
        int[] locals = allocateLocals(method, args, true);
        InsnList code = new InsnList();
        storeStack(code, args, locals, locals[args.length]);
        LabelNode invoke = new LabelNode(), done = new LabelNode();
        addTargetCheck(code, locals[0], invoke);
        code.add(new JumpInsnNode(Opcodes.GOTO, done));
        code.add(invoke);
        loadCall(code, locals, args, locals[args.length]);
        code.add(new MethodInsnNode(call.getOpcode(), call.owner, call.name, call.desc, call.itf));
        code.add(done);
        method.instructions.insertBefore(call, code);
        method.instructions.remove(call);
        return true;
    }

    private boolean patchMethodHandleInvoke(MethodNode method, MethodInsnNode call) {
        Type[] args = Type.getArgumentTypes(call.desc);
        if (Type.getReturnType(call.desc).getSort() != Type.VOID || args.length == 0) return false;
        boolean directShape = args.length > 1 && isReference(args[1]);
        boolean boundShape = isReference(args[0]);
        if (!directShape && !boundShape) return false;
        int[] locals = allocateLocals(method, args, true);
        int typeLocal = method.maxLocals++;
        InsnList code = new InsnList();
        storeStack(code, args, locals, locals[args.length]);
        code.add(new FieldInsnNode(Opcodes.GETSTATIC, UNSAFE_PUT_HOOK_OWNER, "INSTANCE", HOOK_DESC));
        code.add(new VarInsnNode(Opcodes.ALOAD, locals[args.length]));
        code.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, HOOK_OWNER,
                "isUnsafeRelatedMethodHandle", IS_HANDLE_DESC, true));
        code.add(new VarInsnNode(Opcodes.ISTORE, typeLocal));
        LabelNode direct = new LabelNode(), bound = new LabelNode(), invoke = new LabelNode(), done = new LabelNode();
        if (directShape) {
            code.add(new VarInsnNode(Opcodes.ILOAD, typeLocal));
            code.add(new InsnNode(Opcodes.ICONST_0));
            code.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, direct));
        }
        if (boundShape) {
            code.add(new VarInsnNode(Opcodes.ILOAD, typeLocal));
            code.add(new InsnNode(Opcodes.ICONST_1));
            code.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, bound));
        }
        code.add(new JumpInsnNode(Opcodes.GOTO, invoke));
        if (directShape) addHandleTargetCheck(code, locals, 1, direct, invoke, done);
        if (boundShape) addHandleTargetCheck(code, locals, 0, bound, invoke, done);
        code.add(invoke);
        loadCall(code, locals, args, locals[args.length]);
        code.add(new MethodInsnNode(call.getOpcode(), call.owner, call.name, call.desc, call.itf));
        code.add(done);
        method.instructions.insertBefore(call, code);
        method.instructions.remove(call);
        return true;
    }

    private void addHandleTargetCheck(InsnList code, int[] locals, int targetIndex,
                                      LabelNode entry, LabelNode invoke, LabelNode done) {
        code.add(entry);
        code.add(new FieldInsnNode(Opcodes.GETSTATIC, UNSAFE_PUT_HOOK_OWNER, "INSTANCE", HOOK_DESC));
        code.add(new VarInsnNode(Opcodes.ALOAD, locals[targetIndex]));
        code.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, HOOK_OWNER, "isSporeModTarget", IS_TARGET_DESC, true));
        code.add(new JumpInsnNode(Opcodes.IFEQ, invoke));
        code.add(new JumpInsnNode(Opcodes.GOTO, done));
    }

    private void addTargetCheck(InsnList code, int targetLocal, LabelNode invoke) {
        code.add(new FieldInsnNode(Opcodes.GETSTATIC, UNSAFE_PUT_HOOK_OWNER, "INSTANCE", HOOK_DESC));
        code.add(new VarInsnNode(Opcodes.ALOAD, targetLocal));
        code.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, HOOK_OWNER, "isSporeModTarget", IS_TARGET_DESC, true));
        code.add(new JumpInsnNode(Opcodes.IFEQ, invoke));
    }

    private int[] allocateLocals(MethodNode method, Type[] args, boolean withReceiver) {
        int[] locals = new int[args.length + 1];
        int next = method.maxLocals;
        for (int i = args.length - 1; i >= 0; i--) {
            locals[i] = next;
            next += args[i].getSize();
        }
        locals[args.length] = next;
        method.maxLocals = next + 1;
        return locals;
    }

    private void storeStack(InsnList code, Type[] args, int[] locals, int receiverLocal) {
        for (int i = args.length - 1; i >= 0; i--) code.add(new VarInsnNode(storeOpcode(args[i]), locals[i]));
        code.add(new VarInsnNode(Opcodes.ASTORE, receiverLocal));
    }

    private void loadCall(InsnList code, int[] locals, Type[] args, int receiverLocal) {
        code.add(new VarInsnNode(Opcodes.ALOAD, receiverLocal));
        for (int i = 0; i < args.length; i++) code.add(new VarInsnNode(loadOpcode(args[i]), locals[i]));
    }

    private int storeOpcode(Type t) { return t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY ? Opcodes.ASTORE : t.getOpcode(Opcodes.ISTORE); }
    private int loadOpcode(Type t) { return t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY ? Opcodes.ALOAD : t.getOpcode(Opcodes.ILOAD); }
    private boolean isExactObject(Type t) { return t.getSort() == Type.OBJECT && "java/lang/Object".equals(t.getInternalName()); }
    private boolean isReference(Type t) { return t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY; }
    private boolean isUnsafePut(MethodInsnNode c) {
        if (!isUnsafeOwner(c.owner) || c.getOpcode() != Opcodes.INVOKEVIRTUAL || !c.name.startsWith("put")) return false;
        Type[] args = Type.getArgumentTypes(c.desc);
        return args.length > 0 && isExactObject(args[0]);
    }
    private boolean isMethodHandleInvoke(MethodInsnNode c) {
        return c.getOpcode() == Opcodes.INVOKEVIRTUAL
                && METHOD_HANDLE_OWNER.equals(c.owner)
                && ("invoke".equals(c.name) || "invokeExact".equals(c.name));
    }
    private boolean isUnsafeOwner(String owner) { for (String s : UNSAFE_OWNERS) if (s.equals(owner)) return true; return false; }
    private boolean shouldSkip(String name) { return name.equals(SporeUnsafePutTransformer.class.getName()) || name.equals(UnsafePutHook.class.getName()) || name.equals("com.Harbinger.Spore.Core.asmHooks.IUnsafePutHook"); }

    // Kept private and self-contained so loading this transformer does not link StackTraceUtil.
    private boolean isBadModNameCopy(String name) {
        name = name.replace('/', '.');
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.startsWith("com.harbinger.spore.")) return false;
        if (isVanilla(name) || isWhiteListed(name)) return false;
        return true;
    }
    private boolean isVanilla(String n) {
        if (n.startsWith("net.minecraft.")) { String s=n.substring(14); return !s.contains(".") || starts(s,"advancements","client","commands","core","data","gametest","locale","nbt","network","obfuscate","realms","recipebook","resources","server","sounds","stats","tags","util","world"); }
        if (n.startsWith("cpw.mods.")) { String s=n.substring(9); return !s.contains(".") || starts(s,"cl","jarhandling","niofs","util","modlauncher","bootstraplauncher"); }
        return starts(n,"com.electronwill.","com.google.","com.mojang.","com.sun.","cpw.mods.","io.netty.","it.unimi.","java.","javax.","jdk.","joptsimple.","net.minecraftforge.","net.minecrell.","org.antlr.","org.apache.","org.lwjgl.","org.joml.","org.objectweb.","org.openjdk.","org.slf4j.","org.spongepowered.","sun.");
    }
    private boolean isWhiteListed(String n) { return starts(n,"com.llamalad7.mixinextras.","me.jellysquid.mods.sodium.","net.irisshaders.iris.","net.irisshaders.batchedentityrendering.","dev.tr7zw.entityculling.","net.raphimc.immediatelyfast.","malte0811.ferritecore.","software.bernie.geckolib.","com.supermartijn642.fusion.","com.supermartijn642.core.","dev.architectury.","top.theillusivec4.caelus.","com.github.alexthe666.citadel.","top.theillusivec4.curios.","virtuoel.pehkui.","dev.kosmx.playerAnim.","optifine.","net.optifine.","mezz.jei.","fi.dy.masa.tweakeroo","oshi.","io.github.flemmli97.mobbattle."); }
    private boolean starts(String n,String... p){ for(String s:p) if(n.startsWith(s)) return true; return false; }
    @Override public byte[] transformClassByte(ClassLoader loader, String className, byte[] classfileBuffer) { return transformInternal(loader,className,classfileBuffer); }
}
