package indi.etern.checkIn.aop;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.service.dao.RequestRecordService;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Aspect
public class ExamLoggerAspects {
    final RequestRecordService requestRecordService;
    private final WebSocketService webSocketService;
    
    public ExamLoggerAspects(RequestRecordService requestRecordService, WebSocketService webSocketService) {
        this.requestRecordService = requestRecordService;
        this.webSocketService = webSocketService;
    }
    
    @FunctionalInterface
    private interface RequestProcessor {
        void process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, RequestRecord requestRecord);
    }
    
    private RequestRecord record(RequestRecord.Type type, RequestProcessor processor) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        HttpServletResponse httpServletResponse = servletRequestAttributes.getResponse();
        RequestRecord requestRecord = RequestRecord.from(httpServletRequest, httpServletResponse);
        requestRecord.setType(type);
        if (processor != null) {
            processor.process(httpServletRequest, httpServletResponse, requestRecord);
        }
        sendUpdateRequestRecord(requestRecord);
        requestRecordService.save(requestRecord);
        return requestRecord;
    }
    
    private RequestRecord record(RequestRecord.Type type, Throwable throwable, RequestProcessor processor) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        HttpServletResponse httpServletResponse = servletRequestAttributes.getResponse();
        RequestRecord requestRecord = RequestRecord.from(httpServletRequest, httpServletResponse, throwable);
        requestRecord.setType(type);
        if (processor != null) {
            processor.process(httpServletRequest, httpServletResponse, requestRecord);
        }
        sendUpdateRequestRecord(requestRecord);
        requestRecordService.save(requestRecord);
        return requestRecord;
    }
    
    private void sendUpdateRequestRecord(RequestRecord requestRecord) {
        Map<Object, Object> updateData = new HashMap<>();
        updateData.put("type", "updateRequestRecord");
        updateData.put("requestRecord", requestRecord);
        webSocketService.sendMessageToChannel(updateData, "requestRecords");
    }
    
    
    @Pointcut("execution(* indi.etern.checkIn.controller.html.MainController.exam(..))")
    public void visitExam() {
    }
    
    @Before("visitExam()")
    public void beforeVisit() {
        record(RequestRecord.Type.VISIT, null);
    }
    
    @Pointcut("execution(* indi.etern.checkIn.service.exam.ExamGenerator.generateExam(..))")
    public void generateExam() {
    }
    
    @AfterReturning(pointcut = "generateExam()", returning = "examData")
    public void afterGenerate(JoinPoint joinPoint,ExamData examData) {
        record(RequestRecord.Type.GENERATE, getGenerateProcessor(joinPoint,examData));
    }
    
    @AfterThrowing(pointcut = "generateExam()", throwing = "throwable")
    public void afterGenerate(JoinPoint joinPoint, Throwable throwable) {
        record(RequestRecord.Type.GENERATE, throwable, getGenerateProcessor(joinPoint,null));
    }
    
    private RequestProcessor getGenerateProcessor(JoinPoint joinPoint,ExamData examData) {
        return (httpServletRequest, httpServletResponse, requestRecord) -> {
            long qq = (long) joinPoint.getArgs()[0];
            if (examData != null) {
                requestRecord.setRelatedExamDataId(examData.getId());
//                requestRecord.getExtraData().put("examData", examData.toDataMap());
                List<RequestRecord> requestRecords = requestRecordService.findAllBySessionId(requestRecord.getSessionId());
                requestRecords.forEach(requestRecord1 -> requestRecord1.setRelatedExamDataId(examData.getId()));
                requestRecordService.saveAll(requestRecords);
            }
            requestRecord.setQQNumber(qq);
        };
    }
    
    @Pointcut("execution(* indi.etern.checkIn.service.dao.ExamDataService.getExamDataQuestions(..))")
    public void getQuestionsByExamIdAndIndexes() {
    }
    
    @AfterReturning("getQuestionsByExamIdAndIndexes()")
    public void afterGetQuestionsByExamIdAndIndexes(JoinPoint joinPoint) {
        record(RequestRecord.Type.GET_EXAM_QUESTIONS, getGetQuestionsProcessor(joinPoint));
    }
    
    @AfterThrowing(pointcut = "getQuestionsByExamIdAndIndexes()", throwing = "throwable")
    public void afterExceptionGetQuestionsByExamIdAndIndexes(JoinPoint joinPoint, Throwable throwable) {
        record(RequestRecord.Type.GET_EXAM_QUESTIONS, throwable, getGetQuestionsProcessor(joinPoint));
    }
    
    private static RequestProcessor getGetQuestionsProcessor(JoinPoint joinPoint) {
        return (httpServletRequest, httpServletResponse, trafficRecord) -> {
            ExamData examData = (ExamData) joinPoint.getArgs()[1];
            trafficRecord.setRelatedExamDataId(examData.getId());
            trafficRecord.setQQNumber(examData.getQqNumber());
            trafficRecord.getExtraData().put("indexes", joinPoint.getArgs()[0]);
//            trafficRecord.getExtraData().put("examData", examData.toDataMap());
        };
    }
    
    @Pointcut("execution(* indi.etern.checkIn.service.dao.ExamDataService.handleSubmit(..))")
    public void submit() {
    }
    
    @AfterReturning("submit()")
    public void afterSubmit(JoinPoint joinPoint) {
        record(RequestRecord.Type.SUBMIT, getSubmitProcessor(joinPoint));
    }
    
    @AfterThrowing(pointcut = "submit()", throwing = "throwable")
    public void afterSubmitThrowing(JoinPoint joinPoint, Throwable throwable) {
        record(RequestRecord.Type.SUBMIT, throwable, getSubmitProcessor(joinPoint));
    }
    
    private static RequestProcessor getSubmitProcessor(JoinPoint joinPoint) {
        return (httpServletRequest, httpServletResponse, trafficRecord) -> {
            ExamData examData = (ExamData) joinPoint.getArgs()[0];
            trafficRecord.setRelatedExamDataId(examData.getId());
            //noinspection unchecked
            Map<String, Object> answerData = (Map<String, Object>) joinPoint.getArgs()[1];
            trafficRecord.setQQNumber(examData.getQqNumber());
//            trafficRecord.getExtraData().put("answerData", answerData);
//            trafficRecord.getExtraData().put("examData", examData.toDataMap());
        };
    }
}