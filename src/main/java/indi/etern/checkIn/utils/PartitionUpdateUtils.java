package indi.etern.checkIn.utils;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Collection;
import java.util.LinkedHashMap;

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
    
    private static void sendPartitionActionToAll(String updatePartition, Partition partition) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("type", updatePartition);
        map.put("partition", partition.toInfoMap());
        WebSocketService.singletonInstance.sendMessageToAll(map);
    }
    
    public static void sendUpdatePartitionsToAll(Collection<Partition> partition) {
        sendPartitionsActionToAll(partition, "updatePartitions");
    }
    
    public static void sendDeletePartitionsToAll(Collection<Partition> partition) {
        sendPartitionsActionToAll(partition, "deletePartitions");
    }
    
    public static void sendAddPartitionsToAll(Collection<Partition> partition) {
        sendPartitionsActionToAll(partition, "addPartitions");
    }
    
    private static void sendPartitionsActionToAll(Collection<Partition> partition, String type) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("type", type);
        LinkedHashMap<String, Object> partitionIdNameMap = new LinkedHashMap<>();
        for (Partition p : partition) {
            partitionIdNameMap.put(String.valueOf(p.getId()), p.toInfoMap());
        }
        map.put("partitions", partitionIdNameMap);
        WebSocketService.singletonInstance.sendMessageToAll(map);
    }
}
