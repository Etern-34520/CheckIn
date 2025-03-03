package indi.etern.checkIn.action;

import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public abstract class BaseAction<Res, InitDataType> {
    @Getter(AccessLevel.PROTECTED)
    final User currentUser;
    {
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

    public Optional<Res> call(InitDataType initData) throws Exception {
        initData(initData);
        try {
            String requiredPermissionName = requiredPermissionName();
            if (requiredPermissionName != null && !requiredPermissionName.isEmpty())
                for (String s : requiredPermissionName.split(",")) {
                    if (!JwtTokenProvider.singletonInstance.isUserHasPermission(currentUser,s)) {
                        throw new PermissionDeniedException("权限不足，需要 \"" + s + "\"",s);
                    }
                }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return doAction();
    }

    abstract public String requiredPermissionName();

    abstract protected Optional<Res> doAction() throws Exception;
    
    public void initData(InitDataType initData) {}
}