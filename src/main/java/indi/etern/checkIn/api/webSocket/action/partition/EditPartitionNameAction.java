package indi.etern.checkIn.api.webSocket.action.partition;

import com.google.gson.JsonObject;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Optional;

public class EditPartitionNameAction extends PartitionJsonResultAction {
    private final Integer partitionId;
    private final String newName;
    
    public EditPartitionNameAction(Integer partitionId, String newName) {
        this.partitionId = partitionId;
        this.newName = newName;
    }
    
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
}
