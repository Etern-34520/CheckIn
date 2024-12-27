package indi.etern.checkIn.aop;

import indi.etern.checkIn.entities.record.TrafficRecord;
import indi.etern.checkIn.service.dao.TrafficRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Component
@Aspect
public class ExamLoggerAspects {
    final TrafficRecordService trafficRecordService;
    
    public ExamLoggerAspects(TrafficRecordService trafficRecordService) {
        this.trafficRecordService = trafficRecordService;
    }
    
    @FunctionalInterface
    private interface RequestProcessor {
        void process(HttpServletRequest httpServletRequest,TrafficRecord trafficRecord);
    }
    
    private void record(TrafficRecord.Type type, RequestProcessor processor) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        TrafficRecord trafficRecord = TrafficRecord.from(httpServletRequest);
        trafficRecord.setType(type);
        if (processor != null) {
            processor.process(httpServletRequest,trafficRecord);
        }
        trafficRecordService.save(trafficRecord);
    }
    
    @Pointcut("execution(* indi.etern.checkIn.controller.html.MainController.exam())")
    public void visitExam() {}
    
    @Before("visitExam()")
    public void beforeVisit() {
        record(TrafficRecord.Type.VISIT,null);
    }
    
    @Pointcut("execution(* indi.etern.checkIn.controller.rest.Exam.generateExam())")
    public void generateExam() {}
    
    @Before("generateExam()")
    public void beforeGenerate() {
        record(TrafficRecord.Type.GENERATE,((httpServletRequest, trafficRecord) -> {
            long qq = Long.parseLong(httpServletRequest.getParameter("qq"));
            trafficRecord.setQQNumber(qq);
            String[] partitionIdsString = httpServletRequest.getParameterValues("partitionIds");
            List<Integer> partitionIds = Arrays.stream(partitionIdsString).map(Integer::parseInt).toList();
            trafficRecord.getExtraData().put("partitionIds",partitionIds);
        }));
    }
    
    @Pointcut("execution(* indi.etern.checkIn.controller.rest.Exam.loadLastExam())")
    public void loadLastExam() {}
    
    @Before("loadLastExam()")
    public void beforeLoadLast() {
        record(TrafficRecord.Type.LOAD_LAST,((httpServletRequest, trafficRecord) -> {
            long qq = Long.parseLong(httpServletRequest.getParameter("qq"));
            trafficRecord.setQQNumber(qq);
        }));
    }
    
    @Pointcut("execution(* indi.etern.checkIn.controller.rest.Exam.submit())")
    public void submit() {}
    
    @Before("submit()")
    public void beforeSubmit() {
        //TODO
    }
}