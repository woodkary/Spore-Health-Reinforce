package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class WrapperPacket {
    public static final int HEALTH_WRAPPER=0;
    public static final int DEATH_WRAPPER=1;
    private static final Map<Integer, Consumer<Entity>> operations=Map.of(
            HEALTH_WRAPPER, LivingEntityHealthLifecycleWrapperUtil.INSTANCE::createWrapppperLocal,
            DEATH_WRAPPER, LivingEntityHealthLifecycleWrapperUtil.INSTANCE::createDeathWrapppperLocal
            );
    private final int entityId;
    private final int option;

    public WrapperPacket(int entityId, int option) {
        if(option<0||option>1){
            throw new IllegalArgumentException("option must be between 0 and 1");
        }
        this.entityId = entityId;
        this.option = option;
    }
    public WrapperPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.option = buf.readInt();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(option);
    }
    public static void handle(WrapperPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(msg.entityId);
                if(entity!=null){
                    operations.get(msg.option).accept(entity);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
