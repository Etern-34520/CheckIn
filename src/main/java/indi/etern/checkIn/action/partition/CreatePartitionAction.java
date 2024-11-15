package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("createPartition")
public class CreatePartitionAction extends PartitionMapResultAction {
    private String partitionName;
    private Partition createdPartition;

    @Override
    public String requiredPermissionName() {
        return "create partition";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        if (PartitionService.singletonInstance.existsByName(partitionName)) {
            LinkedHashMap<String,Object> error = new LinkedHashMap<>();
            error.put("type", "error");
            error.put("message", "partition already exists");
            return Optional.of(error);
        }
        final Partition partition = Partition.getInstance(partitionName);
        PartitionService.singletonInstance.save(partition);
        createdPartition = partition;
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type", "addPartitionCallBack");
        map.put("id", partition.getId());
        if (createdPartition != null) {
            sendAddPartitionToAll(createdPartition);
        }
        return Optional.of(map);
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionName = (String) dataMap.get("name");
        createdPartition = null;
    }
}
