package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author karywoodOyo
 */
public class HealthDataPacket {
    int entityId;
    float health;
    boolean invokeAll;

    public HealthDataPacket(int entityId,float health,boolean invokeAll) {
        this.entityId = entityId;
        this.health = health;
        this.invokeAll = invokeAll;
    }

    // ✅ encode：写入多个实体数据
    public static void encode(HealthDataPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.health);
        buf.writeBoolean(msg.invokeAll);
    }

    // ✅ decode：读取多个实体数据
    public static HealthDataPacket decode(FriendlyByteBuf buf) {
        return new HealthDataPacket(buf.readInt(), buf.readFloat(), buf.readBoolean());
    }

    // ✅ handle：循环更新客户端实体
    public static void handle(HealthDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(msg.entityId);
                if (SporeJudge.isSporeEntity(entity)) {
                    SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastthLocal((LivingEntity) entity, msg.health);
                }else if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.setHealth(msg.health);
                }

            }
        });
        ctx.get().setPacketHandled(true);
    }
}
