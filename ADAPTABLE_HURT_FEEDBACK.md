# 适应实体受伤反馈接入

`DamageAdaptableEntity` 现在提供从命中反馈发送到客户端临时状态的公共实现。接口不提供适应算法，不依赖 `ICustomLifeCycleEntity`，也不自动接入伤害入口或 NBT。

## 实体最小接入

实体实现 `DamageAdaptableEntity`，提供实体本体和一个固定的每实例状态对象：

```java
private final AdaptableHurtFeedbackState hurtFeedbackState = new AdaptableHurtFeedbackState();

@Override
public LivingEntity entity() {
    return this;
}

@Override
public AdaptableHurtFeedbackState getHurtFeedbackState() {
    return hurtFeedbackState;
}
```

继承 `UtilityEntity` 时已有 `entity()` 实现，可以直接复用。新实体不必继承 `Proto` 或 `Organoid`。

实体继续自行实现 `float adaptDamage(DamageSource, float)` 及适应记忆。在逻辑服务端确定本次适应结果后，调用一次 `sendHurtFeedback(color)` 或 `sendHurtFeedback(color, durationTicks)`，再返回适应后的伤害。颜色来自自身算法的适应程度，不从吸收、护甲或最终扣血反推。`AdaptableHurtColor.fromMultiplier` 是可选的倍率分类工具，不规定实体的适应系数、分类或冻结规则。

发送默认持续 10 tick，时长限制为 1～40 tick。公共方法执行服务端检查，使用实体 ID/UUID 构造已有 `AdaptableHurtFeedbackPacket`，通过已有通道仅发送给追踪该实体的玩家。不需要新增包、注册或客户端实体类型分支。

客户端接收端在主线程确认接口类型和 UUID，然后调用默认 `applyClientHurtFeedback`。每次反馈都更新颜色和客户端世界 `gameTime` 截止时刻，同色反馈同样续时。查询过期状态返回无自定义反馈及普通红色。无需实体 tick 回调；状态不得存入 NBT、`SynchedEntityData` 或全局实体 ID Map。

## 仍由实体接入的钩子

- 使用现有 `UtilityEntity → actualHurt` 路径时，已存在适应调用及返回值使用，不要重复调用。
- 覆盖这条伤害链或使用其他基类时，必须在实际服务端伤害流程中调用一次 `adaptDamage` 并使用返回值；提前拒绝的攻击不应发送反馈。
- 需要持久化适应记忆时，实体仍需实现 `readAdaptData`/`addAdaptData`，并在自己的保存读取回调中显式调用。临时反馈对象不参与保存。
- Proto 继续保留原来的 NBT 回调、适应计算、分类、累计时机和冻结例外，当前系数为 `0.1275`：第 1 次红、第 2～8 次绿、第 9 次起紫。

## 客户端渲染器材质接入

渲染器明确声明原始材质到受伤材质的映射，例如：

```java
private static final Map<RenderType, RenderType> HURT_MATERIALS = Map.of(
    RenderType.entityCutoutNoCull(TEXTURE), AdaptableHurtRenderTypes.cutoutNoCull(TEXTURE),
    RenderType.itemEntityTranslucentCull(TEXTURE), AdaptableHurtRenderTypes.invisibleVisible(TEXTURE)
);

// 在已有 render 中，只包装传入的缓冲源，继续沿用父类流程。
MultiBufferSource hurtBuffers = AdaptableHurtRenderTypes.wrapBuffers(
    buffers, entity.getClientHurtFeedbackSnapshot(), HURT_MATERIALS
);
super.render(entity, yaw, partialTicks, poseStack, hurtBuffers, packedLight);
```

膜、帽子及其他模型材质按需加入映射；未声明的材质直接透传，包括名称、阴影、轮廓等。多贴图/多模型实体可以为各变体缓存各自的映射。Proto 的两套主体材质、膜、帽子和隐身可见分支已经迁移。

通用预设提供 `cutoutNoCull`、`translucent` 和 `invisibleVisible`。特殊材质可自行创建并缓存 RenderType：使用读取 overlay 的实体 shader，并绑定 `AdaptableHurtRenderTypes.overlayState()`，同时保留原始材质需要的混合、剔除、深度、写入掩码、排序和输出目标。尤其不要直接复用不读取 overlay 的 item shader。

每次渲染捕获不可变状态快照和材质映射。`AdaptableOverlayVertexConsumer` 将颜色写入各顶点的 overlay 坐标，适用于逐属性和合并顶点提交，不更改其他顶点属性。调色板稳定，不依赖逐实体修改全局颜色，也不增加模型重绘。调色板资源改为 `textures/misc/adaptable_hurt_overlay.png`，像素及 metadata 与原资源完全一致，通过 TextureManager 在实际绘制状态中绑定并参与常规资源重载。

## 验证范围

`compileJava processResources` 和 `test --tests 'com.Harbinger.Spore.feedback.*' jar`（包含 `reobfJar`）通过。

15 项定向测试覆盖：纯状态和时限、非 Proto 的公共默认方法/包/接收端、UUID 与 ID 复用保护、服务端发送边界、Proto 当前序列/冻结/新分类/零伤害/分类记忆、协议编解码、禁止客户端类的公共类型加载、任意显式材质映射、未声明材质透传、不可变快照、两套 Proto 材质、共享缓冲中的顶点隔离、原有绘制状态、调色板像素与 metadata。

接口默认方法、接收端及 Proto 算法测试执行编译后的生产字节码，仅用小型替身替换世界、实体和网络边界。材质测试初始化原版注册表并检查实际 RenderType，没有创建游戏窗口或实际提交 OpenGL 绘制。

尚未进行游戏内视觉、钻地/出土/动画、实际隐身与轮廓、多人同步、完整专用服务器启动、实际资源重载验证；上述定向测试不代表这些运行场景已实测。
