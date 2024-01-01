package indi.etern.checkIn.api.webSocket.action;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.web.WebSocketService;

import java.util.List;

public abstract class PartitionJsonResultAction extends JsonResultAction {
    protected void sendUpdatePartitionToAll() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "updatePartitionList");
        final List<Partition> partitions = PartitionService.singletonInstance.findAll();
        JsonObject partitionIdNameJsonObj = new JsonObject();
        for (Partition partition : partitions) {
            partitionIdNameJsonObj.addProperty(String.valueOf(partition.getId()), partition.getName());
        }
        jsonObject.add("partitionIdNameMap", partitionIdNameJsonObj);
        WebSocketService.singletonInstance.sendMessageToAll(jsonObject);
    }
}