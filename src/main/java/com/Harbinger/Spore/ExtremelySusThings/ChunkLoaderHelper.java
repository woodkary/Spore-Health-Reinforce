package com.Harbinger.Spore.ExtremelySusThings;

import com.Harbinger.Spore.Core.SticketType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class ChunkLoaderHelper {
    public static final Map<String, ChunkLoadRequest> ACTIVE_REQUESTS =  new HashMap<>();

    public static boolean containsRequest(String requestId) {
        return requestId != null && ACTIVE_REQUESTS.containsKey(requestId);
    }

    public static void addRequest(ChunkLoadRequest request) {
        if (request == null || request.getRequestID() == null) {
            return;
        }
        ChunkLoadRequest previous = ACTIVE_REQUESTS.get(request.getRequestID());
        if (previous != null) {
            return;
        }
        ServerLevel level = request.getDimension();
        if (level == null) {
            return;
        }
        ACTIVE_REQUESTS.put(request.getRequestID(), request);
        SporeSavedData data = SporeSavedData.get(level);
        for (ChunkPos pos : request.getChunkPositionsToLoad()) {
            ChunkLoaderHelper.forceChunk(level, pos);
        }
        data.addRequest(request);
    }

    public static void removeRequest(String requestId) {
        ChunkLoadRequest request = ACTIVE_REQUESTS.remove(requestId);
        if (request != null) {
            ServerLevel level = request.getDimension();
            if (level != null) {
                for (ChunkPos pos : request.getChunkPositionsToLoad()) {
                    ChunkLoaderHelper.unforceChunk(level, pos);
                }
                SporeSavedData data = SporeSavedData.getDataLocation(level);
                if (data != null) {
                    data.removeRequest(requestId);
                }
            }
        }
    }

    public static void removeRequestsByPrefix(String prefix) {
        removeRequestsByPrefixExcept(prefix, null);
    }

    public static void removeRequestsByPrefixExcept(String prefix, String requestIdToKeep) {
        if (prefix == null) {
            return;
        }
        List<String> requestIds = new ArrayList<>();
        for (String requestId : ACTIVE_REQUESTS.keySet()) {
            if (requestId.startsWith(prefix) && !Objects.equals(requestId, requestIdToKeep)) {
                requestIds.add(requestId);
            }
        }
        for (String requestId : requestIds) {
            removeRequest(requestId);
        }
    }

    public static void clear() {
        List<String> requestIds = new ArrayList<>(ACTIVE_REQUESTS.keySet());
        for (String requestId : requestIds) {
            removeRequest(requestId);
        }
        ACTIVE_REQUESTS.clear();
    }

    public static void clearRuntimeRequests() {
        ACTIVE_REQUESTS.clear();
    }

    public static void tick() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, ChunkLoadRequest> entry : ACTIVE_REQUESTS.entrySet()) {
            ChunkLoadRequest request = entry.getValue();

            request.decrementTicksUntilExpiration(1);
            if (request.isExpired() && !request.refreshIfOwnerStillPresent(request.getTickAmount())) {
                toRemove.add(request.getRequestID());
            }
        }
        for (String id : toRemove) {
            removeRequest(id);
        }
    }
    public static void forceChunk(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().addRegionTicket(
                SticketType.SPORE_CHUNK_LOADER,
                pos,
                2,
                pos
        );
    }

    public static void unforceChunk(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().removeRegionTicket(
                SticketType.SPORE_CHUNK_LOADER,
                pos,
                2,
                pos
        );
    }
}
