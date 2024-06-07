package indi.etern.checkIn.action;

import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.auth.PermissionDeniedException;
import indi.etern.checkIn.entities.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.concurrent.Callable;

public abstract class BaseAction<Res, InitDataType> implements Callable<Optional<Res>> {
    @Getter(AccessLevel.PROTECTED)
    final User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    protected boolean logging = true;

    @Override
    public Optional<Res> call() throws Exception {
        try {
            String requiredPermissionName = requiredPermissionName();
            if (requiredPermissionName != null && !requiredPermissionName.isEmpty())
                for (String s : requiredPermissionName.split(",")) {
                    if (!JwtTokenProvider.currentUserHasPermission(s)) {
                        throw new PermissionDeniedException("权限不足，需要" + s,s);
                    }
                }
            return doAction();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    abstract public String requiredPermissionName();

    abstract protected Optional<Res> doAction() throws Exception;

    /*public Optional<Res> getLogMessage(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Res> result) {
        return result;
    }*/

    public boolean shouldLogging() {
        return logging;
    }

    public void initData(InitDataType dataObj) {}

    protected abstract void preLog();
}
