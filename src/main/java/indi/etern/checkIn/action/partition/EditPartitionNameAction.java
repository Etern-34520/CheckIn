package indi.etern.checkIn.action.partition;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Map;
import java.util.Optional;

@Action(name = "editPartition")
public class EditPartitionNameAction extends PartitionJsonResultAction {
    private Integer partitionId;
    private String newName;
    
    @Override
    public String requiredPermissionName() {
        return "edit partition name";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Partition partition = PartitionService.singletonInstance.findById(partitionId).orElseThrow();
        partition.setName(newName);
        PartitionService.singletonInstance.save(partition);
        
        sendUpdatePartitionToAll(partition);
        return Optional.empty();
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionId = ((Double) dataMap.get("id")).intValue();
        newName = (String) dataMap.get("name");
    }
}
