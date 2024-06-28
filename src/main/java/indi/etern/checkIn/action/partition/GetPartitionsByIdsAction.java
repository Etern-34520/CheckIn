package indi.etern.checkIn.action.partition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Action(name = "getPartitionsByIds")
public class GetPartitionsByIdsAction extends PartitionJsonResultAction {
    private final PartitionService partitionService;
    private List<Integer> partitionIds;

    public GetPartitionsByIdsAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }

    @Override
    public String requiredPermissionName() {
        return "";
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        List<Partition> partitions = partitionService.findAllById(partitionIds);
        JsonObject jsonObject = new JsonObject();
        JsonArray partitionsJson = new JsonArray();
        for (Partition partition : partitions) {
            JsonObject partitionJson = new JsonObject();
            partitionJson.addProperty("id", partition.getId());
            partitionJson.addProperty("name", partition.getName());
            partitionsJson.add(partitionJson);
        }
        jsonObject.add("partitions", partitionsJson);
        return Optional.of(jsonObject);
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        //noinspection unchecked
        partitionIds = ((List<Double>) dataMap.get("ids")).stream().map(Double::intValue).collect(Collectors.toList());
    }
}
