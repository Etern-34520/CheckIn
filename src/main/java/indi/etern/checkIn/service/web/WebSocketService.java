package indi.etern.checkIn.service.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.api.webSocket.Connector;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.user.User;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

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
    
    public void sendMessages(String message, HashSet<String> toSids) {
        logger.info("webSocket to:{}, msg:{}", toSids, message);
        for (Connector item : Connector.CONNECTORS) {
            try {
                if (toSids.isEmpty()) {
                    item.sendMessageWithOutLog(message);
                } else if (toSids.contains(item.getSid())) {
                    item.sendMessageWithOutLog(message);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    @SneakyThrows
    private void sendMessages(Map<?, ?> mapMessage, HashSet<String> toSids) {
        String message = objectMapper.writeValueAsString(mapMessage);
        logger.info("webSocket to:{}, msg:{}", toSids, message);
        for (Connector item : Connector.CONNECTORS) {
            try {
                if (toSids.isEmpty()) {
                    item.sendMessageWithOutLog(message);
                } else if (toSids.contains(item.getSid())) {
                    item.sendMessageWithOutLog(message);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    @SneakyThrows
    private void sendMessages(Message<?> message, HashSet<String> toSids) {
        String messageStr = objectMapper.writeValueAsString(message);
        logger.info("webSocket to:{}, msg:{}", toSids, messageStr);
        for (Connector item : Connector.CONNECTORS) {
            try {
                if (toSids.isEmpty()) {
                    item.sendMessageWithOutLog(messageStr);
                } else if (toSids.contains(item.getSid())) {
                    item.sendMessageWithOutLog(messageStr);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    public void sendMessage(String message, String sid) {
        logger.info("webSocket to:{}, msg:{}", sid, message);
        for (Connector item : Connector.CONNECTORS) {
            try {
                if (item.getSid().equals(sid)) {
                    item.sendMessage(message);
                }
            } catch (IOException ignored) {
            }
        }
    }
    
    @SneakyThrows
    public void sendMessage(Message<?> message, String sid) {
        String messageStr = objectMapper.writeValueAsString(message);
        logger.info("webSocket to:{}, msg:{}", sid, messageStr);
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
        logger.info("webSocket to all, msg:{}", message);
        sendMessageToAllWithoutLog(message);
    }
    
    public boolean isOnline(String sid) {
        boolean online = false;
        for (Connector item : Connector.CONNECTORS) {
            if (item.getSid().equals(sid)) {
                online = true;
                break;
            }
        }
        return online;
    }
    
    public boolean isOnline(User user) {
        return isOnline(String.valueOf(user.getQQNumber()));
    }
    
    @SneakyThrows
    public void sendMessageToAll(LinkedHashMap<String, Object> map) {
        sendMessageToAll(objectMapper.writeValueAsString(map));
    }
    
    @SneakyThrows
    public void sendMessageToAllWithoutLog(LinkedHashMap<String, Object> map) {
        sendMessageToAllWithoutLog(objectMapper.writeValueAsString(map));
    }
    
    public void sendMessageToAllWithoutLog(String message) {
        for (Connector item : Connector.CONNECTORS) {
            if (item.isOpen()) {
                try {
                    item.sendMessageWithOutLog(message);
                } catch (IllegalStateException ignored) {
                } catch (Exception e) {
                    logger.error("error occurred when send to sid_{}", item.getSid(), e);
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
                    channelHashMap.remove(channelName);
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
                    channelHashMap.remove(channel.name);
                }
            }
        }
    }
    
    /*public void sendMessageToChannel(String message, String channelName) {
        Channel channel = channelHashMap.get(channelName);
        if (channel != null) {
            sendMessages(message, channel.sids);
        }
    }*/
    
    public void sendMessageToChannel(Map<Object, Object> message, String channelName) {
        logger.info("webSocket to channel:{}}", channelName);
        Channel channel = channelHashMap.get(channelName);
        if (channel != null) {
            message.put("channelName", channelName);
            sendMessages(message, channel.sids);
            message.remove("channelName");
        }
    }
    
    /*
        public void sendMessageToChannel(String message, Channel channel) {
            sendMessages(message, channel.sids);
        }
    */
    public void sendMessageToChannel(Map<?, ?> message, Channel channel) {
        logger.info("webSocket to channel:{}", channel.getName());
        sendMessages(message, channel.sids);
    }
    
    public Channel getChannel(String channelName) {
        return channelHashMap.get(channelName);
    }
}
