package com.Harbinger.Spore.Core.customEntityData;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeInt2ObjectMap;
import com.Harbinger.Spore.Core.utils.unremovableCollections.SporeInt2ObjectMapProxy;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class UnmodifiableEntityData extends SynchedEntityData implements IUnmodifiableData {
    public static final Class<? extends SynchedEntityData> entityDataClass = (Class<? extends SynchedEntityData>) BytecodeUtil.resolveHiddenClassOrSelf(
            UnmodifiableEntityData.class,
            Entity.class,
            SynchedEntityData.class
    );
    private static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            entityDataClass,
            UnmodifiableEntityData.class,
            Entity.class,
            SynchedEntityData.class
    );
    public static SynchedEntityData newInstance(Entity p_135351_,SynchedEntityData oldData){
        constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                entityDataClass,
                UnmodifiableEntityData.class,
                Entity.class,
                SynchedEntityData.class
        );
        if(constructor!=null){
            try{
                return (SynchedEntityData) constructor.invoke(p_135351_,oldData);
            } catch (Throwable e) {
                LogUtil.errorf("failed to new UnmodifiableEntityData instance. %s", e.getMessage());
            }
        }
        return new UnmodifiableEntityData(p_135351_,oldData);
    }

    private final ISporeInt2ObjectMap<DataItem<?>> dataItemsById;
    private final Constructor<?> unmodifiableDataItemConstructor;
    public UnmodifiableEntityData(Entity p_135351_,SynchedEntityData oldData) {
        super(p_135351_);
        Class<?>[] paraTypes=new Class<?>[]{EntityDataAccessor.class,Object.class,Object.class};
        Class<?> unmodifiableDataItemClass= BytecodeUtil.resolveHiddenClassByName(
                "com.Harbinger.Spore.Core.customEntityData.UnmodifiableDataItem",
                paraTypes);
        Constructor<?> ctor=null;
        try{
            ctor=unmodifiableDataItemClass.getDeclaredConstructor(paraTypes);
        }catch(NoSuchMethodException e){
            LogUtil.error("failed to find unmodifiableDataItem constructor");
        }
        unmodifiableDataItemConstructor=ctor;
        this.dataItemsById = SporeInt2ObjectMapProxy.newInstance(new Int2ObjectOpenHashMap<>());
        for (Int2ObjectMap.Entry<DataItem<?>> entry
                : oldData.itemsById.int2ObjectEntrySet()) {
            copyDataItemEntry(entry);
        }
        this.isDirty=oldData.isDirty;
    }
    private <T> void copyDataItemEntry(
            Int2ObjectMap.Entry<DataItem<?>> entry) {
        @SuppressWarnings("unchecked")
        DataItem<T> source = (DataItem<T>) entry.getValue();

        int id = entry.getIntKey();
        this.dataItemsById.actualPut(id, copyDataItem(source));
        this.itemsById.put(id, copyDataItem(source));
    }
    private <T> DataItem<T> copyDataItem(DataItem<T> source) {
        EntityDataAccessor<T> accessor = source.getAccessor();
        EntityDataSerializer<T> serializer = accessor.getSerializer();
        T valueCopied = serializer.copy(source.getValue());
        T initialValueCopied = serializer.copy(source.initialValue);
        DataItem<T> copy=null;
        if(unmodifiableDataItemConstructor!=null){
            try{
                copy= (DataItem<T>) unmodifiableDataItemConstructor.newInstance(
                        accessor,
                        valueCopied,
                        initialValueCopied);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                LogUtil.errorf("failed to new UnmodifiableDataItem. %s", e.getMessage());
            }
        }
        if(copy==null){
            copy=new DataItem<>(
                    accessor,
                    valueCopied
            );
        }

        copy.initialValue = initialValueCopied;
        copy.setDirty(source.isDirty());
        return copy;
    }
    public <T> void define(EntityDataAccessor<T> p_135373_, T p_135374_) {
        int i = p_135373_.getId();
        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        } else if (this.dataItemsById.containsKey(i)) {
            throw new IllegalArgumentException("Duplicate id value for " + i + "!");
        } else if (EntityDataSerializers.getSerializedId(p_135373_.getSerializer()) < 0) {
            EntityDataSerializer<T> var10002 = p_135373_.getSerializer();
            throw new IllegalArgumentException("Unregistered serializer " + var10002 + " for " + i + "!");
        } else {
            this.createDataItem(p_135373_, p_135374_);
        }
    }

    private <T> void createDataItem(EntityDataAccessor<T> accessor, T value) {
        DataItem<T> dataitem=null;
        if(unmodifiableDataItemConstructor!=null){
            try{
                dataitem= (DataItem<T>) unmodifiableDataItemConstructor.newInstance(
                        accessor,
                        value,
                        value);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                LogUtil.errorf("failed to new UnmodifiableDataItem. %s", e.getMessage());
            }
        }
        if(dataitem==null){
            dataitem=new DataItem<>(
                    accessor,
                    value
            );
        }
        this.lock.writeLock().lock();
        this.dataItemsById.actualPut(accessor.getId(), dataitem);
        this.lock.writeLock().unlock();
    }
    public <T> boolean hasItem(EntityDataAccessor<T> p_286294_) {
        return this.dataItemsById.containsKey(p_286294_.getId());
    }

    public <T> DataItem<T> getItem(EntityDataAccessor<T> p_135380_) {
        this.lock.readLock().lock();

        DataItem<T> dataitem;
        try {
            dataitem = (DataItem<T>) this.dataItemsById.get(p_135380_.getId());
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting synched entity data");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Synched entity data");
            crashreportcategory.setDetail("Data ID", p_135380_);
            throw new ReportedException(crashreport);
        } finally {
            this.lock.readLock().unlock();
        }

        return dataitem;
    }

    public <T> T get(EntityDataAccessor<T> p_135371_) {
        return this.getItem(p_135371_).getValue();
    }

    public <T> void set(EntityDataAccessor<T> p_135382_, T p_135383_) {

    }

    public <T> void set(EntityDataAccessor<T> p_276368_, T p_276363_, boolean p_276370_) {

    }
    public boolean isDirty() {
        return false;
    }
    public @NotNull List<DataValue<?>> packDirty() {
        return List.of();
    }
    @Nullable
    public List<DataValue<?>> getNonDefaultValues() {
        List<DataValue<?>> list = null;
        this.lock.readLock().lock();

        for (DataItem<?> dataItem : this.dataItemsById.values()) {
            if (!dataItem.isSetToDefault()) {
                if (list == null) {
                    list = new ArrayList<>();
                }

                list.add(dataItem.value());
            }
        }

        this.lock.readLock().unlock();
        return list;
    }
    public void assignValues(List<DataValue<?>> p_135357_) {

    }
    public boolean isEmpty() {
        return this.dataItemsById.isEmpty();
    }
}
