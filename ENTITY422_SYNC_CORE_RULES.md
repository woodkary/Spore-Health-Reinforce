# Entity422 Remote Sync Core Rules

本文档是本仓库同步 Entity422/Entity442 上游后必须保留和验证的本地核心增强清单。同步完成不能只看 `compileJava` 是否通过；必须按机制检查入口、隐藏类、ASM hook、实体存储替换、网络包、命令、资源和特化寻路是否仍然连成完整链路。

## 基本原则

- 保留本地增强优先级高于上游同名文件的简单覆盖。遇到冲突时，先确认上游真实行为，再把本地增强重新迁移到新结构中。
- 不要把 `com.Harbinger.Spore.Core`、`com.Harbinger.Spore.mixin`、`com.Harbinger.Spore.network`、`com.Harbinger.Spore.sEvents` 当作普通业务代码处理；这些包里有运行时替换、隐藏类、ASM、保存数据和实体封锁逻辑。
- 每次同步后至少执行 `git status --short --branch`、`git diff --name-status <base>...HEAD`、`rg` 关键字审计和 `.\gradlew --no-daemon --console=plain compileJava`。
- 直接 `Entity#hurt`/`LivingEntity#hurt` 不是都要替换。实体自身 `super.hurt(...)`、multipart 转发到父实体、原版主击中路径通常可以保留；额外伤害、弹射物、武器、AOE、效果 tick、绕过原版血量系统的补伤害应优先走 `SporeAttackUtil.INSTANCE.attack(...)` 或对应 manager。

## 1. 日志、隐藏类、Unsafe、MethodHandle

必须保留的核心类：

- `Core/utils/BytecodeUtil.java`
- `Core/utils/ClassUtil.java`
- `Core/utils/ClassLoaderUtil.java`
- `Core/utils/HiddenClassDefiner.java`
- `Core/utils/MethodHandleUtil.java`
- `Core/utils/KlassPointerUtil.java`
- `Core/utils/LogUtil.java`
- `Core/utils/Log4j2PrintStream.java`
- `Core/utils/unremovableCollections/ISporeCollection.java`
- `Core/utils/unremovableCollections/ISporeEntry.java`
- `Core/utils/unremovableCollections/ISporeIterator.java`
- `Core/utils/unremovableCollections/ISporeMap.java`
- `Core/utils/unremovableCollections/ISporeSet.java`
- `Core/utils/unremovableCollections/SporeMapProxy.java`
- `Core/utils/unremovableCollections/SporeSetProxy.java`
- `Core/utils/ProtectedConcurrentHashMap.java`
- `Core/utils/ProtectedWeakHashMap.java`
- `Core/utils/wrappedMethod/WrappedMethod.java`
- `Core/utils/wrappedMethod/IWrappedMethod.java`
- `Core/utils/StackTraceUtil.java`
- `Core/utils/ParentUtil.java`
- `Core/utils/TargetUtil.java`
- `Core/utils/HeasdalthUtil.java`
- `Core/utils/IHeasdalthUtil.java`

同步验证：

- `BytecodeUtil.createHiddenSingletonInstance(...)`、`resolveHiddenClassOrSelf(...)`、隐藏构造器路径不能退化为普通 `new`。
- `HiddenClassDefiner` 的缓存与 `ThreadLocal` in-progress 防递归必须保留；同一线程递归定义相同隐藏类时不能再次进入定义链。
- `ClassUtil` 的隐藏类定义、字段读写、类替换辅助必须保留；如果出现 `VerifyError: Bad type on operand stack`，优先检查隐藏子类是否把 owner 写成了非隐藏宿主类。
- `WrappedMethod`/`MethodHandle` 缓存要按实际签名调用，不能把 `MethodHandle(ServerPlayer,DamageSource)void` 之类的强类型 handle 当成 `(Object[])Object` 直接 invoke。
- `Protected*Map` 用于封锁写入/读取/遍历，不能被上游集合替换覆盖。
- `Core/utils/unremovableCollections` 下的代理集合是核心运行时机制，重要性等同日志、隐藏类、Unsafe 和 MethodHandle。同步时必须保留普通写入/移除封锁、view/iterator/entry 封锁，以及内部可信路径的 `actualPut`、`actualRemove`、`actualSetValue`、`actualRemove()` 等入口。

## 2. 生命周期、ASM 血量、死亡包装

必须保留的入口和核心类：

- `Spore.java`
- `Core/agents/AgentBridge.java`
- `Core/agents/IAgentBridge.java`
- `Core/agents/InstrumentationUtil.java`
- `Core/agents/IInstrumentations.java`
- `Core/agents/JVMTIPointerUtil.java`
- `Core/agents/IJVNTIPointer.java`
- `Core/jvmti/JvmtiMethod.java`
- `Core/utils/JvmtiCapabilities.java`
- `Core/SporeMixinPlugin.java`
- `Core/asmHooks/HiddenDefineHook.java`
- `Core/agents/transformers/SelfTransformer.java`
- `Core/agents/transformers/ICommonBootStrap.java`
- `Core/agents/transformers/SporeClassFileTransformer0.java`
- `Core/agents/transformers/INativeBridge.java`
- `Core/agents/transformers/SporeNativeBridge.java`
- `Core/agents/transformers/SporeLivingEntityHealthTransformer.java`
- `Core/agents/transformers/SporeLivingEntityEffectApplicationTransformer.java`
- `Core/agents/transformers/SporeLivingEntityHealthTransformerBootstrap.java`
- `Core/agents/transformers/SporeHiddenDefineHookTransformer.java`
- `Core/agents/transformers/SporeFrameClassWriter.java`
- `Core/agents/transformers/SporeTransformerDebugDump.java`
- `Core/agents/transformers/InstrumentationImplTransformUtil.java`
- `Core/agents/transformers/IInstrumentationImplTransformer.java`
- `src/main/native/com/Harbinger/Spore/Core/agents/transformers/com_Harbinger_Spore_Core_agents_transformers_SporeClassFileTransformer0.c`
- `src/main/native/include/com_Harbinger_Spore_Core_agents_transformers_SporeClassFileTransformer0.h`
- `src/main/resources/spore.mixins.json`
- `src/main/resources/sporeAgent.jar`
- `src/main/resources/sporeTransformerBridge.dll`
- `Core/utils/LivingEntityHealthLifecycleWrapperUtil.java`
- `Core/utils/BuildWrapperClassFunction.java`
- `Core/utils/BuildDeathWrapperClassFunction.java`
- `Core/entities/SporeDeadLocalPlayer.java`
- `Core/entities/SporeDeadServerPlayer.java`
- `mixin/LivingEntityMixin.java`
- `mixin/LocalPlayerMixin.java`

同步验证：

- `Spore.commonSetup` 必须注册网络包并调用 `SporeLivingEntityHealthTransformerBootstrap.INSTANCE.installAndRetransform()`。
- `SporeLivingEntityHealthTransformerBootstrap.installAndRetransform()` 必须同时注册 `SporeLivingEntityHealthTransformer` 和 `SporeLivingEntityEffectApplicationTransformer`。
- 对 health/effect transformer，Instrumentation 和 JVMTI 是同一套转换器的双后端：优先由 Instrumentation 重转换，失败或有剩余目标时回退 JVMTI；两端都必须支持枚举已加载类、可修改性判断、安装 transformer 和重转换。`AgentBridge`/`InstrumentationUtil` 及 `/sporeAgent.jar`（内含 `SporeAgent.class` 和 `sporeAgent.dll`）的自附加链也不能丢失。
- JVMTI Java/JNA 层必须保留 `JvmtiMethod` 函数表索引、`JvmtiCapabilities` 位布局、`ClassFileLoadHook` 注册、capability 协商、错误码名称、内存 allocate/deallocate 和 native/JNA 回退。不要只保留 `RetransformClasses` 的表面调用。
- native C 层与 `/sporeTransformerBridge.dll` 必须同步保留：除 JNI `ClassFileTransformer#transform` 转发外，还要保留 JVMTI env 获取、capability、已加载类/可修改类查询、`ClassFileLoadHook`、`RetransformClasses` 和 `GetErrorName`。修改 C 或 JNI 签名后必须重新编译并替换 DLL，不能只提交源码或旧 DLL。
- `SporeLivingEntityHealthTransformerBootstrap.retransformMaybeHiddenClasses(...)`、`retransformMaybeHiddenClassesInstOnly(...)`、`retransformMaybeHiddenClassesJVMTIOnly(...)` 必须保留，`HeasdalthUtil` 创建/替换生命周期 wrapper 后必须继续调用这些入口。普通路径和 hidden-retransform 路径使用独立安装状态，保证相关 transformer 至少能为隐藏类兜底重装一次。
- 对已定义隐藏类的重转换是兜底：必须先加载 hook 依赖，暂时清除 Klass 的 hidden/being-redefined access flags，在 `finally` 中恢复原 flags，并以二分重转换隔离单个失败类。`spore.transformer.disableUnsafeHiddenRetransform` 跳过开关不得被误删。
- 首选路径是在定义前转换。`spore.mixins.json` 必须继续声明 `Core.SporeMixinPlugin`；插件加载 `HiddenDefineHook.inspectHiddenDefine()`，后者通过 Instrumentation/JVMTI 安装 `SporeHiddenDefineHookTransformer`，但不主动重转换已加载调用者。
- `SporeHiddenDefineHookTransformer` 只扫描 `StackTraceUtil.isBadModName(...)` 目标，并覆盖直接 `Lookup#defineHiddenClass`、反射 `Method.invoke` 到 `defineHiddenClass`/`makeHiddenClassDefiner`，以及 `Lookup#findStatic` 获取 `ClassLoader#defineClass0` 的路径。反射 Method 判定必须以内联 ASM 完成，非目标调用不能先加载 `HiddenDefineHook`；新增分支必须用 `COMPUTE_FRAMES | COMPUTE_MAXS`。
- `HiddenDefineHook` 必须在原始 bytes 定义前串联 health/effect transformer，并保留 `ThreadLocal` 重入保护、原 bytes 回退和原始 `defineClass0` MethodHandle，避免 `ClassCircularityError` 或递归再次定义。
- `InstrumentationImplTransformUtil` 是正式启用的启动保护：`SporeMixinPlugin` 必须定义该 transformer 类并调用 `InstrumentationImplTransformUtil.INSTANCE.inspectInstrumentationImpl()`。该入口必须先完成 agent attach 和 bootstrap `SporeAgent.getRealByte(...)` bridge 检查，再优先通过 JVMTI 注册/重转换 `sun.instrument.InstrumentationImpl`，失败时回退 Instrumentation；不能仅保留源码而移除启用调用。
- `SporeFrameClassWriter` 必须保留基于 class resource/缓存的 `getCommonSuperClass`、接口/数组及隐藏类 `/0x`/`+0x` 名称兼容，避免帧计算通过 `Class.forName` 触发加载或对隐藏类解析失败。
- 所有核心 transformer 必须继续调用 `SporeTransformerDebugDump.rememberTransformed(...)`；bootstrap 的 Instrumentation/JVMTI 失败分支必须调用 `dumpFailedTransform(...)`，保留 input/transformed class 与元数据，以便定位无 message 的 `VerifyError`。
- `LivingEntityHealthLifecycleWrapperUtil` 的死亡 tick 包装逻辑应保留 `forceDeathTimeIncreasing` 语义：不是粗暴把 tick 改成只调用 death tick，而是在原 tick 后强制死亡时间继续增长。
- `LivingEntityMixin` 的 heal redirect 必须保留：常规 `setHealth` 后补调用 `EntityHeealuthManager.INSTANCE.heal(...)`，但存在 `Seffects.HEALING_INHIBITION` 时跳过。
- `LocalPlayerMixin` 必须继续把本地玩家重新标记为 alive，以配合本地死亡包装实体。

## 3. 血量 Manager 与 FloatEntry

必须保留的核心类：

- `Core/asmHooks/EntityHeealuthManager.java`
- `Core/asmHooks/SporeEntityHeeaafastthManager.java`
- `Core/asmHooks/IEntityHealth.java`
- `Core/asmHooks/ISporeEntityHealth.java`
- `Core/asmHooks/FloatEntry.java`
- `Core/asmHooks/IFloatEntry.java`
- `Core/asmHooks/IFloatEntryFactory.java`
- `Core/asmHooks/NaN.java`
- `Core/asmHooks/NegativeInfinity.java`
- `Core/asmHooks/Zero.java`
- `Sentities/BaseEntities/IFakeDataHealthEntity.java`
- `Sentities/BaseEntities/ICalamityMultipart.java`
- `Sentities/BaseEntities/HohlMultipart.java`
- `Sentities/BaseEntities/LeviathanMultipart.java`

同步验证：

- manager 内直接存 `Float` 的 map 不应恢复；血量和最大血量应通过 `Map<..., IFloatEntry>` 保存。
- `FloatEntry` 要保留隐藏实例、特殊值单例和拆分 int bits 的实现；构造时 upper/lower 带偏移，读回时加回偏移再恢复 float。
- `SporeEntityHeeaafastthManager.getHealthOwner(...)` 必须识别 `ICalamityMultipart` 并通过 `getCalamityHead()` 归属到头部灾难实体。
- `getMaxHeeaafastth(...)`、`setMaxHeeaafastth(...)`、`getHeeaafastth(...)`、`setHeeaafastth(...)` 都要先解析 health owner。
- 所有设置最大生命值的逻辑必须和 `SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(...)` 同步进行。重点审计 `getAttribute(Attributes.MAX_HEALTH)` + `setBaseValue(...)`、`computeAttribute(Attributes.MAX_HEALTH, ...)`、按字符串/注册表遍历到 `Attributes.MAX_HEALTH` 后修改属性等路径；同步代码最好放在 `if (health != null)` 块外，保证原版 `MAX_HEALTH` 属性不存在时也能写入 Spore 实际最大生命值。
- `IFakeDataHealthEntity` 的陷阱血量必须保留：正常伤害路径不增加额外血量，外部直接写 `DATA_HEALTH_ID`/默认 0 delta 后会进入异常状态；正常 `hurrt` 后要调用 `hurtDellta(...)`，移除时要 `clearHllealthDelta()`。

## 4. 攻击、武器、弹射物与直接 hurt 审计

必须保留的核心类：

- `Core/utils/attack/SporeAttackUtil.java`
- `Core/utils/attack/IAttack.java`
- `Core/utils/SporeJudge.java`
- `Core/utils/ASMHurtArrowUtil.java`
- `Core/utils/IASMHurtArrow.java`
- `Sentities/BaseEntities/UtilityEntity.java`
- `Sentities/BaseEntities/Infected.java`
- `Sentities/TrueCalamity.java`
- `Sentities/AI/ASMSetHealthMeleeAttackGoal.java`
- `Sentities/AI/CustomMeleeAttackGoal.java`
- `Sentities/AI/AOEMeleeAttackGoal.java`
- `Sitems/BaseWeapons/SporeWeaponData.java`
- `Sitems/BaseWeapons/SporeToolsBaseItem.java`
- `Sitems/InfectedCrossbow.java`
- `Sitems/InfectedGreatBow.java`
- `Effect/HealingInhibition.java`
- `Core/Seffects.java`
- `Core/utils/effects/IEffectManager.java`
- `Core/utils/effects/SporeEffectsUtil.java`
- `Core/agents/transformers/SporeLivingEntityEffectApplicationTransformer.java`

重点迁移对象：

- 枪械和投掷物：`AbstractGunProjectile`、`ToxinBullet`、`AdaptableProjectile`、`BileProjectile`、`HarpoonProjectile`、`SyringeProjectile`、`ThrownBlockProjectile`、`ThrownBoomerang`、`ThrownKnife`、`ThrownSickle`、`ThrownSpear`、`ThrownTumor`、`VomitHohlBall`、`DrownedFleshBomb`。
- 实体/工具伤害：`Calamity` 碾压、`HohlMultipart`、`DragonHead`、`Verfalldrachen`、`Grober`、`NukeEntity`、`Utilities.explodeCircle`、`Mycelium` 真伤补偿、`l2Hostility/ASMHurtKillerAuraTrait`。

同步验证：

- `UtilityEntity` 和 `Infected` 两条实体基类路线必须保留自定义目标字段 `sporeTarget`，并覆盖 `getTarget()`/`setTarget(...)`；不要回退为直接使用原版 `Mob.target` 字段。
- `setTarget(...)` 必须用 `SporeJudge.isSporeEntity(...)` 拒绝 Spore 实体目标。若子类覆盖 `setTarget(...)` 并带有解除休眠、解除 rooted、潜行、隐身等副作用，也要先做同样过滤，避免被拒绝的 Spore 目标仍触发副作用。
- `TrueCalamity.hurt(CalamityMultipart, DamageSource, float)` 的灾难部位弱点逻辑必须保留。旧灾难中 Gazenbrecher、Grakensenker、Hinderburg、Howitzer、Leviathan、Sieger、Stahlmorder 的特定部位命中要额外调用 `SporeEntityHeeaafastthManager.INSTANCE.hurrt(...)` 直接扣除灾难实际血量；不要把 Verfalldrachen 纳入这条必保规则。
- 对 `LivingEntity` 的额外伤害应走 `SporeAttackUtil.INSTANCE.attack(...)`，以同时更新 ASM 血量 manager、战斗记录、死亡状态和同步包。
- `InfectedCrossbow` 和 `InfectedGreatBow` 的 BEZERK 变种必须在生成箭矢后调用 `ASMHurtArrowUtil.INSTANCE.wrap(...)`。这是武器/弹射物额外伤害路径，不只是隐藏类工具：`ASMHurtArrowUtil` 要生成隐藏 wrapper 覆写 `m_5790_(EntityHitResult)`，先调用 `onHitEntityHook(...)`，再调用 `super.m_5790_(...)`，hook 内额外伤害应走 `SporeAttackUtil.INSTANCE.attack(...)`。
- 武器命中入口应保留 `SporeAttackUtil.INSTANCE.attack(...)`，并保留 `Healing Inhibition` 效果附加逻辑。
- `HEALING_INHIBITION` 注册、贴图 `assets/spore/textures/mob_effect` 和多语言条目不能丢。
- 禁疗强制塞入机制必须保留：`Core/utils/effects/IEffectManager.java`、`Core/utils/effects/SporeEffectsUtil.java` 和 `SporeLivingEntityEffectApplicationTransformer` 是核心类；transformer 要覆盖 bad mod `LivingEntity` 子类的 `addEffect`/`forceAddEffect` 阻断路径，并保留 `getActiveEffects`、`getActiveEffectsMap`、`hasEffect`、`getEffect` 返回值 hook。
- `SporeWeaponData.addHealingInhibitRandom(...)` 必须通过 `SporeEffectsUtil.INSTANCE.forceAddEffect(...)` 强行塞入 `HEALING_INHIBITION`，不能退回普通 `target.addEffect(...)`。
- 保留有意的原版 `hurt`：实体自身覆写 `hurt` 内的 `super.hurt`、multipart 把伤害转发给 parent/head、某些非 LivingEntity 或原版方块/环境伤害路径。每轮同步后用 `rg -n "\.hurt\(|setHealth\(|attack\(" src/main/java` 做差异审计。

### 隐藏物品逐类同步规则

物品同步不能只比较 `Sitems` 中的注册表字段。必须逐一检查每个物品实现类，以及它在 `Sitems.hiddenItem(...)`、隐藏 Spawn Egg 或其他隐藏实例工厂中的创建路径。

- 先把当前本地隐藏物品版本作为基线，与上游同名物品的实现逐项比较：构造参数、`Item.Properties`、食物/装备属性、交互行为、伤害逻辑、注册名和外部可见行为都必须保持一致。
- 同步前必须重新确认“隐藏化改动”仍然存在，包括去掉会触发原始物品类加载的 `static` 状态、lambda、内部类、匿名类、`private record`，以及不必要的直接物品类引用。不要因为上游文件看起来更短，就把普通 `new ItemSubclass(...)` 恢复到已有隐藏物品注册中。
- 如果上游版本与本地隐藏版本在行为上没有明显区别，直接保留并应用现有隐藏物品版本；不要仅因代码形态不同而恢复上游的普通类实现。
- 如果已有隐藏物品的上游变化增加了逻辑，先判断新增内容是否会重新引入上述加载风险。少量 lambda、很短的匿名类或少量简单内部类可以改写成隐藏类可用的形式后迁移；复杂逻辑不要强行改写。
- 对已有隐藏物品，若为了保持隐藏类兼容而需要进行的改写过于复杂，则保留本地隐藏版本，不应用该物品的上游修改，并在同步报告中明确指出该类因隐藏类转换复杂而未同步。不能只同步一半导致行为和隐藏结构都不完整。
- 对新增物品，若其实现可以按现有隐藏模式安全拆解，则使用隐藏物品工厂；若其实现包含复杂的静态状态、lambda、内部/匿名类或 `private record`，不要勉强隐藏化，改用普通 `new` 注册，并记录这是新增物品的有意例外。
- “行为相同”必须同时包括注册时机和类加载边界：隐藏物品注册阶段尽量只保留类名、构造器签名和必要参数，不能为了比较方便而直接引用实现类、初始化原始物品类或让原始物品类提前加载。

建议把以下经验阈值作为复杂度初筛，而不是替代人工判断：只有一处且逻辑少于五行的简单 lambda 通常可以改写；匿名类少于两个且每个内部逻辑少于五行时通常可以迁移；新增内部类少于三个且职责单一时可以评估迁移。超过这些范围，尤其是多个相互引用的匿名类/内部类、复杂静态初始化或携带状态的 `private record`，应按复杂隐藏类改动处理。

每轮同步后必须能回答以下问题：所有物品类是否逐一审计；已有隐藏物品是否仍通过隐藏工厂创建；新增或修改的逻辑是否引入会加载原始类的结构；被保留或改为普通实例的类是否有明确原因。可用 `rg -n "hiddenItem|hiddenSpawnEgg|ITEMS\\.register|new .*Item|static|private record" src/main/java/com/Harbinger/Spore/Core/Sitems.java src/main/java/com/Harbinger/Spore/Sitems -g "*.java"` 进行初筛，但最终结论必须结合类实现阅读。

## 5. 实体存储替换与简单移除

必须保留的核心类：

- `Core/utils/simpleRemoval/SimpleRemoveUtil.java`
- `Core/utils/simpleRemoval/NaNVec3.java`
- `Core/utils/simpleRemoval/NaNAABBClass.java`
- `Core/utils/simpleRemoval/InfiniteBlockPos.java`
- `Core/utils/simpleRemoval/InfiniteChunkPos.java`
- `Core/utils/simpleRemoval/InfiniteMutableBlockPos.java`
- `Core/entityStorages/SporeEntityLookup.java`
- `Core/entityStorages/SporeEntityByIdMap.java`
- `Core/entityStorages/SporeEntityByUuidMap.java`
- `Core/entityStorages/SporeTrackedEntityMap.java`
- `Core/entityStorages/ProtectedEntityMapBase.java`
- `Core/entityStorages/ProtectedTrackedEntityMapBase.java`
- `Core/entityStorages/SporeKnownUuidsHashSet.java`
- `Core/entityStorages/SporeEntityGetter.java`
- `Core/entityStorages/SporeEntitySection.java`
- `Core/entityStorages/SporeEntitySectionStorage.java`
- `Core/entityStorages/serverSide/SporeServerLevel.java`
- `Core/entityStorages/serverSide/SporePersistentEntitySectionManager.java`
- `Core/entityStorages/serverSide/SporeDedicatedServer.java`
- `Core/entityStorages/serverSide/SporeIntegratedServer.java`
- `Core/entityStorages/clientSide/SporeClientLevel.java`
- `Core/entityStorages/clientSide/SporeTransientEntitySectionManager.java`
- `Core/entityStorages/clientSide/SporeMinecraftClient.java`
- `Core/entityStorages/GameTickerUtil.java`
- `Core/entityStorages/SporeServerEntityCallback.java`
- `Core/entityStorages/SporeClientEntityCallback.java`
- `Core/entityStorages/SporeEntityInLevelCallback.java`

同步验证：

- `SporeEntityHeeaafastthManager.replaceEntityMap(...)` 必须替换 `EntityLookup`、`byId`、`byUuid`、`knownUuids`、`ChunkMap.entityMap`。
- `SporeTrackedEntityMap` 的隐藏实例必须通过 base 类承载共享逻辑，避免隐藏类 owner 校验错误。
- 所有 storage 的 `put/get/contains/values/entrySet/forEach` 等路径都要调用 `SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(...)`，封锁被移除实体读写和遍历。
- `SimpleRemoveUtil.removeLocal(...)` 对非 Spore 实体要设置 removal reason、停止乘骑、触发 remove callback、替换实体存储、写入 NaN 位置/AABB、创建 wrapper，并通过 despawn/reset render 包同步客户端。
- `AllReturnUtil` 必须支持 `Vec3` 默认返回 `SimpleRemoveUtil.INSTANCE.getNaNPosition()`，并保留 UUID、BlockPos、BlockState、SynchedEntityData 等特化。

## 6. IDieWithDiscardEntity、Proto/Womb/灾难死亡移除

必须保留的核心类：

- `Sentities/BaseEntities/IDieWithDiscardEntity.java`
- `Sentities/BaseEntities/Calamity.java`
- `Sentities/Organoids/Proto.java`
- `Sentities/Organoids/Womb.java`
- `network/SyncLegalPositionPacket.java`
- `network/SyncLegalPositionPacketHandler.java`

同步验证：

- `Calamity`、`Proto`、`Womb` 必须实现 `IDieWithDiscardEntity` 或保留等价机制。
- `tickLegalPosition()` 要继续维护最后合法坐标，并在必要时把 server/client level 或 entity manager 替换为 Spore 版本。
- `syncAtFinalizeSpawn()` 和 `SyncLegalPositionPacket` 要保留，以免客户端缺失最后合法位置。
- storage callback 遇到未 special death 的 `IDieWithDiscardEntity` 时要走 `specialDie(...)`，不能被普通移除吞掉。

## 7. 网络、命令、SavedData、自定义 EventBus

必须保留的核心类和入口：

- `ExtremelySusThings/SporePacketHandler.java`
- `network/HealthDataPacket.java`
- `network/HealthDeltaPacket.java`
- `network/WrapperPacket.java`
- `network/DespawnPacket.java`
- `network/ResetRenderRequest.java`
- `network/SyncLegalPositionPacket.java`
- `sEvents/SporeEventBus.java`
- `sEvents/ISporeEventBus.java`
- `sEvents/HandlerEvents.java`
- `ExtremelySusThings/SporeSavedData.java`
- `Spore.java`

同步验证：

- `Spore` 构造器必须调用 `SporeEventBus.tick().addSelfListener()`，并把 `HandlerEvents.onMobEffectAdded` 注册到 Forge event bus。
- `SporeEventBus` 必须继续拦截已移除实体相关事件，并在 tick 中驱动 `SimpleRemoveUtil.tickServer/tickClient`、`SporeEntityHeeaafastthManager.tick()`、`EntityHeealuthManager.tick()`。
- `SporeEventBus` 必须继续拦截 `MobEffectEvent.Applicable`：遇到 `HEALING_INHIBITION` 时阻止原版路径，并调用 `SporeEffectsUtil.INSTANCE.forceAddEffect(...)`；还必须拦截 `MobEffectEvent.Remove`，禁止手动移除禁疗效果，只允许倒计时自然失效。
- `Spore` 必须把 `SporeEffectsUtil.INSTANCE` 注册到 Forge `LivingEvent.LivingTickEvent`，以便它遍历 `ISporeMap` 管理的 `activeEffects` 并用 `actualRemove()` 手动清除过期效果。
- 网络包必须注册：血量同步、delta 同步、wrapper、despawn、reset render、legal position。
- 命令必须保留：`spore:force_kill`、`spore:force_remove`、`spore:force_remove_all`、`spore:enable_light`，以及本地修改过的 `set_area`。
- `spore:enable_light <true|false>` 应写入 `SporeSavedData.get(serverLevel).setCasingLightAllowed(value)`，不是临时全局变量。
- `SporeSavedData` 必须保存/读取 `CasingLightAllowed`。

## 8. CasingGenerator、Proto 列表与发光菌毯

必须保留的核心类：

- `Sentities/CasingGenerator.java`
- `Sentities/Organoids/Proto.java`
- `Sblocks/CasingBiomassBlock.java`
- `ExtremelySusThings/SporeSavedData.java`

同步验证：

- `CasingGenerator.getProtoLevel()` 默认可空，`Proto` 必须返回自身 `level()`。
- `CasingGenerator.withCasingLight(...)` 必须在服务端、`SporeSavedData.isCasingLightAllowed()` 为 true 时，以 30% 概率把支持 `CasingBiomassBlock.LIT` 的候选方块设为亮。
- `Proto.possibleBlocks()` 和 `Proto.fungalStalkBlocks()` 应返回实例级 final `List.of(...)`，不能每次 new `ArrayList`。
- 判断候选方块时要把 lit 状态归一化为 false 后再比较。

## 9. Calamity 与 Grakensenker/Howitzer 寻路增强

必须保留的核心类：

- `Sentities/BaseEntities/Calamity.java`
- `Sentities/AI/CalamityPathNavigation.java`
- `Sentities/AI/CalamityPathTypePolicy.java`
- `Sentities/AI/IPathTypePolicy.java`
- `Sentities/AI/AmphibianCalamityNodeEvaluator.java`
- `Sentities/AI/GrakensenkerPathNavigation.java`
- `Sentities/Calamities/Grakensenker.java`
- `Sentities/Calamities/Howitzer.java`

同步验证：

- `Calamity.forceStart(Goal)` 不能退回成只反射调用 `"start"`。必须保留混淆名优先的启动链：先用 `Goal#m_8056_()` 的 `MethodHandle`，再退回 `Goal#start()` 的 `MethodHandle`，再用混淆名/反混淆名反射，最后才直接 `goal.start()`。这是冰冻触发 `SporeBurstSupport` 的关键路径。
- `CalamityPathNavigation` 必须保留 detour 栈、临时目标、stuck 诊断、终点/水节点恢复、`recomputePath()` 包装和水节点短期黑名单。
- `CalamityPathTypePolicy` 必须保留陆地/天空灾难避开水、岩浆、粉雪，以及水灾难陆上/水中不同策略。Gazenbrecher 这类火适应实体不能被误判为必须避开岩浆。
- 水灾难应使用 `AmphibianCalamityNodeEvaluator`：陆上走陆地评估，水中走水中评估，避免水陆交界处一直使用不合适的 evaluator。
- `Grakensenker` 必须继续使用 `GrakensenkerPathNavigation`，且 detour 只影响陆行分支，水中追击/触手/跳跃逻辑不能被陆行 detour 覆盖。
- `Howitzer` 必须继续使用 `PausableCalamityPathNavigation`。`HowitzerRangedAttackGoal` 在站桩射击时应 `pause()` 寻路、停止或离开射击状态时 `resume()`，并在射击 goal 结束时通过一次直接 `moveTo(target, speed)` 重算路径；不要退回到旧的 moveTo/stop 冷却判定或每 tick 高频切换导航。
- `setPathfindingMalus(...)` 的上游覆盖要审计，确保本地对可破坏方块、液体、粉雪等成本调整仍然生效。

## 10. 其它同步风险点

- 构建产物名应由 `gradle.properties` 的 `mod_id=spore`、`mod_version=1.0-SNAPSHOT` 和 `build.gradle` 的 `archivesName = mod_id` 控制；若变成 `examplemod-1.0.0.jar`，说明 Gradle 模板属性被上游覆盖。
- 语言资源中 `effect.spore.healing_inhibition` 必须保留，中文为“愈合抑制”，英文为 `Healing Inhibition`，日语/俄语等按已迁移含义保留。
- `assets/spore/textures/mob_effect` 中禁止回血效果图标必须保留。
- `mods.toml` 的 `modId="spore"` 和版本信息不要被 examplemod 模板覆盖。

## 推荐审计命令

```powershell
git status --short --branch
git log --oneline --decorate --max-count=20
git diff --name-status origin/master...HEAD
rg -n "JVMTIPointerUtil|JvmtiMethod|JvmtiCapabilities|ClassFileLoadHook|retransformMaybeHiddenClasses|SporeHiddenDefineHookTransformer|HiddenDefineHook|InstrumentationImplTransformUtil|SporeFrameClassWriter|SporeTransformerDebugDump|installAndRetransform|SporeEventBus|SporePacketHandler|SimpleRemoveUtil|SporeEntityLookup|SporeTrackedEntityMap|SporeKnownUuidsHashSet|unremovableCollections|SporeMapProxy|ISporeMap|ISporeEntry|FloatEntry|ICalamityMultipart|IDieWithDiscardEntity|TrueCalamity|SporeEntityHeeaafastthManager\.INSTANCE\.hurrt|sporeTarget|SporeJudge\.isSporeEntity|ASMHurtArrowUtil|InfectedCrossbow|InfectedGreatBow|HEALING_INHIBITION|SporeEffectsUtil|IEffectManager|SporeLivingEntityEffectApplicationTransformer|addHealingInhibitRandom|enable_light|CasingLightAllowed|forceStart|m_8056_|CalamityPathNavigation|GrakensenkerPathNavigation|HowitzerRangedAttackGoal|PausableCalamityPathNavigation" src/main/java src/main/native src/main/resources -S
git ls-files src/main/resources/sporeAgent.jar src/main/resources/sporeTransformerBridge.dll src/main/native
rg -n "\.hurt\(|hurrt\(|setHealth\(|attack\(|ASMHurtArrowUtil\.INSTANCE\.wrap|setMaxHeeaafastth|getTarget\(|setTarget\(" src/main/java -S
rg -n "getAttribute\(Attributes\.MAX_HEALTH\)|computeAttribute\(Attributes\.MAX_HEALTH|Attributes\.MAX_HEALTH|setBaseValue\(" src/main/java -S
.\gradlew --no-daemon --console=plain compileJava
```

## 完成标准

- `compileJava` 通过。
- 关键入口 `Spore.commonSetup`、`SporeEventBus.tick().addSelfListener()`、`SporePacketHandler.registerPackets()` 全部存在。
- JVMTI/Instrumentation 双后端、native `ClassFileLoadHook`、`sporeAgent.jar`、C 源码/JNI header/当前 DLL 均有代码和跟踪证据；若 native 源码或签名变化，DLL 已同步重编译。
- 已定义隐藏类兜底重转换入口仍由 `HeasdalthUtil` 调用，并保留依赖预加载、flag 临时清除/`finally` 恢复、独立 transformer 安装状态与二分失败隔离。
- 定义前转换链有完整入口证据：`spore.mixins.json -> SporeMixinPlugin -> HiddenDefineHook.inspectHiddenDefine -> SporeHiddenDefineHookTransformer -> HiddenDefineHook -> health/effect transformers`；反射判定内联且 `HiddenDefineHook` 有 `ThreadLocal` 重入保护。
- `SporeFrameClassWriter` 和 `SporeTransformerDebugDump` 仍被核心 transformer/bootstrap 实际调用；`SporeMixinPlugin` 中有 `InstrumentationImplTransformUtil.INSTANCE.inspectInstrumentationImpl()` 的当前启用证据，且该入口保留 JVMTI 优先、Instrumentation 回退及实际 Hook 应用成功判定。
- `UtilityEntity`/`Infected` 自定义 `sporeTarget` 目标字段、`getTarget()`/`setTarget(...)` 覆盖和 Spore 目标过滤全部有代码证据。
- 旧灾难的 `TrueCalamity.hurt(CalamityMultipart, DamageSource, float)` 部位弱点额外直接扣血逻辑有代码证据：Gazenbrecher、Grakensenker、Hinderburg、Howitzer、Leviathan、Sieger、Stahlmorder 均保留 `SporeEntityHeeaafastthManager.INSTANCE.hurrt(...)` 调用；Verfalldrachen 不属于这条必保规则。
- `InfectedCrossbow`/`InfectedGreatBow` 的 BEZERK 箭矢包装链有代码证据：生成的 `AbstractArrow`/projectile 调用 `ASMHurtArrowUtil.INSTANCE.wrap(...)`，wrapper 的 `m_5790_` hook 额外伤害走 `SporeAttackUtil.INSTANCE.attack(...)`。
- 血量、最大血量、heal redirect、禁止回血效果、fake data health、multipart owner、IDieWithDiscardEntity 特殊死亡全部有代码证据。
- `unremovableCollections` 代理集合、`ISporeEntry.actualSetValue`、`ISporeMap.actualPut/actualRemove`、iterator `actualRemove()` 等封锁/可信写入入口全部有代码证据。
- 禁疗效果管理链路全部有代码证据：`SporeEffectsUtil`/`IEffectManager`、`SporeLivingEntityEffectApplicationTransformer`、bootstrap transformer 注册、`SporeEventBus` 添加/移除事件处理、`Spore` 注册 tick listener、过期效果 `actualRemove()` 清理、`SporeWeaponData.addHealingInhibitRandom` 强制 `forceAddEffect`。
- 运行期最大生命值变更有配对证据：每个 `Attributes.MAX_HEALTH` 的 `setBaseValue(...)` 或等价 helper 都有同路径 `SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(...)`，且同步不依赖原版属性非空。
- 实体 storage 替换覆盖 server/client lookup、id map、uuid map、known UUID set、tracked entity map、section/callback。
- 额外伤害路径已迁移到 `SporeAttackUtil` 或明确标记为有意保留的原版主击中路径。
- Calamity `forceStart` 混淆名优先启动链、Grakensenker 陆行 detour、Howitzer `PausableCalamityPathNavigation` 站桩射击暂停/恢复寻路机制仍在当前上游结构中生效。
- native DLL、贴图、语言、mods.toml、Gradle 属性没有被上游模板覆盖。
