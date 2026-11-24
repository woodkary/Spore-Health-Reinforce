package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassReflectionUtil;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeIterator;
import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeMap;
import com.Harbinger.Spore.Core.utils.unremovableCollections.SporeMapProxy;
import com.Harbinger.Spore.Sentities.AI.TargetSameModGoal;
import com.Harbinger.Spore.sEvents.HandlerEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;

import java.util.Map;
import java.util.WeakHashMap;

public final class MobGetTargetHook implements IGetTargetHook {
    public static final IGetTargetHook INSTANCE;
    private final ISporeMap<Mob,Object> selfTargetMobs= SporeMapProxy.newInstance(new WeakHashMap<>());
    private int tickCount = 100;
    @Override
    public LivingEntity getTargetCallSiteHook(Mob mob, LivingEntity original){
        if(SporeJudge.isSporeEntity(mob)||!isSelfTarrget(mob)){
            return original;
        }
        TargetSameModGoal goal = HandlerEvents.findGoal(mob, TargetSameModGoal.class);
        if(goal==null){
            return original;
        }
        LivingEntity newTarget=goal.getSameModMobTarget();
        return newTarget!=null&&EntityHeealuthManager.INSTANCE.rawIsAlliive(newTarget)
                ?newTarget:
                original;
    }
    @Override
    public boolean isSelfTarrget(Mob mob){
        return selfTargetMobs.containsKey(mob);
    }
    @Override
    public void putSelfTarrget(Mob mob){
        if(mob.level.isClientSide){
            return;
        }
        selfTargetMobs.actualPut(mob,EntityHeealuthManager.NULL_OBJECT);
    }
    @Override
    public void serverTick(TickEvent.ServerTickEvent event) {
        tickCount--;
        if (tickCount <= 0) {
            tickCount=100;
            ISporeIterator<Map.Entry<Mob,Object>> iterator= (ISporeIterator<Map.Entry<Mob, Object>>) selfTargetMobs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Mob, Object> entry = iterator.next();
                Mob mob = entry.getKey();
                if(mob==null||mob.isRemoved()&&mob.level.getEntity(mob.id)==null){
                    iterator.actualRemove();
                }
            }
        }
    }
    @Override
    public void accept(TickEvent.ServerTickEvent serverTickEvent) {
        serverTick(serverTickEvent);
    }

    static{
        Class<? extends IGetTargetHook>[] clazz=new Class[1];
        INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
                clazz,
                IGetTargetHook.class,
                MobGetTargetHook.class,
                new Class<?>[0]
        );
        if(clazz[0]!=null){
            ClassReflectionUtil.removeCachedReflectionData(clazz[0]);
        }
        ClassReflectionUtil.removeCachedReflectionData(MobGetTargetHook.class);
    }
}
