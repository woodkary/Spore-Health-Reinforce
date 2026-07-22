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

public final class SporeEntityRendererTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String SPORE_PACKAGE_PREFIX = "com/Harbinger/Spore/";
    private static final String ENTITY_RENDERER_INTERNAL = "net/minecraft/client/renderer/entity/EntityRenderer";
    private static final String SIMPLE_REMOVE_OWNER = "com/Harbinger/Spore/Core/utils/simpleRemoval/SimpleRemoveUtil";
    private static final String SIMPLE_REMOVE_INTERFACE = "com/Harbinger/Spore/Core/utils/simpleRemoval/ISimpleRemoval";
    private static final String CHECK_REMOVED_METHOD = "checkIsRemovedAndUpdate";
    private static final String CHECK_REMOVED_DESC = "(Ljava/lang/Object;)Z";
    private static final String LEGACY_CHECK_REMOVED_DESC = "(Lnet/minecraft/world/entity/Entity;)Z";
    private static final String RENDER_DESC_TAIL = "FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
    private static final String RENDER_NAME_TAG_DESC_TAIL = "Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
    private static final String SHOULD_RENDER_DESC_TAIL = "Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z";
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS =
            (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeEntityRendererTransformer.class
            );
    private static MethodHandle constructor;

    static {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeEntityRendererTransformer.class
        );
    }

    public static SelfTransformer newSelfTransformer() {
        ClassFileTransformer transformer = newInstance();
        if (transformer instanceof SelfTransformer selfTransformer) {
            return selfTransformer;
        }
        return new SporeEntityRendererTransformer();
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeEntityRendererTransformer.class
        );
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeEntityRendererTransformer, %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
        return new SporeEntityRendererTransformer();
    }

    public SporeEntityRendererTransformer() {
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
            if (classNode.name.startsWith(SPORE_PACKAGE_PREFIX)
                    || !isSubClass(classNode, loader, ENTITY_RENDERER_INTERNAL)) {
                return null;
            }
            if (transformRendererMethods(classNode)) {
                return toBytes(loader, effectiveClassName, classfileBuffer, classNode);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform EntityRenderer method of %s, %s",
                    className == null ? "<hidden-or-anonymous>" : className,
                    t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
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
                input = SporeEntityRendererTransformer.class.getClassLoader().getResourceAsStream(resource);
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
            LogUtil.errorf("failed to inspect EntityRenderer parent class %s, %s", internalName, t.getMessage());
            return null;
        }
    }

    private boolean transformRendererMethods(ClassNode classNode) {
        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            int shortCircuitOpcode = resolveShortCircuitOpcode(method);
            if (shortCircuitOpcode == -1 || !canPatch(method) || alreadyCallsHook(method)) {
                continue;
            }
            if (patchMethodStart(classNode.name, method, shortCircuitOpcode)) {
                modified = true;
                LogUtil.logf("Transformed EntityRenderer method %s.%s%s",
                        classNode.name,
                        method.name,
                        method.desc);
            }
        }
        return modified;
    }

    private int resolveShortCircuitOpcode(MethodNode method) {
        if (("render".equals(method.name) || "m_7392_".equals(method.name))
                && matchesDescriptorTail(method, RENDER_DESC_TAIL)) {
            return Opcodes.RETURN;
        }
        if (("renderNameTag".equals(method.name) || "m_7649_".equals(method.name))
                && matchesDescriptorTail(method, RENDER_NAME_TAG_DESC_TAIL)) {
            return Opcodes.RETURN;
        }
        if (("shouldRender".equals(method.name) || "m_5523_".equals(method.name))
                && matchesDescriptorTail(method, SHOULD_RENDER_DESC_TAIL)) {
            return Opcodes.IRETURN;
        }
        return -1;
    }

    private boolean matchesDescriptorTail(MethodNode method, String expectedTail) {
        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (arguments.length == 0 || arguments[0].getSort() != Type.OBJECT) {
            return false;
        }
        int tailStart = 1 + arguments[0].getDescriptor().length();
        return tailStart < method.desc.length() && expectedTail.equals(method.desc.substring(tailStart));
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
            if (insn instanceof MethodInsnNode methodInsn
                    && SIMPLE_REMOVE_INTERFACE.equals(methodInsn.owner)
                    && CHECK_REMOVED_METHOD.equals(methodInsn.name)
                    && (CHECK_REMOVED_DESC.equals(methodInsn.desc)
                        || LEGACY_CHECK_REMOVED_DESC.equals(methodInsn.desc))) {
                return true;
            }
        }
        return false;
    }

    private boolean patchMethodStart(String classInternalName, MethodNode method, int shortCircuitOpcode) {
        AbstractInsnNode firstRealInsn = findFirstRealInstruction(method);
        if (firstRealInsn == null) {
            return false;
        }
        LabelNode continueLabel = new LabelNode();
        InsnList inject = new InsnList();
        inject.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                SIMPLE_REMOVE_OWNER,
                "INSTANCE",
                "L" + SIMPLE_REMOVE_INTERFACE + ";"
        ));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEINTERFACE,
                SIMPLE_REMOVE_INTERFACE,
                CHECK_REMOVED_METHOD,
                CHECK_REMOVED_DESC,
                true
        ));
        inject.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        if (shortCircuitOpcode == Opcodes.IRETURN) {
            inject.add(new InsnNode(Opcodes.ICONST_0));
        }
        inject.add(new InsnNode(shortCircuitOpcode));
        inject.add(continueLabel);
        List<Object> locals = createInitialFrameLocals(classInternalName, method);
        inject.add(new FrameNode(Opcodes.F_NEW, locals.size(), locals.toArray(), 0, null));
        method.instructions.insertBefore(firstRealInsn, inject);
        return true;
    }

    private List<Object> createInitialFrameLocals(String classInternalName, MethodNode method) {
        List<Object> locals = new ArrayList<>();
        locals.add(classInternalName);
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

    private byte[] toBytes(ClassLoader loader, String className, byte[] inputBytes, ClassNode classNode) {
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
