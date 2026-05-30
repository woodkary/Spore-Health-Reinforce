package com.Harbinger.Spore.network;

import java.util.concurrent.atomic.AtomicInteger;

public class ChannelIdHandler {
    private static final AtomicInteger channelIds = new AtomicInteger(0);
    public static int getChannelId() {
        return channelIds.incrementAndGet();
    }
}
