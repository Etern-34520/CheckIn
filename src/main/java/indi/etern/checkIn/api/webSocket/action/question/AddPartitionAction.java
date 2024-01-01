package indi.etern.checkIn.api.webSocket.action.question;

import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.PartitionJsonResultAction;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Optional;

public class AddPartitionAction extends PartitionJsonResultAction {
    private final String partitionName;
    public AddPartitionAction(String partitionName) {
        this.partitionName = partitionName;
    }
    
    @Override
    public String requiredPermissionName() {
        return "create partition";
    }
    
    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        final Partition partition = Partition.getInstance(partitionName);
        PartitionService.singletonInstance.save(partition);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "addPartitionCallBack");
        jsonObject.addProperty("id", partition.getId());
        return Optional.of(jsonObject);
    }
    
    @Override
    public void afterAction() {
        sendUpdatePartitionToAll();
    }
}
