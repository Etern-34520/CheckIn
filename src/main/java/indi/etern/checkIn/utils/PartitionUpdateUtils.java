package indi.etern.checkIn.utils;

import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.web.WebSocketService;

public class PartitionUpdateUtils {
    public static void sendUpdatePartitionToAll(Partition partition) {
        sendPartitionActionToAll("updatePartition", partition);
    }
    
    public static void sendDeletePartitionToAll(Partition partition) {
        sendPartitionActionToAll("deletePartition", partition);
    }
    
    public static void sendAddPartitionToAll(Partition partition) {
        sendPartitionActionToAll("addPartition", partition);
    }
    
    private static void sendPartitionActionToAll(String type, Partition partition) {
        Message<Partition> message = Message.of(type,partition);
        WebSocketService.singletonInstance.sendMessageToAll(message);
    }
    
}
