package indi.etern.checkIn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.Context;
import indi.etern.checkIn.action.JsonContext;
import indi.etern.checkIn.action.interfaces.ResultContext;
import indi.etern.checkIn.action.interfaces.ResultJsonContext;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = CheckInApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContextActionTest {
    @Autowired
    ActionExecutor actionExecutor;
    @Test
    public void test1() {
        final ResultContext<TestAction.Output> testEcho = actionExecutor.execute(TestAction.class, new TestAction.Input("test echo", null));
        System.out.println(testEcho.getOutput().echo());
    }
    @Test
    public void test2() {
        try {
            final ResultContext<TestAction.Output> execute = actionExecutor.execute(TestAction.class, new TestAction.Input("test echo", "test"));
            System.out.println(execute.getOutput().echo());
            throw new RuntimeException();
        } catch (PermissionDeniedException e) {
            System.out.println("success denied");
        }
    }
    @Test
    public void test3() {
        final ResultJsonContext<?> testEcho = (ResultJsonContext<?>) actionExecutor.execute(TestAction.class, JsonContext.class, new TestAction.Input("test echo", null));
        System.out.println(testEcho.getOptionalJsonResult());
    }
    @Test
    public void test4() throws JsonProcessingException {
        try {
            final ResultContext<TestAction.Output> testEcho = actionExecutor.executeWithJson(TestAction.class, Context.class, "{\"echo\":\"test\"}");
            System.out.println(testEcho.getOutput().echo());
        } catch (JsonProcessingException e) {
            throw e;
        }
    }
    @Test
    public void test5() throws JsonProcessingException {
        final ResultJsonContext<?> testEcho = actionExecutor.executeWithJson(TestAction.class, "{\"echo\":\"test\"}");
        System.out.println(testEcho.getOptionalJsonResult());
    }
    @Test
    public void test6() throws JsonProcessingException {
        try {
            final ResultJsonContext<?> testEcho = actionExecutor.executeWithJson(TestAction.class, "{}");
            System.out.println(testEcho.getOptionalJsonResult());
            throw new RuntimeException();
        } catch (ValueInstantiationException e) {
            System.out.println("exception expected:");
            e.printStackTrace();
        }
    }
}
