package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.utils.PartitionUpdateUtils;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

@Action("createPartition")
public class CreatePartitionByNameAction extends BaseAction<CreatePartitionByNameAction.Input, OutputData> {
    public record Input(@Nonnull String name) implements InputData {}
    public record SuccessOutput(Result result, String id) implements OutputData {}
    
    private final PartitionService partitionService;
    
    public CreatePartitionByNameAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        context.requirePermission("create partition");
        final Input input = context.getInput();
        
        if (partitionService.existsByName(input.name)) {
            context.resolve(new MessageOutput(OutputData.Result.ERROR, "partition already exists"));
            return;
        }
        final Partition partition = Partition.ofName(input.name);
        partitionService.save(partition);
        PartitionUpdateUtils.sendAddPartitionToAll(partition);
        context.resolve(new SuccessOutput(OutputData.Result.SUCCESS, partition.getId()));
    }
}
