package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Action("getPartitionById")
public class GetPartitionByIdAction extends BaseAction1<GetPartitionByIdAction.Input, OutputData> {
    public record Input(@Nonnull String id) implements InputData {}
    public record SuccessOutput(Map<String,Object> partitionDataMap) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final PartitionService partitionService;
    
    public GetPartitionByIdAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<Input, OutputData> context) {
        final Input input = context.getInput();
        Optional<Partition> optionalPartition = partitionService.findById(input.id);
        if (optionalPartition.isPresent()) {
            Partition partition = optionalPartition.get();
            context.resolve(new SuccessOutput(partition.toInfoMap()));
        } else {
            context.resolve(MessageOutput.error("Partition not found"));
        }
    }
}
