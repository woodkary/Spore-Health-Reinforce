# Entity422 Remote Sync Core Rules

本文档是本仓库同步 Entity422/Entity442 上游后必须保留和验证的本地核心增强清单。同步完成不能只看 `compileJava` 是否通过；必须按机制检查入口、隐藏类、ASM hook、实体存储替换、网络包、命令、资源和特化寻路是否仍然连成完整链路。

## 基本原则

- 保留本地增强优先级高于上游同名文件的简单覆盖。遇到冲突时，先确认上游真实行为，再把本地增强重新迁移到新结构中。
- 不要把 `com.Harbinger.Spore.Core`、`com.Harbinger.Spore.mixin`、`com.Harbinger.Spore.network`、`com.Harbinger.Spore.sEvents` 当作普通业务代码处理；这些包里有运行时替换、隐藏类、ASM、保存数据和实体封锁逻辑。
- 每次同步后至少执行 `git status --short --branch`、`git diff --name-status <base>...HEAD`、`rg` 关键字审计和 `.\gradlew --no-daemon --console=plain compileJava`。
- 直接 `Entity#hurt`/`LivingEntity#hurt` 不是都要替换。实体自身 `super.hurt(...)`、multipart 转发到父实体、原版主击中路径通常可以保留；额外伤害、弹射物、武器、AOE、效果 tick、绕过原版血量系统的补伤害应优先走 `SporeAttackUtil.INSTANCE.dealDamage(...)` 或对应 manager。

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
- `Core/utils/ProtectedConcurrentHashMap.java`
- `Core/utils/ProtectedWeakHashMap.java`
- `Core/utils/wrappedMethod/WrappedMethod.java`
- `Core/utils/wrappedMethod/IWrappedMethod.java`
- `Core/utils/StackTraceUtil.java`
- `Core/utils/ParentUtil.java`
- `Core/utils/TargetUtil.java`

同步验证：

- `BytecodeUtil.createHiddenSingletonInstance(...)`、`resolveHiddenClassOrSelf(...)`、隐藏构造器路径不能退化为普通 `new`。
- `ClassUtil` 的隐藏类定义、字段读写、类替换辅助必须保留；如果出现 `VerifyError: Bad type on operand stack`，优先检查隐藏子类是否把 owner 写成了非隐藏宿主类。
- `WrappedMethod`/`MethodHandle` 缓存要按实际签名调用，不能把 `MethodHandle(ServerPlayer,DamageSource)void` 之类的强类型 handle 当成 `(Object[])Object` 直接 invoke。
- `Protected*Map` 用于封锁写入/读取/遍历，不能被上游集合替换覆盖。

## 2. 生命周期、ASM 血量、死亡包装

必须保留的入口和核心类：

- `Spore.java`
- `Core/agents/AgentBridge.java`
- `Core/agents/InstrumentationUtil.java`
- `Core/agents/transformers/SporeClassFileTransformer0.java`
- `Core/agents/transformers/SporeNativeBridge.java`
- `Core/agents/transformers/SporeLivingEntityHealthTransformer.java`
- `Core/agents/transformers/SporeLivingEntityHealthTransformerBootstrap.java`
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
- `SporeNativeBridge` 依赖 `/sporeTransformerBridge.dll`，同步或清理资源时不能丢失 DLL。
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
- 所有修改 `Attributes.MAX_HEALTH` 的逻辑，如果原版属性更新后仍要影响 Spore 实际最大生命，必须额外同步到 `SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(...)`。同步代码最好放在 `if (health != null)` 块外。
- `IFakeDataHealthEntity` 的陷阱血量必须保留：正常伤害路径不增加额外血量，外部直接写 `DATA_HEALTH_ID`/默认 0 delta 后会进入异常状态；正常 `hurrt` 后要调用 `hurtDellta(...)`，移除时要 `clearHllealthDelta()`。

## 4. 攻击、武器、弹射物与直接 hurt 审计

必须保留的核心类：

- `Core/utils/attack/SporeAttackUtil.java`
- `Core/utils/attack/IAttack.java`
- `Sentities/AI/ASMSetHealthMeleeAttackGoal.java`
- `Sentities/AI/CustomMeleeAttackGoal.java`
- `Sentities/AI/AOEMeleeAttackGoal.java`
- `Sitems/BaseWeapons/SporeWeaponData.java`
- `Sitems/BaseWeapons/SporeToolsBaseItem.java`
- `Effect/HealingInhibition.java`
- `Core/Seffects.java`

重点迁移对象：

- 枪械和投掷物：`AbstractGunProjectile`、`ToxinBullet`、`AdaptableProjectile`、`BileProjectile`、`HarpoonProjectile`、`SyringeProjectile`、`ThrownBlockProjectile`、`ThrownBoomerang`、`ThrownKnife`、`ThrownSickle`、`ThrownSpear`、`ThrownTumor`、`VomitHohlBall`、`DrownedFleshBomb`。
- 实体/工具伤害：`Calamity` 碾压、`HohlMultipart`、`DragonHead`、`Verfalldrachen`、`Grober`、`NukeEntity`、`Utilities.explodeCircle`、`Mycelium` 真伤补偿、`l2Hostility/ASMHurtKillerAuraTrait`。

同步验证：

- 对 `LivingEntity` 的额外伤害应走 `SporeAttackUtil.INSTANCE.dealDamage(...)`，以同时更新 ASM 血量 manager、战斗记录、死亡状态和同步包。
- 武器命中入口应保留 `SporeAttackUtil.INSTANCE.attack(...)`，并保留 `Healing Inhibition` 效果附加逻辑。
- `HEALING_INHIBITION` 注册、贴图 `assets/spore/textures/mob_effect` 和多语言条目不能丢。
- 保留有意的原版 `hurt`：实体自身覆写 `hurt` 内的 `super.hurt`、multipart 把伤害转发给 parent/head、某些非 LivingEntity 或原版方块/环境伤害路径。每轮同步后用 `rg -n "\.hurt\(|setHealth\(|dealDamage\(" src/main/java` 做差异审计。

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
- `Howitzer.HowitzerRangedAttackGoal` 必须保留 moveTo/stop 冷却和目标路径复用，避免每 tick 高频切换导航导致服务器卡顿。
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
rg -n "installAndRetransform|SporeEventBus|SporePacketHandler|SimpleRemoveUtil|SporeEntityLookup|SporeTrackedEntityMap|SporeKnownUuidsHashSet|FloatEntry|ICalamityMultipart|IDieWithDiscardEntity|HEALING_INHIBITION|enable_light|CasingLightAllowed|forceStart|m_8056_|CalamityPathNavigation|GrakensenkerPathNavigation|HowitzerRangedAttackGoal" src/main/java src/main/resources -S
rg -n "\.hurt\(|setHealth\(|dealDamage\(|setMaxHeeaafastth" src/main/java -S
.\gradlew --no-daemon --console=plain compileJava
```

## 完成标准

- `compileJava` 通过。
- 关键入口 `Spore.commonSetup`、`SporeEventBus.tick().addSelfListener()`、`SporePacketHandler.registerPackets()` 全部存在。
- 血量、最大血量、heal redirect、禁止回血效果、fake data health、multipart owner、IDieWithDiscardEntity 特殊死亡全部有代码证据。
- 实体 storage 替换覆盖 server/client lookup、id map、uuid map、known UUID set、tracked entity map、section/callback。
- 额外伤害路径已迁移到 `SporeAttackUtil` 或明确标记为有意保留的原版主击中路径。
- Calamity `forceStart` 混淆名优先启动链、Grakensenker/Howitzer 寻路增强仍在当前上游结构中生效。
- native DLL、贴图、语言、mods.toml、Gradle 属性没有被上游模板覆盖。
