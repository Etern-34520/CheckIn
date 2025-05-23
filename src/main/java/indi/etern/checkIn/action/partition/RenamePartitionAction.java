package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.BasicOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.utils.PartitionUpdateUtils;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

@Action("renamePartition")
public class RenamePartitionAction extends BaseAction<RenamePartitionAction.Input,BasicOutput> {
    public record Input(@Nonnull String partitionId, @Nonnull String newName) implements InputData {}
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, BasicOutput> context) {
        context.requirePermission("edit partition name");
        final Input input = context.getInput();
        Partition partition = PartitionService.singletonInstance.findById(input.partitionId).orElseThrow();
        partition.setName(input.newName);
        PartitionService.singletonInstance.save(partition);
        
        PartitionUpdateUtils.sendUpdatePartitionToAll(partition);
        context.resolve(BasicOutput.SUCCESS);
    }
}
