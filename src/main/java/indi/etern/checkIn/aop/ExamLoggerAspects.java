package indi.etern.checkIn.aop;

import indi.etern.checkIn.controller.rest.Exam;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.record.TrafficRecord;
import indi.etern.checkIn.service.dao.TrafficRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

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
    
    @Pointcut("execution(* indi.etern.checkIn.controller.rest.Exam.generateExam(..))")
    public void generateExam() {}
    
    @Before("generateExam()")
    public void beforeGenerate(JoinPoint joinPoint) {
        record(TrafficRecord.Type.GENERATE,(httpServletRequest, trafficRecord) -> {
            Exam.GenerateRequest generateRequest = (Exam.GenerateRequest) joinPoint.getArgs()[0];
            long qq = generateRequest.qq();
            trafficRecord.setQQNumber(qq);
            trafficRecord.getExtraData().put("partitionIds",generateRequest.partitionIds());
        });
    }
    
    /*@Pointcut("execution(* indi.etern.checkIn.controller.rest.Exam.loadLastExam())")
    public void loadLastExam() {}
    
    @Before("loadLastExam()")
    public void beforeLoadLast() {
        record(TrafficRecord.Type.LOAD_LAST,((httpServletRequest, trafficRecord) -> {
            long qq = Long.parseLong(httpServletRequest.getParameter("qq"));
            trafficRecord.setQQNumber(qq);
        }));
    }*/
    
    @Pointcut("execution(* indi.etern.checkIn.service.dao.ExamDataService.handleSubmit(..))")
    public void submit() {}
    
    @Before("submit()")
    public void beforeSubmit(JoinPoint joinPoint) {
        record(TrafficRecord.Type.SUBMIT, (httpServletRequest, trafficRecord) -> {
            ExamData examData = (ExamData) joinPoint.getArgs()[0];
            //noinspection unchecked
            Map<String,Object> answerData = (Map<String, Object>) joinPoint.getArgs()[1];
            trafficRecord.setQQNumber(examData.getQqNumber());
            trafficRecord.getExtraData().put("answerData",answerData);
        });
    }
}