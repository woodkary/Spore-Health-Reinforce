package com.Harbinger.Spore.ExtremelySusThings;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Protector;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.Spore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.lang.ref.WeakReference;

public class SporeSavedData extends SavedData {
    public static final String NAME = Spore.MODID +"_world_data";
    private static final List<WeakReference<Protector>> protectorList = new ArrayList<>();
    private static final List<WeakReference<Proto>> protos = new ArrayList<>();
    private final Map<String, ChunkLoadRequest> activeRequests = new HashMap<>();
    private transient ServerLevel ownerLevel;
    private boolean casingLightAllowed;

    public static synchronized void addProtector(Protector protector){
        pruneProtectors();
        for (WeakReference<Protector> reference : protectorList) {
            if (reference.get() == protector) {
                return;
            }
        }
        protectorList.add(new WeakReference<>(protector));
    }

    public static synchronized void removeProtector(Protector protector){
        protectorList.removeIf(reference -> {
            Protector value = reference.get();
            return value == null || value == protector;
        });
    }

    public static synchronized List<Protector> protectorList(){
        List<Protector> result = new ArrayList<>();
        protectorList.removeIf(reference -> {
            Protector value = reference.get();
            if (value == null || value.isRemoved()) {
                return true;
            }
            result.add(value);
            return false;
        });
        return result;
    }

    public static synchronized List<Protector> protectorList(ServerLevel level){
        List<Protector> result = new ArrayList<>();
        protectorList.removeIf(reference -> {
            Protector value = reference.get();
            if (value == null || value.isRemoved()) {
                return true;
            }
            if (value.level() == level) {
                result.add(value);
            }
            return false;
        });
        return result;
    }

    public static synchronized void addProto(Proto proto){
        pruneProtos();
        for (WeakReference<Proto> reference : protos) {
            if (reference.get() == proto) {
                return;
            }
        }
        protos.add(new WeakReference<>(proto));
    }

    public static synchronized void removeProto(Proto proto){
        protos.removeIf(reference -> {
            Proto value = reference.get();
            return value == null || value == proto;
        });
    }

    public static synchronized List<Proto> getHiveminds(){
        List<Proto> result = new ArrayList<>();
        protos.removeIf(reference -> {
            Proto value = reference.get();
            if (value == null || value.isRemoved()) {
                return true;
            }
            result.add(value);
            return false;
        });
        return result;
    }

    public static synchronized List<Proto> getHiveminds(ServerLevel level){
        List<Proto> result = new ArrayList<>();
        protos.removeIf(reference -> {
            Proto value = reference.get();
            if (value == null || value.isRemoved()) {
                return true;
            }
            if (value.level() == level) {
                result.add(value);
            }
            return false;
        });
        return result;
    }

    private static void pruneProtectors() {
        protectorList.removeIf(reference -> {
            Protector value = reference.get();
            return value == null || value.isRemoved();
        });
    }

    private static void pruneProtos() {
        protos.removeIf(reference -> {
            Proto value = reference.get();
            return value == null || value.isRemoved();
        });
    }

    public static synchronized void clearRuntimeEntityReferences() {
        protectorList.clear();
        protos.clear();
    }

    public SporeSavedData() {
        super();
    }

    public int getAmountOfHiveminds(){
        return ownerLevel == null ? getHiveminds().size() : getHiveminds(ownerLevel).size();
    }


    public static SporeSavedData getDataLocation(ServerLevel level){
        SporeSavedData data = level.getDataStorage().get(SporeSavedData::load,NAME);
        if (data != null) {
            data.ownerLevel = level;
        }
        return data;
    }

    public static SporeSavedData get(ServerLevel level) {
        SporeSavedData data = level.getDataStorage().computeIfAbsent(
                SporeSavedData::load,
                SporeSavedData::new,
                NAME
        );
        data.ownerLevel = level;
        return data;
    }

    public void addRequest(ChunkLoadRequest request) {
        activeRequests.put(request.getRequestID(), request);
        setDirty();
    }

    public void removeRequest(String id) {
        activeRequests.remove(id);
        setDirty();
    }

    public Collection<ChunkLoadRequest> getRequests() {
        return List.copyOf(activeRequests.values());
    }

    public boolean isCasingLightAllowed() {
        return casingLightAllowed;
    }

    public void setCasingLightAllowed(boolean casingLightAllowed) {
        this.casingLightAllowed = casingLightAllowed;
        setDirty();
    }

    public static SporeSavedData load(CompoundTag tag){
        SporeSavedData data = new SporeSavedData();
        data.casingLightAllowed = tag.getBoolean("CasingLightAllowed");
        if (tag.contains("ChunkRequests", 9)) { // 9 = ListTag
            var list = tag.getList("ChunkRequests", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                ChunkLoadRequest request = ChunkLoadRequest.deserializeNBT(entry);
                data.activeRequests.put(request.getRequestID(), request);
            }
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        var listTag = new net.minecraft.nbt.ListTag();
        for (ChunkLoadRequest request : activeRequests.values()) {
            listTag.add(request.serializeNBT());
        }
        tag.put("ChunkRequests", listTag);
        tag.putBoolean("CasingLightAllowed", casingLightAllowed);
        return tag;
    }

}
