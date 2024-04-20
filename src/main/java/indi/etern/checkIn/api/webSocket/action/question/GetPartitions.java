package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.JsonResultAction;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Optional;

public class GetPartitions extends JsonResultAction {
    @Override
    public String requiredPermissionName() {
        return "";
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        JsonObject result = new JsonObject();
        JsonArray partitionList = new JsonArray();
        result.add("partitions", partitionList);
        PartitionService.singletonInstance.findAll().forEach(partition -> {
            JsonObject partitionInfo = new JsonObject();
            partitionInfo.addProperty("id", partition.getId());
            partitionInfo.addProperty("name", partition.getName());
            partitionInfo.addProperty("empty", partition.getQuestions().isEmpty());
            partitionList.add(partitionInfo);
        });
        return Optional.of(result);
    }
}
