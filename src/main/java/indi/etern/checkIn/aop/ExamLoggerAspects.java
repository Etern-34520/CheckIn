package indi.etern.checkIn.aop;

import indi.etern.checkIn.controller.rest.Exam;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.record.TrafficRecord;
import indi.etern.checkIn.service.dao.TrafficRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
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
        void process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, TrafficRecord trafficRecord);
    }
    
    private void record(TrafficRecord.Type type, RequestProcessor processor) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        HttpServletResponse httpServletResponse = servletRequestAttributes.getResponse();
        TrafficRecord trafficRecord = TrafficRecord.from(httpServletRequest, httpServletResponse);
        trafficRecord.setType(type);
        if (processor != null) {
            processor.process(httpServletRequest, httpServletResponse, trafficRecord);
        }
        trafficRecordService.save(trafficRecord);
    }
    
    private void record(TrafficRecord.Type type,Throwable throwable,RequestProcessor processor) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        HttpServletResponse httpServletResponse = servletRequestAttributes.getResponse();
        TrafficRecord trafficRecord = TrafficRecord.from(httpServletRequest, httpServletResponse, throwable);
        trafficRecord.setType(type);
        if (processor != null) {
            processor.process(httpServletRequest, httpServletResponse, trafficRecord);
        }
        trafficRecordService.save(trafficRecord);
    }
    
    
    @Pointcut("execution(* indi.etern.checkIn.controller.html.MainController.exam())")
    public void visitExam() {
    }
    
    @Before("visitExam()")
    public void beforeVisit() {
        record(TrafficRecord.Type.VISIT, null);
    }
    
    @Pointcut("execution(* indi.etern.checkIn.controller.rest.Exam.generateExam(..))")
    public void generateExam() {
    }
    
    @AfterReturning("generateExam()")
    public void afterGenerate(JoinPoint joinPoint) {
        record(TrafficRecord.Type.GENERATE, getGenerateProcessor(joinPoint));
    }
    
    @AfterThrowing(pointcut = "generateExam()",throwing = "throwable")
    public void afterGenerate(JoinPoint joinPoint, Throwable throwable) {
        record(TrafficRecord.Type.GENERATE, throwable, getGenerateProcessor(joinPoint));
    }
    
    private static RequestProcessor getGenerateProcessor(JoinPoint joinPoint) {
        return (httpServletRequest, httpServletResponse, trafficRecord) -> {
            Exam.GenerateRequest generateRequest = (Exam.GenerateRequest) joinPoint.getArgs()[0];
            long qq = generateRequest.qq();
            trafficRecord.setQQNumber(qq);
            trafficRecord.getExtraData().put("partitionIds", generateRequest.partitionIds());
        };
    }
    
    @Pointcut("execution(* indi.etern.checkIn.service.dao.ExamDataService.getExamDataQuestions(..))")
    public void getQuestionsByExamIdAndIndexes() {
    }
    
    @AfterReturning("getQuestionsByExamIdAndIndexes()")
    public void afterGetQuestionsByExamIdAndIndexes(JoinPoint joinPoint) {
        record(TrafficRecord.Type.GET_EXAM_QUESTION, getGetQuestionsProcessor(joinPoint));
    }
    
    @AfterThrowing(pointcut = "getQuestionsByExamIdAndIndexes()",throwing = "throwable")
    public void afterExceptionGetQuestionsByExamIdAndIndexes(JoinPoint joinPoint, Throwable throwable) {
        record(TrafficRecord.Type.GET_EXAM_QUESTION, throwable, getGetQuestionsProcessor(joinPoint));
    }
    
    private static RequestProcessor getGetQuestionsProcessor(JoinPoint joinPoint) {
        return (httpServletRequest, httpServletResponse, trafficRecord) -> {
            ExamData examData = (ExamData) joinPoint.getArgs()[1];
            trafficRecord.setQQNumber(examData.getQqNumber());
            trafficRecord.getExtraData().put("indexes", joinPoint.getArgs()[0]);
        };
    }
    
    @Pointcut("execution(* indi.etern.checkIn.service.dao.ExamDataService.handleSubmit(..))")
    public void submit() {
    }
    
    @AfterReturning("submit()")
    public void afterSubmit(JoinPoint joinPoint) {
        record(TrafficRecord.Type.SUBMIT, getSubmitProcessor(joinPoint));
    }
    
    @AfterThrowing(pointcut = "submit()",throwing = "throwable")
    public void afterSubmitThrowing(JoinPoint joinPoint, Throwable throwable) {
        record(TrafficRecord.Type.SUBMIT, throwable, getSubmitProcessor(joinPoint));
    }
    
    private static RequestProcessor getSubmitProcessor(JoinPoint joinPoint) {
        return (httpServletRequest, httpServletResponse, trafficRecord) -> {
            ExamData examData = (ExamData) joinPoint.getArgs()[0];
            //noinspection unchecked
            Map<String, Object> answerData = (Map<String, Object>) joinPoint.getArgs()[1];
            trafficRecord.setQQNumber(examData.getQqNumber());
            trafficRecord.getExtraData().put("answerData", answerData);
        };
    }
}