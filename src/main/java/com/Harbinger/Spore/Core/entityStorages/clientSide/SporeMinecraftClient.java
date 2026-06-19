package com.Harbinger.Spore.Core.entityStorages.clientSide;

import com.Harbinger.Spore.Core.entityStorages.GameTickerUtil;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;

public final class SporeMinecraftClient extends Minecraft {
    public static final Class<? extends Minecraft> MINECRAFT_CLASS = (Class<? extends Minecraft>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeMinecraftClient.class,
            GameConfig.class
    );
    public SporeMinecraftClient(GameConfig p_91084_) {
        super(p_91084_);
    }
    public void tick() {
        GameTickerUtil.INSTANCE.tickClient(this);
        super.tick();
    }
}
