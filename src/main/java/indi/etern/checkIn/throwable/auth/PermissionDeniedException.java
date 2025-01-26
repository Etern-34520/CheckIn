package indi.etern.checkIn.throwable.auth;

import lombok.Getter;

@Getter
public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String s, String requiredPermissionName) {
        super(s);
        this.description = s;
        this.requiredPermissionName = requiredPermissionName;
    }

    protected String description;
    protected String requiredPermissionName;
}
