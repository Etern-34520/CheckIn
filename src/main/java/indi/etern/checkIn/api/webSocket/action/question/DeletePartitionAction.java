package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.PartitionJsonResultAction;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Optional;

public class DeletePartitionAction extends PartitionJsonResultAction {
    
    private final int partitionId;
    
    public DeletePartitionAction(int partitionId) {
        this.partitionId = partitionId;
    }
    
    @Override
    public String requiredPermissionName() {
        return "delete partition";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        Partition partition = PartitionService.singletonInstance.findById(partitionId).orElseThrow();
        if (partition.getQuestions().isEmpty()) {
            PartitionService.singletonInstance.delete(partition);
        } else {
            return getOptionalErrorJsonObject("partition \"" + partition.getName() + "\" is not empty");
        }
        sendUpdatePartitionToAll();
        return Optional.empty();
    }
}
