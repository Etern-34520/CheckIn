package indi.etern.checkIn.service.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.api.webSocket.Connector;
import indi.etern.checkIn.api.webSocket.Message;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    public static WebSocketService singletonInstance;
    private final ObjectMapper objectMapper;
    private final HashMap<String, Channel> channelHashMap = new HashMap<>();
    
    @Getter
    public static class Channel {
        private final String name;
        private final HashSet<String> sids = new HashSet<>();
        
        public Channel(String name) {
            this.name = name;
        }
        
        public void sendMessage(Message<?> message) {
            singletonInstance.sendMessages(message, sids);
        }
        
        public void close() {
            for (String sid : sids) {
                singletonInstance.unsubscribeChannel(sid, name);
            }
            sids.clear();
            singletonInstance.channelHashMap.remove(name);
        }
    }
    
    protected WebSocketService(ObjectMapper objectMapper) {
        singletonInstance = this;
        this.objectMapper = objectMapper;
    }
    
    @SneakyThrows
    private void sendMessages(Message<?> message, HashSet<String> toSids) {
        String messageStr = objectMapper.writeValueAsString(message);
        logger.debug("webSocket to:{}, msg:{}", toSids, messageStr);
        for (Connector item : Connector.CONNECTORS) {
            try {
                if (toSids.isEmpty()) {
                    item.sendMessageWithoutLog(messageStr);
                } else if (toSids.contains(item.getSid())) {
                    item.sendMessageWithoutLog(messageStr);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    @SneakyThrows
    public void sendMessage(Message<?> message, String sid) {
        String messageStr = objectMapper.writeValueAsString(message);
        logger.debug("webSocket to:{}, msg:{}", sid, messageStr);
        for (Connector item : Connector.CONNECTORS) {
            try {
                if (item.getSid().equals(sid)) {
                    item.sendMessage(messageStr);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    public void sendMessageToAll(String message) {
        logger.debug("webSocket to all, msg:{}", message);
        sendMessageToAllWithoutLog(message);
    }
    
    @SneakyThrows
    public void sendMessageToAll(LinkedHashMap<String, Object> map) {
        sendMessageToAll(objectMapper.writeValueAsString(map));
    }
    
    @SneakyThrows
    public void sendMessageToAll(Message<?> message) {
        sendMessageToAll(objectMapper.writeValueAsString(message));
    }
    
    public void sendMessageToAllWithoutLog(String message) {
        for (Connector connector : Connector.CONNECTORS) {
            if (connector.isOpen()) {
                try {
                    connector.sendMessageWithoutLog(message);
                } catch (IllegalStateException ignored) {
                } catch (Exception e) {
                    logger.error("error occurred when send to sid_{}", connector.getSid(), e);
                }
            }
        }
    }
    
    public void subscribeChannel(String sid, String channelName) {
        Channel channel = channelHashMap.get(channelName);
        if (channel == null) {
            channel = new Channel(channelName);
            channelHashMap.put(channelName, channel);
        }
        channel.sids.add(sid);
    }
    
    public void unsubscribeChannel(String sid, String channelName) {
        if (!channelHashMap.isEmpty()) {
            Channel channel = channelHashMap.get(channelName);
            if (channel != null) {
                channel.sids.remove(sid);
                if (channel.sids.isEmpty()) {
                    channel.close();
                }
            }
        }
    }
    
    public void unsubscribeAllChannels(String sid) {
        if (!channelHashMap.isEmpty()) {
            final HashSet<Channel> channels = new HashSet<>(channelHashMap.values());
            for (Channel channel : channels) {
                channel.sids.remove(sid);
                if (channel.sids.isEmpty()) {
                    channel.close();
                }
            }
        }
    }
    
    public void sendMessageToChannel(Message<?> message, String channelName) {
        logger.debug("webSocket to channel:{}}", channelName);
        Channel channel = channelHashMap.get(channelName);
        if (channel != null) {
            message.setChannelName(channelName);
            channel.sendMessage(message);
        }
    }
    
    @SuppressWarnings("unused")
    public Channel getChannel(String channelName) {
        return channelHashMap.get(channelName);
    }
}
