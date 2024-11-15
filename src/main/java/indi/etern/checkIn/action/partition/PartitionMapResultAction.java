package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Collection;
import java.util.LinkedHashMap;

public abstract class PartitionMapResultAction extends TransactionalAction {
    protected void sendUpdatePartitionToAll(Partition partition) {
        sendPartitionActionToAll("updatePartition", partition);
    }

    protected void sendDeletePartitionToAll(Partition partition) {
        sendPartitionActionToAll("deletePartition", partition);
    }

    protected void sendAddPartitionToAll(Partition partition) {
        sendPartitionActionToAll("addPartition", partition);
    }
    private static void sendPartitionActionToAll(String updatePartition, Partition partition) {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type", updatePartition);
        LinkedHashMap<String,Object> partitionMap = new LinkedHashMap<>();
        partitionMap.put("id", partition.getId());
        partitionMap.put("name", partition.getName());
        map.put("partition", partitionMap);
        WebSocketService.singletonInstance.sendMessageToAll(map);
    }
    protected void sendUpdatePartitionsToAll(Collection<Partition> partition) {
        sendPartitionsActionToAll(partition, "updatePartitions");
    }
    protected void sendDeletePartitionsToAll(Collection<Partition> partition) {
        sendPartitionsActionToAll(partition, "deletePartitions");
    }
    protected void sendAddPartitionsToAll(Collection<Partition> partition) {
        sendPartitionsActionToAll(partition, "addPartitions");
    }

    private void sendPartitionsActionToAll(Collection<Partition> partition, String type) {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type", type);
        LinkedHashMap<String,Object> partitionIdNameMap = new LinkedHashMap<>();
        for (Partition p : partition) {
            partitionIdNameMap.put(String.valueOf(p.getId()), p.getName());
        }
        map.put("partitions", partitionIdNameMap);
        WebSocketService.singletonInstance.sendMessageToAll(map);
    }
}