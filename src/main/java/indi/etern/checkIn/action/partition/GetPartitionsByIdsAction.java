package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Action("getPartitionsByIds")
public class GetPartitionsByIdsAction extends BaseAction<GetPartitionsByIdsAction.Input, GetPartitionsByIdsAction.Output> {
    public record Input(@Nonnull List<String> ids) implements InputData {}
    public record Output(List<Partition> partitions) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final PartitionService partitionService;

    public GetPartitionsByIdsAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, Output> context) {
        final Input input = context.getInput();
        List<Partition> partitions = partitionService.findAllByIds(input.ids);
        context.resolve(new Output(partitions));
    }
}
