package com.Harbinger.Spore.Core.utils.simpleRemoval;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ISimpleRemoval {
    void tickServer();

    void tickClient();

    Vec3 getNaNPosition();

    boolean checkIsRemovedAndUpdate(Object entity);

    <T extends EntityAccess> Collection<T> getAllEntities(Level level, Predicate<T> filter);

    boolean checkIsRemovedAndUpdate(Entity entity);

    boolean checkIsRemovedAndUpdate(Integer id);

    boolean checkIsRemovedAndUpdate(UUID uuid);

    boolean isRemoved(Object key);
    boolean isRemoved(Entity entity);
    boolean isRemoved(Integer id);
    boolean isRemoved(UUID uuid);


    boolean remove(Entity entity, Entity.RemovalReason removalReason);
    Entity removeLocal(Entity entity, Entity.RemovalReason removalReason);
}
