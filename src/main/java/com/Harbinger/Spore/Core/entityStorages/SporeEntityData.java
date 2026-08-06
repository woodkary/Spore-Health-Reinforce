package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
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
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

public final class SporeEntityData extends SynchedEntityData implements ICustomEntityData {
    private static final Class<? extends SynchedEntityData> entityDataClass= (Class<? extends SynchedEntityData>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityData.class,
            Entity.class,
            SynchedEntityData.class
    );
    private static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            entityDataClass,
            SporeEntityData.class,
            Entity.class,
            SynchedEntityData.class
    );
    public static SynchedEntityData newInstance(Entity entity,SynchedEntityData oldData){
        constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                entityDataClass,
                SporeEntityData.class,
                Entity.class,
                SynchedEntityData.class
        );
        if(constructor!=null){
            try{
                return (SynchedEntityData) constructor.invoke(entity,oldData);
            } catch (Throwable e) {
                LogUtil.errorf("failed to invoke constructor of EntityData. %s",e.getMessage());
            }
        }
        return new SporeEntityData(entity,oldData);
    }
    private final Int2ObjectMap<DataItem<?>> dataItemsById;
    public SporeEntityData(Entity entity,SynchedEntityData oldData) {
        super(entity);
        this.dataItemsById = new Int2ObjectOpenHashMap<>(oldData.itemsById);
        this.itemsById.putAll(oldData.itemsById);
        this.isDirty=oldData.isDirty;
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

    private <T> void createDataItem(EntityDataAccessor<T> p_135386_, T p_135387_) {
        DataItem<T> dataitem = new DataItem<>(p_135386_, p_135387_);
        this.lock.writeLock().lock();
        this.dataItemsById.put(p_135386_.getId(), dataitem);
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
        this.set(p_135382_, p_135383_, false);
    }

    public <T> void set(EntityDataAccessor<T> p_276368_, T p_276363_, boolean p_276370_) {
        DataItem<T> dataitem = this.getItem(p_276368_);
        if (p_276370_ || ObjectUtils.notEqual(p_276363_, dataitem.getValue())) {
            dataitem.setValue(p_276363_);
            this.entity.onSyncedDataUpdated(p_276368_);
            dataitem.setDirty(true);
            this.isDirty = true;
        }

    }

    public boolean isDirty() {
        return this.isDirty;
    }

    @Nullable
    public List<DataValue<?>> packDirty() {
        List<DataValue<?>> list = null;
        if (this.isDirty) {
            this.lock.readLock().lock();

            for (DataItem<?> dataItem : this.dataItemsById.values()) {
                if (dataItem.isDirty()) {
                    dataItem.setDirty(false);
                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    list.add(dataItem.value());
                }
            }

            this.lock.readLock().unlock();
        }

        this.isDirty = false;
        return list;
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
        this.lock.writeLock().lock();

        try {
            for(DataValue<?> datavalue : p_135357_) {
                DataItem<?> dataitem = this.dataItemsById.get(datavalue.id());
                if (dataitem != null) {
                    this.assignValue(dataitem, datavalue);
                    this.entity.onSyncedDataUpdated(dataitem.getAccessor());
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }

        this.entity.onSyncedDataUpdated(p_135357_);
    }

    public boolean isEmpty() {
        return this.dataItemsById.isEmpty();
    }

    @Override
    public Int2ObjectMap<DataItem<?>> itemsById() {
        return this.dataItemsById;
    }
}
