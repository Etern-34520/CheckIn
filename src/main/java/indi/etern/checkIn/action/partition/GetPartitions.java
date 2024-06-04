package indi.etern.checkIn.action.partition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.utils.TransactionTemplateUtil;

import java.util.Optional;

@Action(name = "getPartitions")
public class GetPartitions extends TransactionalAction {
    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        JsonObject result = new JsonObject();
        TransactionTemplateUtil.getTransactionTemplate().execute((res) -> {
            JsonArray partitionList = new JsonArray();
            result.add("partitions", partitionList);
            PartitionService.singletonInstance.findAll().forEach(partition -> {
                JsonObject partitionInfo = new JsonObject();
                partitionInfo.addProperty("id", partition.getId());
                partitionInfo.addProperty("name", partition.getName());
                partitionInfo.addProperty("empty", partition.getQuestionLinks().isEmpty());
                partitionList.add(partitionInfo);
            });
            return null;
        });
        return Optional.of(result);
    }
}
