package indi.etern.checkIn.action.partition;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Map;
import java.util.Optional;

@Action(name = "getPartitionById")
public class GetPartitionByIdAction extends PartitionJsonResultAction {
    private final PartitionService partitionService;
    private Integer partitionId;

    public GetPartitionByIdAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }

    @Override
    public String requiredPermissionName() {
        return "";
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Optional<Partition> optionalPartition = partitionService.findById(partitionId);
        if (optionalPartition.isPresent()) {
            Partition partition = optionalPartition.get();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", partition.getId());
            jsonObject.addProperty("name", partition.getName());
            return Optional.of(jsonObject);
        } else {
            return getOptionalErrorJsonObject("Partition not found");
        }
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionId = ((Double) dataMap.get("id")).intValue();
    }
}
