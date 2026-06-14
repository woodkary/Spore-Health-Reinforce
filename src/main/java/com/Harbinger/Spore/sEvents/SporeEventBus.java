package com.Harbinger.Spore.sEvents;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.BusBuilderImpl;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
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
        Map<Object, List<IEventListener>> listeners= (Map<Object, List<IEventListener>>) ClassUtil.getFieldValueFromHierarchy(MinecraftForge.EVENT_BUS, "listeners");
        for(Field f : EventBus.class.getDeclaredFields()) {
            ClassUtil.setFieldValue(f,
                    evb,
                    ClassUtil.getFieldValue(f,MinecraftForge.EVENT_BUS));
        }
        return evb;
    }
    private static Optional<?> resetEventBusClass(){
        if(eventBusClass!=null&&INSTANCE.getClass()!=eventBusClass){
            KlassPointerUtil.INSTANCE.replaceClass(INSTANCE, eventBusClass,"",0,0.0f);
        }
        return Optional.empty();
    }
    public static void tick(){
        resetEventBusClass();
        INSTANCE.tickMinecraftForgeEventBus();
    }

    public SporeEventBus(BusBuilderImpl busBuilder) {
        super(busBuilder);
    }
    @Override
    public void tickMinecraftForgeEventBus() {
        if(MinecraftForge.EVENT_BUS.getClass()!=eventBusClass){
            MinecraftForge.EVENT_BUS=this;
        }
    }

    @Override
    public boolean post(Event event) {
        return this.post(event,this);
    }

    @Override
    public boolean post(Event event, IEventBusInvokeDispatcher wrapper) {
        return super.post(event, wrapper);
    }

    @Override
    public void invoke(IEventListener listener, Event event) {
        try {
            listener.invoke(event);
        }catch (Throwable ignored){}
    }
}
