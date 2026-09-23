# Entity422 Remote Sync Core Rules

本文档记录本仓库同步 Entity422/Entity442 上游时需要保护的本地机制。项目是 Minecraft 1.20.1 / Forge 47 的 Spore 分支：`Spore` 注册内容、事件与启动转换器；`Core` 负责隐藏类、ASM/JVMTI 血量与效果、实体存储和移除；`Sentities`、`Sitems` 等实现玩法；网络包、SavedData 和资源维持客户端与服务端行为。编译通过只证明代码可以编译，不能证明这些运行时链路仍然有效。

本文档是风险索引，不是每次同步都要逐项执行的固定脚本。先看下方“同步流程”，再按变更面阅读相关章节：1 隐藏类与集合；2 生命周期、转换器与 native；3 血量；4 攻击与隐藏物品；5–6 实体存储和特殊死亡；7–8 事件、网络、存档与资源；9 寻路；10 构建配置。章节中的类名和调用形式描述当前实现；若上游重构带来等价实现，按可验证的行为和调用链判断，不因名称变化直接判为回归。

## 同步流程

1. **确认来源和基线。** 查看工作区状态、现有 remote、分支和提交。当前 `origin` 指向本地增强仓库，不能把 `origin/master` 当成 Entity422 上游。根据用户指定的仓库、分支或提交确定上游 ref；缺少来源或无法取得目标提交时，完成本地可做的审计并明确指出所缺信息。保留已有未提交改动，记录同步前本地 HEAD 与上游目标提交；必要时在独立分支或工作树集成。
2. **比较两边，再选择集成方式。** 用 `merge-base` 区分共同祖先、本地改动和待引入的上游改动；若已知上次同步的上游提交，再比较上游旧版与目标版。根据提交关系和任务选择 merge、cherry-pick 或逐项移植。先读行为变化及依赖，再处理冲突；不按文件名直接以任一侧整文件覆盖。
3. **按影响面审计。** 从上游改动文件、集成差异和调用/注册入口扩展到相关章节。核心机制跨包时检查完整入口到效果的链路；未受影响的长清单无需每次逐类重查。`rg` 只定位线索，结论要由代码、差异和必要的运行验证支持。对新增或改动的物品，连同注册工厂检查类加载边界。
4. **验证并交付。** 代码同步后先检查差异和编译，再针对受影响机制运行已有的聚焦验证；只有 native C/JNI 或签名变化时才要求重编译相应 DLL 并核对打包。需要游戏内、双端或特定模组环境才能验证的行为，说明已验证证据与未验证项。报告上游范围、集成方式、本地机制保留或等价替换的证据，以及尚未解决的冲突；不要把缺失的上游功能或只通过编译的状态称为完成。

## 基本原则

- 保留本地增强优先级高于上游同名文件的简单覆盖。遇到冲突时，先确认上游真实行为，再把本地增强重新迁移到新结构中。
- 不要把 `com.Harbinger.Spore.Core`、`com.Harbinger.Spore.mixin`、`com.Harbinger.Spore.network`、`com.Harbinger.Spore.sEvents` 当作普通业务代码处理；这些包里有运行时替换、隐藏类、ASM、保存数据和实体封锁逻辑。
- 比较范围必须对应实际来源：同步前分别看本地相对共同祖先的改动与上游待引入的改动；同步后再看相对同步前 HEAD 的集成结果。`origin/master...HEAD` 不能作为本仓库的固定上游差异命令。
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
- `Core/agents/transformers/SporeDiscoveredLifeCycleMethodTransformer.java`
- `Core/agents/transformers/SporeDiscoveredLifeCycleMethodRegistry.java`
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
- `Core/utils/LifeCycleInvocationInspector.java`
- `Core/utils/ILifeCycleInvocationInspect.java`
- `Core/utils/LifeCycleMethod.java`
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
- `LifeCycleInvocationInspector.inspectAndCacheLifeCycleInvocations(...)` 必须独立扫描 raw 原类的完整 `LivingEntity` 父链，不依赖 wrapper 是否能构建，也不能因 final 类或 final 方法跳过分析。检查缓存必须由实际 root 实体类和生命周期声明类共同确定，避免不同子类覆盖实现被父类缓存吞掉。`LivingEntityHealthLifecycleWrapperUtil` 不持有 inspector 的方法或证据缓存。
- `LifeCycleInvocationInspector.INSTANCE` 是隐藏副本；Inspector 必须保留一个显式 `Function` 实现供 `computeIfAbsent` 使用，不能在该类中用 lambda 或方法引用创建缓存工厂。其他缓存应使用显式 `get/put` 或定义在普通 util 类中的具名 `Function` 实现，避免隐藏类的 lambda 辅助类绑定回原类。
- `LifeCycleInvocationInspector` 对 `float`/`double` 调用保留跨至少两个 `LifeCycleKind` 的阈值，并登记为 `HEALTH`。boolean 调用只能使用精确 `m_6084_()Z` 与 `m_21224_()Z` 的证据；静态参数或实例 receiver 必须由 ASM 数据流证明来自 `this`，实例调用还必须排除名称已经符合 alive/dead-or-dying 的方法，并从实际 root 实体类向父类解析第一个具有方法体的具体实现 owner。实例 owner 解析必须优先使用 `ClassReflectionUtil.getDeclaredMethods(...)` 查看 Mixin 合并后的运行时父类方法，找不到时再回退原始 `ClassNode` 扫描；发现目标的注册与已加载类重转换筛选只排除 `com/Harbinger/Spore/` 自身类，不能用通用 mod 白名单排除 Minecraft/Forge/Mixin 目标类。
- boolean 极性必须由保守控制流分析证明，至少覆盖直接 `IRETURN`、`ISTORE/ILOAD`、`ICONST_1 + IXOR`、javac 的 `IFEQ/IFNE + ICONST_0/1 + GOTO` 物化形式及多个正常返回路径；混合或无法证明的路径必须产生 `UNKNOWN`。`m_6084_` DIRECT + `m_21224_` NEGATED 登记为 `ALIVE`，反向登记为 `DEAD_OR_DYING`，其余组合不得登记。
- `SporeDiscoveredLifeCycleMethodRegistry` 必须以 `LifeCycleMethodTarget(EntitySource, int[], LifeCycleMethodCategory)` 区分实体来源与 `HEALTH`/`ALIVE`/`DEAD_OR_DYING`。同一 owner + method key 的来源或类别冲突必须输出明确日志并使目标失效，不能依赖注册顺序静默选择。
- `SporeDiscoveredLifeCycleMethodTransformer` 的具体方法选择只依赖 registry 的 owner + method key，不得再额外按 owner 白名单或参数声明类型拒绝已注册方法。`HEALTH` 只匹配 `float`/`double` 的 `getHeealth` Object Hook；`ALIVE`/`DEAD_OR_DYING` 只匹配 boolean 的 `isAlliive`/`isDeeadfOrDyaging` Object Hook。静态目标使用已登记的引用参数，实例目标使用 `ALOAD 0`；必须保留访问标志、类别/返回值组合、幂等检查、`SporeFrameClassWriter` 和 `SporeTransformerDebugDump`。
- 发现方法重转换必须保留 Instrumentation 优先、JVMTI 回退、二分失败隔离、hidden Klass flag 临时清除和 `finally` 恢复。`HiddenDefineHook` 的定义前 transformer 链也必须包含 `SporeDiscoveredLifeCycleMethodTransformer`，避免后定义 hidden 工具类漏转换。
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

物品同步不能只比较 `Sitems` 中的注册字段。对上游新增、修改的物品，以及注册方式或共享构造路径受到影响的本地物品，检查实现类和 `Sitems.hiddenItem(...)`、隐藏 Spawn Egg 等实例工厂；其余物品可先核对注册入口是否保持原状，再按发现的依赖扩大范围。

- 以当前本地隐藏物品版本为基线，比较受影响物品在上游的构造参数、`Item.Properties`、食物/装备属性、交互与伤害、注册名和外部可见行为；区分上游有意新增的行为与无意回退。
- 同步前必须重新确认“隐藏化改动”仍然存在，包括去掉会触发原始物品类加载的 `static` 状态、lambda、内部类、匿名类、`private record`，以及不必要的直接物品类引用。不要因为上游文件看起来更短，就把普通 `new ItemSubclass(...)` 恢复到已有隐藏物品注册中。
- 如果上游版本与本地隐藏版本行为相同，可保留本地实现；不要仅因代码形态不同而恢复普通类构造。
- 如果上游增加了行为，先尝试在不破坏隐藏类加载边界的前提下移植，并检查直接引用、lambda、匿名/内部类、`private record` 和静态初始化是否引入加载风险。复杂度是设计和验证成本的信号，不是放弃上游变化的自动条件。
- 若完整保留上游新行为与现有隐藏化机制确实无法同时做到，先完成其他可独立集成的部分，列明此项未完成、具体冲突和可选实现，由用户决定取舍；不得静默丢弃上游行为或只迁移一半。
- 新增物品是否采用隐藏工厂，应根据它是否需要相同类加载边界及可验证的实现成本决定；普通 `new` 是有依据的例外，不能只因代码复杂就自动采用。
- “行为相同”必须同时包括注册时机和类加载边界：隐藏物品注册阶段尽量只保留类名、构造器签名和必要参数，不能为了比较方便而直接引用实现类、初始化原始物品类或让原始物品类提前加载。

同步后应能说明：受影响物品的行为和注册路径是否完整；已有隐藏物品是否仍通过隐藏工厂创建；新增结构是否触发原始类加载；普通实例的例外依据是什么。可用 `rg -n "hiddenItem|hiddenSpawnEgg|ITEMS\\.register|new .*Item|static|private record" src/main/java/com/Harbinger/Spore/Core/Sitems.java src/main/java/com/Harbinger/Spore/Sitems -g "*.java"` 定位，但最终结论必须结合变更类实现阅读。

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

## 按需审计命令

先确认实际的上游仓库与 ref，把下例中的字符串替换成取得的提交或远端分支。以下命令在同步前执行，保存输出中的本地 HEAD 和共同祖先；不要直接套用 `origin/master`。

```powershell
git status --short --branch
git remote -v
git log --oneline --decorate --max-count=20
$upstreamRef = '已确认的上游 ref'
$localHead = git rev-parse HEAD
$base = git merge-base $localHead $upstreamRef
git diff --name-status "${base}..${localHead}"
git diff --name-status "${base}..${upstreamRef}"
```

集成后用保存的 `$localHead` 执行 `git diff --name-status "${localHead}..HEAD"` 和 `git diff --check "${localHead}..HEAD"`，并用 `git status --short` / `git diff --check` 检查尚未提交的改动。若两边没有共同祖先，应明确这是无共同历史的移植并逐项比较，不能伪造 merge base。对受影响章节选取 `rg` 搜索词并阅读命中代码，不需要运行覆盖全仓的超长正则。代码改动完成后运行 `.\gradlew --no-daemon --console=plain compileJava`；若变更触及转换器、native 或游戏运行期行为，再做对应的聚焦验证。

## 完成判定

- 已说明上游来源、目标提交、共同祖先、同步前本地提交及实际集成范围；上游新增行为与本地增强均有明确处理结果。
- 受影响的章节已有入口、调用链或行为证据；存在等价重构时说明新旧机制的对应关系。搜索命中和编译结果不能单独替代这项判断。
- 编译及受影响机制的验证已完成；若 native 源码/JNI 签名变更，已重编译和核对 DLL。无法运行的验证、冲突和待用户选择的取舍明确标记为未完成。
