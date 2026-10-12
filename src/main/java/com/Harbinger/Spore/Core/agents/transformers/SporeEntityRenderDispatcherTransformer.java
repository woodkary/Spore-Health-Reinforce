package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SporeEntityRenderDispatcherTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String DISPATCHER = "net/minecraft/client/renderer/entity/EntityRenderDispatcher";
    private static final String ENTITY = "net/minecraft/world/entity/Entity";
    private static final String HOOK = "com/Harbinger/Spore/Core/asmHooks/SporeEntityRenderDispatcherHook";
    private static final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/asmHooks/ISporeEntityRenderDispatcherHook";
    private static final String RENDER_TAIL = "DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
    private static final String HOOK_DESC = "(L" + DISPATCHER + ";L" + ENTITY + ";" + RENDER_TAIL;
    private static final Class<?> TRANSFORM_CLASS = BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityRenderDispatcherTransformer.class
    );
    private static MethodHandle constructor;

    static {
        if (!TRANSFORM_CLASS.isHidden()) {
            throw new IllegalStateException("EntityRenderDispatcher transformer must be a hidden class");
        }
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor, TRANSFORM_CLASS, SporeEntityRenderDispatcherTransformer.class);
    }

    public SporeEntityRenderDispatcherTransformer() {
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor, TRANSFORM_CLASS, SporeEntityRenderDispatcherTransformer.class);
        try {
            if (constructor == null) {
                throw new IllegalStateException("Hidden dispatcher transformer constructor is unavailable");
            }
            ClassFileTransformer transformer = (ClassFileTransformer) constructor.invoke();
            if (!transformer.getClass().isHidden()) {
                throw new IllegalStateException("Dispatcher transformer factory returned a named class");
            }
            return transformer;
        } catch (Throwable failure) {
            throw new IllegalStateException("Failed to construct hidden EntityRenderDispatcher transformer", failure);
        }
    }

    public static SelfTransformer newSelfTransformer() {
        return (SelfTransformer) newInstance();
    }

    @Override
    public byte[] transformClassByte(ClassLoader loader, String className, byte[] classfileBuffer) {
        return transformInternal(loader, className, classfileBuffer);
    }

    @Override
    protected byte[] transformInternal(ClassLoader loader, String className, byte[] classfileBuffer) {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return null;
        }
        try {
            ClassNode node = new ClassNode();
            new ClassReader(classfileBuffer).accept(node, ClassReader.EXPAND_FRAMES);
            if (node.name == null || node.superName == null) {
                return null;
            }
            cacheSuperName(node.name, node.superName);
            // Project-owned dispatcher subclasses must be covered too.
            if (!isDispatcher(node, loader)) {
                return null;
            }
            boolean modified = false;
            for (MethodNode method : node.methods) {
                if (!matches(method) || alreadyInjected(method)) {
                    continue;
                }
                patch(node.name, method);
                modified = true;
                LogUtil.logf("Transformed EntityRenderDispatcher method %s.%s%s", node.name, method.name, method.desc);
            }
            if (!modified) {
                return null;
            }
            ClassWriter writer = new SporeFrameClassWriter(loader, node, ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            byte[] transformed = writer.toByteArray();
            SporeTransformerDebugDump.rememberTransformed(getClass().getName(),
                    className == null ? node.name : className, node.name, classfileBuffer, transformed);
            return transformed;
        } catch (Throwable failure) {
            LogUtil.errorf("Failed to transform EntityRenderDispatcher %s: %s", className, failure.getMessage());
            LogUtil.printStackTrace(failure);
            return null;
        }
    }

    private boolean isDispatcher(ClassNode node, ClassLoader loader) {
        String name = node.name;
        String parent = node.superName;
        Set<String> visited = new HashSet<>();
        while (name != null && visited.add(name)) {
            if (DISPATCHER.equals(name)) {
                return true;
            }
            if (parent == null || "java/lang/Object".equals(parent)) {
                return false;
            }
            name = parent;
            if (DISPATCHER.equals(name)) {
                return true;
            }
            parent = superNameCache.get(name);
            if (parent == null) {
                ClassNode ancestor = readParent(loader, name);
                if (ancestor == null) {
                    return false;
                }
                parent = ancestor.superName;
                cacheSuperName(ancestor.name, parent);
            }
        }
        return false;
    }

    private ClassNode readParent(ClassLoader loader, String name) {
        String resource = name + ".class";
        InputStream input = loader == null ? null : loader.getResourceAsStream(resource);
        if (input == null) {
            input = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (input == null) {
            input = SporeEntityRenderDispatcherTransformer.class.getClassLoader().getResourceAsStream(resource);
        }
        if (input == null) {
            return null;
        }
        try (InputStream closeable = input) {
            ClassNode node = new ClassNode();
            new ClassReader(closeable).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        } catch (Exception failure) {
            LogUtil.errorf("Failed to inspect dispatcher parent %s: %s", name, failure.getMessage());
            return null;
        }
    }

    private boolean matches(MethodNode method) {
        if (!("render".equals(method.name) || "m_114384_".equals(method.name))
                || (method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0
                || method.instructions == null || method.instructions.size() == 0
                || Type.getReturnType(method.desc).getSort() != Type.VOID) {
            return false;
        }
        Type[] arguments = Type.getArgumentTypes(method.desc);
        return arguments.length == 9 && arguments[0].getSort() == Type.OBJECT
                && RENDER_TAIL.equals(method.desc.substring(1 + arguments[0].getDescriptor().length()));
    }

    private boolean alreadyInjected(MethodNode method) {
        // Recognize the actual entry sequence, not an unrelated call later in the original body.
        AbstractInsnNode cursor = nextInstruction(method.instructions.getFirst());
        if (!(cursor instanceof VarInsnNode load) || load.getOpcode() != Opcodes.ALOAD || load.var != 1) {
            return false;
        }
        cursor = nextInstruction(cursor.getNext());
        if (!(cursor instanceof TypeInsnNode check) || check.getOpcode() != Opcodes.INSTANCEOF || !ENTITY.equals(check.desc)) {
            return false;
        }
        cursor = nextInstruction(cursor.getNext());
        if (!(cursor instanceof JumpInsnNode branch) || branch.getOpcode() != Opcodes.IFEQ) {
            return false;
        }
        cursor = nextInstruction(cursor.getNext());
        if (!(cursor instanceof FieldInsnNode field) || field.getOpcode() != Opcodes.GETSTATIC
                || !HOOK.equals(field.owner) || !"INSTANCE".equals(field.name)
                || !("L" + HOOK_INTERFACE + ";").equals(field.desc)) {
            return false;
        }
        for (cursor = cursor.getNext(); cursor != null && cursor != branch.label; cursor = cursor.getNext()) {
            if (cursor instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKEINTERFACE
                    && HOOK_INTERFACE.equals(call.owner) && "render".equals(call.name) && HOOK_DESC.equals(call.desc)) {
                AbstractInsnNode returnInsn = nextInstruction(cursor.getNext());
                return returnInsn != null && returnInsn.getOpcode() == Opcodes.RETURN;
            }
        }
        return false;
    }

    private AbstractInsnNode nextInstruction(AbstractInsnNode cursor) {
        while (cursor != null && cursor.getOpcode() < 0) {
            cursor = cursor.getNext();
        }
        return cursor;
    }

    private void patch(String owner, MethodNode method) {
        Type[] arguments = Type.getArgumentTypes(method.desc);
        LabelNode originalBody = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 1));
        prefix.add(new TypeInsnNode(Opcodes.INSTANCEOF, ENTITY));
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, originalBody));
        prefix.add(new FieldInsnNode(Opcodes.GETSTATIC, HOOK, "INSTANCE", "L" + HOOK_INTERFACE + ";"));
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        int slot = 1;
        for (int index = 0; index < arguments.length; index++) {
            Type argument = arguments[index];
            prefix.add(new VarInsnNode(argument.getOpcode(Opcodes.ILOAD), slot));
            if (index == 0) {
                prefix.add(new TypeInsnNode(Opcodes.CHECKCAST, ENTITY));
            }
            slot += argument.getSize();
        }
        prefix.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, HOOK_INTERFACE, "render", HOOK_DESC, true));
        prefix.add(new InsnNode(Opcodes.RETURN));
        prefix.add(originalBody);
        List<Object> locals = new ArrayList<>();
        locals.add(owner);
        for (Type argument : arguments) {
            switch (argument.getSort()) {
                case Type.DOUBLE -> locals.add(Opcodes.DOUBLE);
                case Type.FLOAT -> locals.add(Opcodes.FLOAT);
                case Type.INT -> locals.add(Opcodes.INTEGER);
                default -> locals.add(argument.getInternalName());
            }
        }
        // Insert outside all original labels/exception ranges. Existing frames and debug tables remain intact.
        // If offset zero already had a frame, it describes the same initial locals at the new branch target.
        AbstractInsnNode firstFrame = method.instructions.getFirst();
        while (firstFrame != null && (firstFrame instanceof LabelNode || firstFrame instanceof LineNumberNode)) {
            firstFrame = firstFrame.getNext();
        }
        if (!(firstFrame instanceof FrameNode)) {
            prefix.add(new FrameNode(Opcodes.F_NEW, locals.size(), locals.toArray(), 0, null));
        }
        method.instructions.insert(prefix);
    }
}
