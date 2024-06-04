package indi.etern.checkIn.action.partition;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Map;
import java.util.Optional;

@Action(name = "createPartition")
public class CreatePartitionAction extends PartitionJsonResultAction {
    private String partitionName;
    private Partition createdPartition;

    @Override
    public String requiredPermissionName() {
        return "create partition";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        if (PartitionService.singletonInstance.existsByName(partitionName)) {
            JsonObject error = new JsonObject();
            error.addProperty("type", "error");
            error.addProperty("message", "partition already exists");
            return Optional.of(error);
        }
        final Partition partition = Partition.getInstance(partitionName);
        PartitionService.singletonInstance.save(partition);
        createdPartition = partition;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "addPartitionCallBack");
        jsonObject.addProperty("id", partition.getId());
        if (createdPartition != null) {
            sendAddPartitionToAll(createdPartition);
        }
        return Optional.of(jsonObject);
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionName = (String) dataMap.get("name");
        createdPartition = null;
    }
}
