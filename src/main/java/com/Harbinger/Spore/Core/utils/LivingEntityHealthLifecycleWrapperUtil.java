package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.entities.SporeDeadLocalPlayer;
import com.Harbinger.Spore.Core.entities.SporeDeadServerPlayer;
import com.Harbinger.Spore.Core.utils.inventory.SporeEmptyInventory;
import com.Harbinger.Spore.network.WrapperPacket;
import com.Harbinger.Spore.network.WrapperPacketHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Dynamically creates wrapper subclasses for non-player LivingEntity callbacks and rewrites
 * health/lifecycle related methods to:
 * return EntityHeealltuth.INSTANCE.xxx1(this, super.xxx(...))
 */
public final class LivingEntityHealthLifecycleWrapperUtil implements ILivingEntityLifeCycle {
    private static final String WRAPPER_SUFFIX = "SporeHealthLifecycleWrapper";
    private static final String DEATH_WRAPPER_SUFFIX = "SporeDeathLifecycleWrapper";
    private static final String LIVING_ENTITY_INTERNAL = "net/minecraft/world/entity/LivingEntity";
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/EntityHeealuthManager";
    private static final String IENTITY_HEALTH_INTERNAL = "com/Harbinger/Spore/Core/asmHooks/IEntityHealth";
    private static final String RESPAWN_DESC = "()V";
    private static final String RESPAWN_NAME = "respawn";
    private static final String RESPAWN_OBF_NAME = "m_7583_";
    private static final String TICK_NAME = "tick";
    private static final String TICK_OBF_NAME = "m_8119_";
    private static final String DO_TICK_NAME = "doTick";
    private static final String DO_TICK_OBF_NAME = "m_9240_";
    private static final String AI_STEP_NAME = "aiStep";
    private static final String AI_STEP_OBF_NAME = "m_8107_";
    private static final String SERVER_AI_STEP_NAME = "serverAiStep";
    private static final String SERVER_AI_STEP_OBF_NAME = "m_6140_";
    private static final String RIDE_TICK_NAME = "rideTick";
    private static final String RIDE_TICK_OBF_NAME = "m_6083_";
    private static final String BASE_TICK_NAME = "baseTick";
    private static final String BASE_TICK_OBF_NAME = "m_6075_";
    private static final String DEATH_TIME_OBF_NAME = "f_20919_";
    private static final String GET_INVENTORY_NAME = "getInventory";
    private static final String GET_INVENTORY_OBF_NAME = "m_150109_";
    private static final String PLAYER_INVENTORY_DESC = "()Lnet/minecraft/world/entity/player/Inventory;";
    private static final String EMPTY_INVENTORY_OWNER = "com/Harbinger/Spore/Core/utils/inventory/SporeEmptyInventory";
    private static final String EMPTY_INVENTORY_FACTORY_DESC = "(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/entity/player/Inventory;";
    private static final String HIDDEN_NAME_SEGMENT = "/0x";
    public static final ILivingEntityLifeCycle INSTANCE = BytecodeUtil.createHiddenSingletonInstance(ILivingEntityLifeCycle.class, LivingEntityHealthLifecycleWrapperUtil.class);
    private final Function<Class<?>, Class<?>> BUILD_WARPPER_FUNC=BytecodeUtil.createHiddenSingletonInstance(
            Function.class,
            BuildWrapperClassFunction.class
    );
    private final Function<Class<?>, Class<?>> BUILD_DEATH_WARPPER_FUNC=BytecodeUtil.createHiddenSingletonInstance(
            Function.class,
            BuildDeathWrapperClassFunction.class
    );
    private final ConcurrentMap<Class<?>, Class<?>> CACHE = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> DEATH_CACHE = new ConcurrentHashMap<>();

    public LivingEntityHealthLifecycleWrapperUtil() {
    }

    @Override
    public Class<?> getOrginalClass(Class<?> wrapperValue){
        //通过value找回第一个key
        return ClassLoaderUtil.INSTANCE.tryAvoidHiddenClass(wrapperToOriginal.getOrDefault(wrapperValue, wrapperValue));
    }
    @Override
    public void createWrapppperLocal(Object entity){
        Class<?> wrapper = createWrapper(entity.getClass());
        if (wrapper != null) {
            KlassPointerUtil.INSTANCE.replaceClass(entity, wrapper, "", 0, 0.0f);
        }
    }
    @Override
    public void createWrapppper(Object entity){
        createWrapppperLocal(entity);
        if(entity instanceof Entity e){
            WrapperPacketHandler.sendToClient(new WrapperPacket(e.id,WrapperPacket.HEALTH_WRAPPER));
        }

    }
    @Override
    public void createDeathWrapppperLocal(Object entity){
        Class<?> wrapper = createDeathWrapper(entity.getClass());
        if (wrapper != null) {
            KlassPointerUtil.INSTANCE.replaceClass(entity, wrapper, "", 0, 0.0f);
        }
    }
    @Override
    public void createDeathWrapppper(Object entity){
        createDeathWrapppperLocal(entity);
        if(entity instanceof Entity e){
            WrapperPacketHandler.sendToClient(new WrapperPacket(e.id,WrapperPacket.DEATH_WRAPPER));
        }
    }
    @Override
    public void slayPlayer(Player player){
        EntityHeealuthManager.INSTANCE.setHeealtthDelta(player, Float.NEGATIVE_INFINITY);
        player.getPersistentData().putBoolean("SporeDeeaadfd", true);
        KlassPointerUtil.INSTANCE.replaceClass(player.getInventory(), SporeEmptyInventory.inventoryClass, "", 0, 0.0f);
        Class<?> wrapper = getDeadPlayerWrapper(createDeathWrapperForPlayer(player.getClass()), player);
        if(wrapper!=null){
            KlassPointerUtil.INSTANCE.replaceClass(player, wrapper, "", 0, 0.0f);
        }
    }
    private Class<?> getDeadPlayerWrapper(Class<?> wrapper,Player player){
        if(wrapper!=null){
            return wrapper;
        }
        if(player instanceof ServerPlayer){
            return SporeDeadServerPlayer.serverPlayerClass;
        }else if(player instanceof LocalPlayer){
            return SporeDeadLocalPlayer.localPlayerClass;
        }
        return null;
    }

    private Class<?> createWrapper(Class<?> callback) {
        callback = getOrginalClass(callback);
        if (callback == null) {
            return null;
        }
        if (!LivingEntity.class.isAssignableFrom(callback)) {
            return null;
        }
        if (callback.getName().contains(WRAPPER_SUFFIX)) {
            return callback;
        }
        if (Modifier.isFinal(callback.getModifiers())) {
            return null;
        }

        Class<?> wrapper=CACHE.computeIfAbsent(callback,BUILD_WARPPER_FUNC);
        if(wrapper!=null){
            wrapperToOriginal.putIfAbsent(wrapper, callback);
        }
        return wrapper;
    }
    private Class<?> createDeathWrapper(Class<?> callback) {
        callback = getOrginalClass(callback);
        if (callback == null) {
            return null;
        }
        if (!LivingEntity.class.isAssignableFrom(callback) || Player.class.isAssignableFrom(callback)) {
            return null;
        }
        if (callback.getName().contains(DEATH_WRAPPER_SUFFIX)) {
            return callback;
        }
        if (Modifier.isFinal(callback.getModifiers())) {
            return null;
        }

        Class<?> wrapper = DEATH_CACHE.computeIfAbsent(callback, BUILD_DEATH_WARPPER_FUNC);
        if(wrapper!=null){
            wrapperToOriginal.putIfAbsent(wrapper, callback);
        }
        return wrapper;
    }
    private Class<?> createDeathWrapperForPlayer(Class<?> callback) {
        callback = getOrginalClass(callback);
        if (callback == null) {
            return null;
        }
        if (!Player.class.isAssignableFrom(callback)) {
            return null;
        }
        if (callback.getName().contains(DEATH_WRAPPER_SUFFIX)) {
            return callback;
        }
        if (Modifier.isFinal(callback.getModifiers())) {
            return null;
        }

        Class<?> wrapper = DEATH_CACHE.computeIfAbsent(callback, BUILD_DEATH_WARPPER_FUNC);
        if(wrapper!=null){
            wrapperToOriginal.putIfAbsent(wrapper, callback);
        }
        return wrapper;
    }
    private final ConcurrentHashMap<Class<?>, Class<?>> wrapperToOriginal = new ConcurrentHashMap<>();
    @Override
    public Class<?> buildWrapperClass(Class<?> callback) {
        try {
            ClassNode node = new ClassNode();
            String superName = Type.getInternalName(callback);
            String wrapperName = buildWrapperInternalName(callback, WRAPPER_SUFFIX);

            node.visit(Opcodes.V17, Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL, wrapperName, null, superName, null);
            node.visitSource(".dynamic", null);

            boolean hasCtor = emitConstructors(node, callback, superName);
            if (!hasCtor) {
                return null;
            }

            emitHookedMethods(node, callback, superName);
            node.visitEnd();

            return ClassLoaderUtil.INSTANCE.deffineneHiddenClazz(node,callback);
        } catch (Throwable t) {
            LogUtil.errorf("failed to build health wrapper class %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }
    private void forceDeathTimeIncreasing(ClassNode node, String superName){
        //node.methods.removeIf(method -> TICK_OBF_NAME.equals(method.name) && RESPAWN_DESC.equals(method.desc));
        Iterator<MethodNode> iterator = node.methods.iterator();
        while(iterator.hasNext()){
            MethodNode method = iterator.next();
            if(TICK_OBF_NAME.equals(method.name) && RESPAWN_DESC.equals(method.desc)){
                iterator.remove();
            }
        }
        MethodVisitor mv = node.visitMethod(Opcodes.ACC_PUBLIC, TICK_OBF_NAME, RESPAWN_DESC, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, LIVING_ENTITY_INTERNAL, DEATH_TIME_OBF_NAME, "I");
        mv.visitVarInsn(Opcodes.ISTORE, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, TICK_OBF_NAME, RESPAWN_DESC, false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IADD);
        mv.visitFieldInsn(Opcodes.PUTFIELD, LIVING_ENTITY_INTERNAL, DEATH_TIME_OBF_NAME, "I");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    @Override
    public Class<?> buildDeathWrapperClass(Class<?> callback) {
        try {
            ClassNode node = new ClassNode();
            String superName = Type.getInternalName(callback);
            String wrapperName = buildWrapperInternalName(callback, DEATH_WRAPPER_SUFFIX);

            node.visit(Opcodes.V17, Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL, wrapperName, null, superName, null);
            node.visitSource(".dynamic", null);

            boolean hasCtor = emitConstructors(node, callback, superName);
            if (!hasCtor) {
                return null;
            }

            emitDeathStateMethods(node, callback);
            if(Player.class.isAssignableFrom(callback)){
                emitPlayerRespawnNoopMethods(node, callback);
            }
//            if(callback.getName().startsWith("com.jerotes.jerotesvillage.entity.Boss.Biome.VariantZsieinEntity")){
//                forceDeathTimeIncreasing(node, superName);
//            }
            node.visitEnd();

            return ClassLoaderUtil.INSTANCE.deffineneHiddenClazz(node,callback);
        } catch (Throwable t) {
            LogUtil.errorf("failed to build death wrapper class %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }
    private String buildWrapperInternalName(Class<?> callback, String suffix) {
        String stableBinaryName = toStableBinaryName(callback);
        int lastDot = stableBinaryName.lastIndexOf('.');
        String pkg = lastDot >= 0 ? stableBinaryName.substring(0, lastDot) : "";
        String simple = lastDot >= 0 ? stableBinaryName.substring(lastDot + 1) : stableBinaryName;
        if (simple.isEmpty()) {
            simple = "SporeDynamicHost";
        }
        if (pkg.isEmpty()) {
            return simple + suffix;
        }
        return pkg.replace('.', '/') + "/" + simple + suffix;
    }
    private String toStableBinaryName(Class<?> callback) {
        String name = callback.getName();
        int hidden = name.indexOf(HIDDEN_NAME_SEGMENT);
        if (hidden > 0) {
            return name.substring(0, hidden);
        }
        return name;
    }

    private boolean emitConstructors(ClassNode node, Class<?> callback, String superName) {
        boolean added = false;
        for (Constructor<?> ctor : callback.getDeclaredConstructors()) {
            int mod = ctor.getModifiers();
            if (Modifier.isPrivate(mod)) {
                continue;
            }
            int access = 0;
            if (Modifier.isPublic(mod)) {
                access |= Opcodes.ACC_PUBLIC;
            } else if (Modifier.isProtected(mod)) {
                access |= Opcodes.ACC_PROTECTED;
            } else {
                access |= 0;
            }

            String desc = Type.getConstructorDescriptor(ctor);
            MethodVisitor mv = node.visitMethod(access, "<init>", desc, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);

            Type[] args = Type.getArgumentTypes(desc);
            int slot = 1;
            for (Type arg : args) {
                mv.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), slot);
                slot += arg.getSize();
            }

            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", desc, false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            added = true;
        }
        return added;
    }

    private void emitHookedMethods(ClassNode node, Class<?> callback, String superName) {
        Set<String> visited = new HashSet<>();
        for (Class<?> cursor = callback; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            for (Method method : cursor.getDeclaredMethods()) {
                int mod = method.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isPrivate(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                if (!(Modifier.isPublic(mod) || Modifier.isProtected(mod))) {
                    continue;
                }
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }

                HookSpec spec = resolveHook(method);
                if (spec == null) {
                    continue;
                }

                String desc = Type.getMethodDescriptor(method);
                String sig = method.getName() + desc;
                if (!visited.add(sig)) {
                    continue;
                }

                int access = Modifier.isPublic(mod) ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PROTECTED;
                MethodVisitor mv = node.visitMethod(access, method.getName(), desc, null, null);
                mv.visitCode();

                // super.xxx(args...)
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                Type[] args = Type.getArgumentTypes(desc);
                int slot = 1;
                for (Type arg : args) {
                    mv.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), slot);
                    slot += arg.getSize();
                }
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, method.getName(), desc, false);

                // EntityHeealltuth.INSTANCE.hook(this, superRet)
                Type ret = Type.getType(method.getReturnType());
                int local = slot;
                if (ret.getSort() == Type.DOUBLE) {
                    mv.visitVarInsn(Opcodes.DSTORE, local);
                } else {
                    mv.visitVarInsn(ret.getOpcode(Opcodes.ISTORE), local);
                }
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        HOOK_OWNER,
                        "INSTANCE",
                        "L" + IENTITY_HEALTH_INTERNAL + ";"
                );
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                if (ret.getSort() == Type.DOUBLE) {
                    mv.visitVarInsn(Opcodes.DLOAD, local);
                } else {
                    mv.visitVarInsn(ret.getOpcode(Opcodes.ILOAD), local);
                }
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, IENTITY_HEALTH_INTERNAL, spec.hookMethodName, spec.hookDesc, true);
                mv.visitInsn(spec.returnOpcode);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
    }
    private void emitDeathStateMethods(ClassNode node, Class<?> callback) {
        Set<String> visited = new HashSet<>();
        for (Class<?> cursor = callback; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            for (Method method : cursor.getDeclaredMethods()) {
                int mod = method.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isPrivate(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                if (!(Modifier.isPublic(mod) || Modifier.isProtected(mod))) {
                    continue;
                }
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }

                String desc = Type.getMethodDescriptor(method);
                String sig = method.getName() + desc;
                if (!visited.add(sig)) {
                    continue;
                }

                Type ret = Type.getType(method.getReturnType());
                int sort = ret.getSort();
                if (sort != Type.FLOAT && sort != Type.DOUBLE && sort != Type.BOOLEAN) {
                    continue;
                }

                String name = method.getName();
                boolean isHealth = nameLooksLikeHealth(name);
                boolean isMaxHealth = nameLooksLikeMaxHealth(name);
                boolean isDeadOrDying = nameLooksLikeIsDeadOrDying(name);
                boolean isAlive = nameLooksLikeIsAlive(name);
                if (!isHealth && !isMaxHealth && !isDeadOrDying && !isAlive) {
                    continue;
                }

                int valueOpcode;
                int returnOpcode;
                if (sort == Type.BOOLEAN) {
                    if (isDeadOrDying) {
                        valueOpcode = Opcodes.ICONST_1;
                        returnOpcode = Opcodes.IRETURN;
                    } else if (isAlive) {
                        valueOpcode = Opcodes.ICONST_0;
                        returnOpcode = Opcodes.IRETURN;
                    } else {
                        continue;
                    }
                } else if (sort == Type.FLOAT) {
                    if (!(isHealth || isMaxHealth)) {
                        continue;
                    }
                    valueOpcode = Opcodes.FCONST_0;
                    returnOpcode = Opcodes.FRETURN;
                } else if (sort == Type.DOUBLE) {
                    if (!(isHealth || isMaxHealth)) {
                        continue;
                    }
                    valueOpcode = Opcodes.DCONST_0;
                    returnOpcode = Opcodes.DRETURN;
                } else {
                    continue;
                }

                int access = Modifier.isPublic(mod) ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PROTECTED;
                MethodVisitor mv = node.visitMethod(access, method.getName(), desc, null, null);
                mv.visitCode();
                mv.visitInsn(valueOpcode);
                mv.visitInsn(returnOpcode);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
    }

    private void emitPlayerRespawnNoopMethods(ClassNode node, Class<?> callback) {
        Set<String> emitted = new HashSet<>();
        emitPlayerNoopMethodPair(node, callback, emitted, RESPAWN_NAME, RESPAWN_OBF_NAME);
        emitPlayerNoopMethodPair(node, callback, emitted, TICK_NAME, TICK_OBF_NAME);
        emitPlayerNoopMethodPair(node, callback, emitted, DO_TICK_NAME, DO_TICK_OBF_NAME);
        emitPlayerNoopMethodPair(node, callback, emitted, AI_STEP_NAME, AI_STEP_OBF_NAME);
        emitPlayerNoopMethodPair(node, callback, emitted, SERVER_AI_STEP_NAME, SERVER_AI_STEP_OBF_NAME);
        emitPlayerNoopMethodPair(node, callback, emitted, RIDE_TICK_NAME, RIDE_TICK_OBF_NAME);
        emitPlayerNoopMethodPair(node, callback, emitted, BASE_TICK_NAME, BASE_TICK_OBF_NAME);
        emitPlayerInventoryMethodPair(node, callback, emitted, GET_INVENTORY_NAME, GET_INVENTORY_OBF_NAME);
    }

    private void emitPlayerNoopMethodPair(ClassNode node, Class<?> callback, Set<String> emitted, String readableName, String obfName) {
        boolean overridden = false;
        overridden |= emitNoopOverrideIfFound(node, callback, emitted, readableName);
        if (!Objects.equals(obfName, readableName)) {
            overridden |= emitNoopOverrideIfFound(node, callback, emitted, obfName);
        }
        if (overridden) {
            return;
        }

        addNoopMethodIfSafe(node, callback, emitted, readableName);
        if (!Objects.equals(obfName, readableName)) {
            addNoopMethodIfSafe(node, callback, emitted, obfName);
        }
    }

    private boolean emitNoopOverrideIfFound(ClassNode node, Class<?> callback, Set<String> emitted, String targetName) {
        for (Class<?> cursor = callback; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            for (Method method : cursor.getDeclaredMethods()) {
                int mod = method.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isPrivate(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                if (!(Modifier.isPublic(mod) || Modifier.isProtected(mod))) {
                    continue;
                }
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }
                if (!targetName.equals(method.getName())) {
                    continue;
                }
                String desc = Type.getMethodDescriptor(method);
                if (!RESPAWN_DESC.equals(desc)) {
                    continue;
                }
                String sig = method.getName() + desc;
                if (!emitted.add(sig)) {
                    return true;
                }
                int access = Modifier.isPublic(mod) ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PROTECTED;
                emitNoopVoidMethod(node, access, method.getName(), desc);
                return true;
            }
        }
        return false;
    }

    private void addNoopMethodIfSafe(ClassNode node, Class<?> callback, Set<String> emitted, String methodName) {
        String sig = methodName + RESPAWN_DESC;
        if (!emitted.add(sig)) {
            return;
        }
        if (!canDeclarePlayerNoopMethod(callback, methodName, RESPAWN_DESC)) {
            emitted.remove(sig);
            return;
        }
        emitNoopVoidMethod(node, Opcodes.ACC_PUBLIC, methodName, RESPAWN_DESC);
    }

    private boolean canDeclarePlayerNoopMethod(Class<?> callback, String methodName, String desc) {
        for (Class<?> cursor = callback; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            for (Method method : cursor.getDeclaredMethods()) {
                if (!methodName.equals(method.getName())) {
                    continue;
                }
                if (!desc.equals(Type.getMethodDescriptor(method))) {
                    continue;
                }
                int mod = method.getModifiers();
                if (Modifier.isPrivate(mod)) {
                    continue;
                }
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    private void emitPlayerInventoryMethodPair(ClassNode node, Class<?> callback, Set<String> emitted, String readableName, String obfName) {
        boolean overridden = false;
        overridden |= emitInventoryOverrideIfFound(node, callback, emitted, readableName);
        if (!Objects.equals(obfName, readableName)) {
            overridden |= emitInventoryOverrideIfFound(node, callback, emitted, obfName);
        }
        if (overridden) {
            return;
        }

        addInventoryMethodIfSafe(node, callback, emitted, readableName);
        if (!Objects.equals(obfName, readableName)) {
            addInventoryMethodIfSafe(node, callback, emitted, obfName);
        }
    }

    private boolean emitInventoryOverrideIfFound(ClassNode node, Class<?> callback, Set<String> emitted, String targetName) {
        for (Class<?> cursor = callback; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            for (Method method : cursor.getDeclaredMethods()) {
                int mod = method.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isPrivate(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                if (!(Modifier.isPublic(mod) || Modifier.isProtected(mod))) {
                    continue;
                }
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }
                if (!targetName.equals(method.getName())) {
                    continue;
                }
                String desc = Type.getMethodDescriptor(method);
                if (!PLAYER_INVENTORY_DESC.equals(desc)) {
                    continue;
                }
                String sig = method.getName() + desc;
                if (!emitted.add(sig)) {
                    return true;
                }
                int access = Modifier.isPublic(mod) ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PROTECTED;
                emitPlayerInventoryMethod(node, access, method.getName());
                return true;
            }
        }
        return false;
    }

    private void addInventoryMethodIfSafe(ClassNode node, Class<?> callback, Set<String> emitted, String methodName) {
        String sig = methodName + PLAYER_INVENTORY_DESC;
        if (!emitted.add(sig)) {
            return;
        }
        if (!canDeclarePlayerNoopMethod(callback, methodName, PLAYER_INVENTORY_DESC)) {
            emitted.remove(sig);
            return;
        }
        emitPlayerInventoryMethod(node, Opcodes.ACC_PUBLIC, methodName);
    }

    private void emitPlayerInventoryMethod(ClassNode node, int access, String methodName) {
        MethodVisitor mv = node.visitMethod(access, methodName, PLAYER_INVENTORY_DESC, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                EMPTY_INVENTORY_OWNER,
                "newInstance",
                EMPTY_INVENTORY_FACTORY_DESC,
                false
        );
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void emitNoopVoidMethod(ClassNode node, int access, String methodName, String desc) {
        MethodVisitor mv = node.visitMethod(access, methodName, desc, null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private HookSpec resolveHook(Method method) {
        Type ret = Type.getType(method.getReturnType());
        String name = method.getName();
        if (!isFloatDoubleOrBoolean(ret)) {
            return null;
        }
        if (!nameLooksLikeHealth(name)
                && !nameLooksLikeMaxHealth(name)
                && !nameLooksLikeIsDeadOrDying(name)
                && !nameLooksLikeIsAlive(name)) {
            return null;
        }

        if (ret.getSort() == Type.BOOLEAN) {
            if (nameLooksLikeIsDeadOrDying(name)) {
                return new HookSpec("isDeeadfOrDyaging", "(L" + LIVING_ENTITY_INTERNAL + ";Z)Z", Opcodes.SWAP, Opcodes.IRETURN);
            }
            if (nameLooksLikeIsAlive(name)) {
                return new HookSpec("isAlliive", "(L" + LIVING_ENTITY_INTERNAL + ";Z)Z", Opcodes.SWAP, Opcodes.IRETURN);
            }
            return null;
        }

        if (nameLooksLikeMaxHealth(name)) {
            if (ret.getSort() == Type.FLOAT) {
                return new HookSpec("getMaaxxHeaaltsh", "(L" + LIVING_ENTITY_INTERNAL + ";F)F", Opcodes.SWAP, Opcodes.FRETURN);
            }
            if (ret.getSort() == Type.DOUBLE) {
                return new HookSpec("getMaaxxHeaaltsh", "(L" + LIVING_ENTITY_INTERNAL + ";D)D", Opcodes.NOP, Opcodes.DRETURN);
            }
            return null;
        }

        if (nameLooksLikeHealth(name)) {
            if (ret.getSort() == Type.FLOAT) {
                return new HookSpec("getHeealth", "(L" + LIVING_ENTITY_INTERNAL + ";F)F", Opcodes.SWAP, Opcodes.FRETURN);
            }
            if (ret.getSort() == Type.DOUBLE) {
                return new HookSpec("getHeealth", "(L" + LIVING_ENTITY_INTERNAL + ";D)D", Opcodes.NOP, Opcodes.DRETURN);
            }
        }
        return null;
    }

    private boolean isFloatDoubleOrBoolean(Type type) {
        return type.getSort() == Type.FLOAT || type.getSort() == Type.DOUBLE || type.getSort() == Type.BOOLEAN;
    }

    private boolean nameLooksLikeHealth(String name) {
        if(name.equals("haveDiexv")){
            return true;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return (n.contains("heal") && !n.contains("max")) || "m_21223_".equals(name);
    }

    private boolean nameLooksLikeMaxHealth(String name) {
        if(name.equals("haveBigDiexv")){
            return true;
        }
        String n = name.toLowerCase(Locale.ROOT);
        return (n.contains("max") && n.contains("heal")) || "m_21233_".equals(name);
    }
    @Override
    public boolean nameLooksLikeIsDeadOrDying(String name) {
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

    private static final class HookSpec {
        final String hookMethodName;
        final String hookDesc;
        final int swapOpcode;
        final int returnOpcode;

        HookSpec(String hookMethodName, String hookDesc, int swapOpcode, int returnOpcode) {
            this.hookMethodName = hookMethodName;
            this.hookDesc = hookDesc;
            this.swapOpcode = swapOpcode;
            this.returnOpcode = returnOpcode;
        }
    }
}
