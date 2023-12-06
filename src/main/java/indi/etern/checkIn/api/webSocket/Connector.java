package indi.etern.checkIn.api.webSocket;

import com.google.gson.Gson;
import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ServerEndpoint("/api/websocket/{sid}")
public class Connector {
    public static final HashSet<String> ALL = new HashSet<>(0);
    public static final CopyOnWriteArraySet<Connector> CONNECTORS = new CopyOnWriteArraySet<>();
    private static final Logger logger = LoggerFactory.getLogger(CheckInApplication.class);
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private static PartitionService partitionService;
    private static WebSocketService webSocketService;
    private static MultiPartitionableQuestionService multiPartitionableQuestionService;
    private static TransactionTemplate transactionTemplate;
    private Gson gson = new Gson();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //接收sid
    @Getter
    private String sid = "";
    
    public Connector() {
//        this.partitionService = CheckInApplication.applicationContext.getBean(PartitionService.class);
    }
    
    /**
     * 获取当前在线人数
     */
    public static int getOnlineCount() {
        return onlineCount.get();
    }
    
    /**
     * 当前在线人数 +1
     */
    public static void addOnlineCount() {
        onlineCount.getAndIncrement();
    }
    
    /**
     * 当前在线人数 -1
     */
    public static void subOnlineCount() {
        onlineCount.getAndDecrement();
    }
    
    @Autowired
    public void setPartitionService(PartitionService partitionService) {
        Connector.partitionService = partitionService;
    }
    
    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        Connector.webSocketService = webSocketService;
    }
    
    @Autowired
    public void setMultiPartitionableQuestionService(MultiPartitionableQuestionService multiPartitionableQuestionService) {
        Connector.multiPartitionableQuestionService = multiPartitionableQuestionService;
    }
    
    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        Connector.transactionTemplate = transactionTemplate;
    }
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        CONNECTORS.add(this);     // 加入set中
        this.sid = sid;
        addOnlineCount();           // 在线数加1
        //            sendMessage("conn_success");
        logger.info("sid_" + sid + ":connected");
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        CONNECTORS.remove(this);  // 从set中删除
        subOnlineCount();              // 在线数减1
        // 断开连接情况下，更新主板占用情况为释放
        logger.info("sid_" + sid + ":close");
        releaseResource();
    }
    
    private void releaseResource() {
        // 这里写释放资源和要处理的业务
//        logger.info("close:"+sid);
    }
    
    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            logger.info("sid_" + sid + ":" + message);
            Map<String, Object> contentMap = new Gson().fromJson(message, HashMap.class);
            final String partitionName = (String) contentMap.get("partitionName");
            switch ((String) contentMap.get("type")) {
                case "addPartition" -> {
                    final Partition partition = Partition.getInstance(partitionName);
                    partitionService.save(partition);
                    sendMessage("{\"type\":\"addPartitionCallBack\",\"id\":"+partition.getId()+"}");
                    sendUpdatePartitionToAll();
                }
                case "deletePartition" -> transactionTemplate.execute((TransactionCallback<Object>) result -> {
                    try {
                        Partition partition = partitionService.findByName(partitionName);
                        if (partition.getQuestions().isEmpty()) {
                            partitionService.delete(partition);
                            sendUpdatePartitionToAll();
                        } else {
                            Map<String, String> dataMap = new HashMap<>();
                            dataMap.put("type", "error");
                            dataMap.put("data", "partition " + partitionName + " is not empty");
                            sendMessage(gson.toJson(dataMap));
                        }
                    } catch (Exception e) {
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                });
                case "deleteQuestion" -> {
                    final String questionMD5 = (String) contentMap.get("questionMD5");
                    transactionTemplate.execute((TransactionCallback<Object>) result -> {
                        MultiPartitionableQuestion multiPartitionableQuestion = multiPartitionableQuestionService.getByMD5(questionMD5);
                        Set<Partition> partitions = multiPartitionableQuestion.getPartitions();
                        for (Partition partition : partitions) {
                            partition.getQuestions().remove(multiPartitionableQuestion);
                            partitionService.saveAndFlush(partition);
                        }
                        return Boolean.TRUE;
                    });
                    multiPartitionableQuestionService.deleteById(questionMD5);
                    sendDeleteQuestionToAll(questionMD5);
                }
                case "editPartition" -> {
                    final String newName = (String) contentMap.get("partitionName");
                    Partition partition = partitionService.findById(Integer.valueOf(contentMap.get("partitionId").toString())).orElseThrow();
                    partition.setName(newName);
                    partitionService.saveAndFlush(partition);
                    sendUpdatePartitionToAll();
                }
            }
        } catch (Exception e) {
            try {
                logger.error(e.getClass().getName() + ":" + e.getMessage());
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("type", "error");
                dataMap.put("data", e.getClass().getSimpleName() + ":" + e.getMessage());
                sendMessage(gson.toJson(dataMap));
//                sendMessage("error:" + e.getMessage());
            } catch (IOException exception) {
                logger.error("while sending message:" + message + "to sid_" + sid + ":" + exception.getMessage());
            }
        }
        /*// 群发消息
        HashSet<String> sids = new HashSet<>();
        for (Connector item : CONNECTORS) {
            sids.add(item.sid);
        }*/
    }
    
    private void sendDeleteQuestionToAll(String questionMD5) throws IOException {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "deleteQuestion");
        dataMap.put("questionMD5", questionMD5);
        webSocketService.sendMessageToAll(gson.toJson(dataMap));
    }
    
    private void sendUpdatePartitionToAll() throws IOException {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "updatePartitionList");
        final List<Partition> partitions = partitionService.findAll();
        final Map<String,String> partitionIdNameMap = new HashMap<>();
//        final List<String> partitionNames = new ArrayList<>();
//        final List<Integer> partitionIds = new ArrayList<>();
        for (Partition partition : partitions) {
            partitionIdNameMap.put(String.valueOf(partition.getId()),partition.getName());
        }
//        dataMap.put("partitions", partitionNames);
//        dataMap.put("partitionIds", partitionIds);
        dataMap.put("partitionIdNameMap", partitionIdNameMap);
        webSocketService.sendMessageToAll(gson.toJson(dataMap));
    }
    
    /**
     * 发生错误回调
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error(session.getBasicRemote() + " error");
        error.printStackTrace();
    }
    
    /**
     * 实现服务器主动推送消息到 指定客户端
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    
}