package com.Harbinger.Spore.sEvents;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.entityStorages.SporeEntitySectionStorage;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.effects.SporeEffectsUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.BusBuilderImpl;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher;
import net.minecraftforge.eventbus.api.IEventListener;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SporeEventBus extends EventBus implements ISporeEventBus,IEventBusInvokeDispatcher {
    private static final ISporeEventBus INSTANCE=init();
    private static Class<? extends ISporeEventBus> eventBusClass;
    private static ISporeEventBus init(){
        Class<? extends ISporeEventBus>[] clazz=new Class[1];
        ISporeEventBus evb= BytecodeUtil.createHiddenSingletonInstance(
                clazz,
                ISporeEventBus.class,
                SporeEventBus.class,
                new Class<?>[]{BusBuilderImpl.class},
                new BusBuilderImpl()
        );
        if(clazz[0]!=null){
            eventBusClass=clazz[0];
        }
        for(Field f : EventBus.class.getDeclaredFields()) {
            ClassUtil.setFieldValue(f,
                    evb,
                    ClassUtil.getFieldValue(f,MinecraftForge.EVENT_BUS));
        }
        return evb;
    }
    private static ISporeEventBus resetEventBusClass(){
        if(eventBusClass!=null&&INSTANCE.getClass()!=eventBusClass){
            KlassPointerUtil.INSTANCE.replaceClass(INSTANCE, eventBusClass,"",0,0.0f);
        }
        return INSTANCE;
    }
    public static ISporeEventBus tick(){
        return resetEventBusClass().tickMinecraftForgeEventBus();
    }
    private final Field shutdownField;
    public SporeEventBus(BusBuilderImpl busBuilder) {
        super(busBuilder);
        Field shutdown=null;
        try{
            shutdown=EventBus.class.getDeclaredField("shutdown");
        } catch (NoSuchFieldException e) {
            LogUtil.errorf("failed to find shutdown field,%s,default null",e.getMessage());
        }
        shutdownField=shutdown;
    }
    @Override
    public ISporeEventBus tickMinecraftForgeEventBus() {
        if(MinecraftForge.EVENT_BUS.getClass()!=eventBusClass){
            MinecraftForge.EVENT_BUS=this;
        }
        return this;
    }

    @Override
    public void addSelfListener() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,this);
    }

    @Override
    public boolean post(Event event) {
        return this.post(event,this);
    }

    @Override
    public boolean post(Event event, IEventBusInvokeDispatcher wrapper) {
        if(shutdownField!=null){
            ClassUtil.setFieldValue(shutdownField,this,false);
        }
        if(event instanceof EntityEvent entityEvent&&!(entityEvent instanceof ItemTooltipEvent)&&SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entityEvent.getEntity())){
            return true;
        }
        if(event instanceof MobEffectEvent.Applicable applicable){
            MobEffectInstance effectInstance = applicable.getEffectInstance();
            if(effectInstance.getEffect()== Seffects.HEALING_INHIBITION.get()) {
                //禁止原版的addEffect，执行自己的
                applicable.setResult(Event.Result.DENY);
                SporeEffectsUtil.INSTANCE.forceAddEffect(applicable.getEntity(), effectInstance, null);
                return true;
            }
        }
        if(event instanceof MobEffectEvent.Remove removeEffect&&removeEffect.getEffect()==Seffects.HEALING_INHIBITION.get()) {
            //禁止移除禁疗效果
            return true;
        }
        return super.post(event, wrapper);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void invoke(IEventListener listener, Event event) {
        try {
            listener.invoke(event);
        }catch (Throwable ignored){}
    }

    @Override
    public void accept(TickEvent tickEvent) {
        if(tickEvent instanceof TickEvent.ClientTickEvent||tickEvent instanceof TickEvent.ServerTickEvent){
            if(tickEvent instanceof TickEvent.ServerTickEvent serverTickEvent){
                SimpleRemoveUtil.INSTANCE.tickServer();
                for (ServerLevel level : serverTickEvent.getServer().getAllLevels()) {
                    PersistentEntitySectionManager<? extends EntityAccess> manager=level.entityManager;
                    if(manager.sectionStorage.getClass()!=SporeEntitySectionStorage.entitySectionStorageClass) {
                        KlassPointerUtil.INSTANCE.replaceClass(manager.sectionStorage, SporeEntitySectionStorage.entitySectionStorageClass, "", 0, 0.0f);
                    }
                }
            }else{
                ClientLevel level=Minecraft.getInstance().level;
                if(level!=null){
                    TransientEntitySectionManager<? extends EntityAccess> manager=level.entityStorage;
                    if(manager.sectionStorage.getClass()!=SporeEntitySectionStorage.entitySectionStorageClass) {
                        KlassPointerUtil.INSTANCE.replaceClass(manager.sectionStorage, SporeEntitySectionStorage.entitySectionStorageClass,"",0,0.0f);
                    }
                }
                SimpleRemoveUtil.INSTANCE.tickClient();
            }
            SporeEntityHeeaafastthManager.INSTANCE.tick();
            EntityHeealuthManager.INSTANCE.tick();
        }
    }
}
