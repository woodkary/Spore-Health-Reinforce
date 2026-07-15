package com.Harbinger.Spore.Sitems.Guns;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public final class MistMakerEntityJudge implements Predicate<Entity> {
    private final Player player;

    public MistMakerEntityJudge(Player player) {
        this.player = player;
    }

    @Override
    public boolean test(Entity entity) {
        return entity instanceof LivingEntity &&
                entity != player &&
                !entity.isSpectator() &&
                entity.isAlive();
    }
}
