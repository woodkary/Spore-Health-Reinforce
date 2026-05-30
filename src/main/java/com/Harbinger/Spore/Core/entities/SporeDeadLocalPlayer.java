package com.Harbinger.Spore.Core.entities;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.inventory.SporeEmptyInventory;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;

public final class SporeDeadLocalPlayer extends LocalPlayer {
    @SuppressWarnings("unchecked")
    public static final Class<? extends LocalPlayer> localPlayerClass = (Class<? extends LocalPlayer>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeDeadLocalPlayer.class,
            Minecraft.class,
            ClientLevel.class,
            ClientPacketListener.class,
            StatsCounter.class,
            ClientRecipeBook.class,
            boolean.class,
            boolean.class
    );

    public SporeDeadLocalPlayer(Minecraft minecraft,
                                ClientLevel level,
                                ClientPacketListener connection,
                                StatsCounter stats,
                                ClientRecipeBook recipeBook,
                                boolean wasShiftKeyDown,
                                boolean wasSprinting) {
        super(minecraft, level, connection, stats, recipeBook, wasShiftKeyDown, wasSprinting);
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
    public boolean isAlive() {
        return false;
    }

    @Override
    public void respawn() {
    }

    @Override
    public void tick() {
    }

    @Override
    public void aiStep() {
    }

    @Override
    public void serverAiStep() {
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
