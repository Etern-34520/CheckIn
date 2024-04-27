package indi.etern.checkIn.api.webSocket.action.partition;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.Collection;

public abstract class PartitionJsonResultAction extends TransactionalAction {
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
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", updatePartition);
        JsonObject partitionJsonObj = new JsonObject();
        partitionJsonObj.addProperty("id", partition.getId());
        partitionJsonObj.addProperty("name", partition.getName());
        jsonObject.add("partition", partitionJsonObj);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
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
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type);
        JsonObject partitionIdNameJsonObj = new JsonObject();
        for (Partition p : partition) {
            partitionIdNameJsonObj.addProperty(String.valueOf(p.getId()), p.getName());
        }
        jsonObject.add("partitions", partitionIdNameJsonObj);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
}