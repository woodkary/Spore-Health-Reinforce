package com.Harbinger.Spore.Core.entities;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.inventory.SporeEmptyInventory;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;

public final class SporeDeadServerPlayer extends ServerPlayer {
    @SuppressWarnings("unchecked")
    public static final Class<? extends ServerPlayer> serverPlayerClass = (Class<? extends ServerPlayer>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeDeadServerPlayer.class,
            MinecraftServer.class,
            ServerLevel.class,
            GameProfile.class
    );

    public SporeDeadServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(server, level, profile);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Entity other) {
            return other.id == this.id && this.getClass() == other.getClass();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean canUpdate(){
        return false;
    }

    @Override
    public void canUpdate(boolean flag){
        super.canUpdate(false);
    }

    @Override
    public float getMaxHealth(){
        return 0.0f;
    }

    @Override
    public float getHealth() {
        return 0.0f;
    }

    @Override
    public boolean isDeadOrDying() {
        return true;
    }

    @Override
    public void respawn() {
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public void tick() {
    }

    @Override
    public void doTick() {
    }

    @Override
    public void aiStep() {
    }

    @Override
    protected void serverAiStep() {
    }

    @Override
    public void rideTick() {
    }

    @Override
    public void baseTick() {
    }

    @Override
    public Inventory getInventory() {
        return SporeEmptyInventory.newInstance(this);
    }
}
