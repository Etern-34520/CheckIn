package indi.etern.checkIn.action;

import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.ResultContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.throwable.auth.InvalidOperatorException;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("universalActionContext")
@Scope("prototype")
public class Context<I extends InputData, O extends OutputData> implements ResultContext<O>,ExecuteContext<I,O> {
    @Getter
    final User currentUser;
    {
        //FIXME
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object object = authentication.getPrincipal();
            if (object instanceof User user) {
                currentUser = user;
            } else {
                currentUser = User.ANONYMOUS;
            }
        } else {
            currentUser = User.ANONYMOUS;
        }
    }
    final JwtTokenProvider jwtTokenProvider;
    
    @Setter
    protected I input;
    protected O output;
    
    public Context(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public void requirePermission(String permissionName) {
        if (!jwtTokenProvider.isUserHasPermission(currentUser, permissionName)) {
            throw new PermissionDeniedException("权限不足，需要 \"" + permissionName + "\"",permissionName);
        }
    }
    
    @Override
    public boolean hasPermission(String permissionName) {
        return jwtTokenProvider.isUserHasPermission(currentUser, permissionName);
    }
    
    @Override
    public void requireIsCurrentUser(User user) {
        if (!currentUser.equals(user)) {
            throw new InvalidOperatorException("currentUser not match to user[" + user.getName() + "](" + user.getQQNumber() + ")");
        }
    }
    
    @Override
    public void requireIsCurrentUserByQQ(long qqNumber) {
        if (currentUser.getQQNumber() != qqNumber) {
            throw new InvalidOperatorException("currentUser not match to user(" + qqNumber + ")");
        }
    }
    
    @Override
    public boolean isCurrentUser(User user) {
        return currentUser.equals(user);
    }
    
    @Override
    public boolean isCurrentUserByQQ(long qqNumber) {
        return currentUser.getQQNumber() == qqNumber;
    }
    
    @Override
    public I getInput() {
        return input;
    }
    
    @Override
    public void resolve(O output) {
        this.output = output;
    }
    
    @Override
    public O getOutput() {
        return output;
    }
    
}