/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.net;

import cn.tdchain.jbcc.rpc.MessageHandler;
import cn.tdchain.jbcc.rpc.nio.handler.NioHandshakerType;
import cn.tdchain.jbcc.rpc.nio.handler.NioHeartServerHandler;
import cn.tdchain.jbcc.rpc.nio.handler.NioServerHandler;
import cn.tdchain.tdmsp.util.Sha1Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.*;
import java.util.concurrent.*;

public class ConnectionCount {

    private final static ConcurrentHashMap<String, OUKey> clientKeyMap = new ConcurrentHashMap<>();
    private static ConnectionCount connectionCount;
    private static MessageHandler handler;
    private static ThreadPoolExecutor threadpool;

    private static void init() {
        threadpool = new ThreadPoolExecutor(10, 200,
                10000l, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService scheduledheart = Executors.newSingleThreadScheduledExecutor();
        scheduledheart.scheduleAtFixedRate(() -> {
            clientKeyMap.forEach((k, v) -> {
                try {
                    Channel heart = v.getHeart();
                    if (heart == null) {
                        long l = System.currentTimeMillis() - v.getStartTime();
                        if (l > 30000) {
                            close(k, v);
                        }
                    } else if (v.getHeartServerHandler().isTimeOut()) {
                        close(k, v);
                    }
                } catch (Exception e) {

                }
            });
        }, 1000, 3000, TimeUnit.MILLISECONDS);
        scheduled.scheduleAtFixedRate(() ->
                        clientKeyMap.forEach((k, v) -> {
                            threadpool.submit(() -> {
                                try {
                                    Channel channel = v.getChannel();
                                    if (channel != null) {
                                        String result = handler.getResultMapByConnectionId(k, v.getClientKey());
                                        if (result != null) {
                                            channel.writeAndFlush(result);
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            });
                        })
                , 0, 30, TimeUnit.MILLISECONDS);
    }

    private ConnectionCount() {
        init();
    }

    public synchronized static ConnectionCount newInstance() {
        if (connectionCount == null) {
            connectionCount = new ConnectionCount();
        }
        return connectionCount;
    }

    public synchronized void handleConnection(String conId, String ckey, String orgName, Channel channel) {
        if (clientKeyMap.containsKey(conId)) {
            OUKey ouKey = clientKeyMap.get(conId);
            ouKey.getChannels().add(channel);
            return;
        }
        OUKey ouKey = new OUKey();
        ouKey.clientKey = ckey;
        ouKey.ou = orgName;
        ouKey.account = Sha1Util.sha1(ckey);
        ouKey.startTime = System.currentTimeMillis();
        ouKey.getChannels().add(channel);
        clientKeyMap.put(conId, ouKey);
    }

    public synchronized boolean checkSingle(String connid, String clientKey) {
        // 如果有connid
        OUKey ouKey = clientKeyMap.get(connid);
        // 如果key一致,则true,否则为false
        if (ouKey != null) {
            if (ouKey.getClientKey().equals(clientKey)) {
                return true;
            }
            return false;
        }
        // 如果没有connid,此时key尚且未被销毁,证明有另外一个client正在使用,则等待心跳销毁后方可使用
        boolean contains = clientKeyMap.values().stream().anyMatch(ou -> ou.getClientKey().equals(clientKey));
        if (contains) {
            return false;
        }
        return true;
    }

    public static void close(String connId, OUKey ouKey) {
        try {
            ouKey.getChannels().forEach(ch -> {
                ch.close();
            });
            clientKeyMap.remove(connId);
        } catch (Exception e) {
        }
    }

    public String getOUbyPublickey(String ckey) {
        Set<Map.Entry<String, OUKey>> entries = clientKeyMap.entrySet();
        Iterator<Map.Entry<String, OUKey>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, OUKey> next = iterator.next();
            OUKey value = next.getValue();
            if (value.clientKey.equals(ckey)) {
                return value.getOu();
            }
        }
        return null;
    }

    public OUKey getOUKey(String connId) {
        if (connId == null) {
            return null;
        }
        return clientKeyMap.get(connId);
    }

    public void handleSocket(String connId, Channel channel) {
        if (clientKeyMap.containsKey(connId)) {
            OUKey ouKey = clientKeyMap.get(connId);
            ouKey.setChannel(channel);
        } else {
            channel.close();
        }
    }

    public boolean containsKey(String connId) {
        return clientKeyMap.containsKey(connId);
    }

    public void remove(String connid) {
        clientKeyMap.remove(connid);
    }

    public ConcurrentHashMap<String, OUKey> getClientKeyMap() {
        return clientKeyMap;
    }

    public void handleHeart(Channel channel) {
        Attribute<String> attr = channel.attr(AttributeKey.valueOf(NioHandshakerType.CONNECTIONID.name()));
        String connectionId = attr.get();
        OUKey ouKey = clientKeyMap.get(connectionId);
        if (ouKey == null) {
            channel.close();
        } else {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.remove(LengthFieldBasedFrameDecoder.class);
            pipeline.remove(LengthFieldPrepender.class);
            pipeline.remove(NioServerHandler.class);

            NioHeartServerHandler nioHeartServerHandler = new NioHeartServerHandler();
            pipeline.addLast(nioHeartServerHandler);
            ouKey.setHeart(channel);
            ouKey.setHeartServerHandler(nioHeartServerHandler);
        }
    }


    public static class OUKey {
        private String clientKey;
        private String ou;
        private String account;
        private Channel channel;
        private Channel heart;
        private long startTime;
        private NioHeartServerHandler heartServerHandler;
        private List<Channel> channels = new ArrayList<>();

        public List<Channel> getChannels() {
            return channels;
        }

        public NioHeartServerHandler getHeartServerHandler() {
            return heartServerHandler;
        }

        public void setHeartServerHandler(NioHeartServerHandler heartServerHandler) {
            this.heartServerHandler = heartServerHandler;
        }

        public void setChannels(List<Channel> channels) {
            this.channels = channels;
        }

        public Channel getHeart() {
            return heart;
        }

        public void setHeart(Channel heart) {
            this.heart = heart;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getClientKey() {
            return clientKey;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }

        public String getOu() {
            return ou;
        }

        public void setOu(String ou) {
            this.ou = ou;
        }

    }

    public ConnectionCount buildHandler(MessageHandler handler) {
        if (this.handler == null) {
            this.handler = handler;
        }
        return this;
    }
}
