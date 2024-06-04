package indi.etern.checkIn.auth;

import lombok.Getter;

@Getter
public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String s, String requiredPermissionName) {
        super(s);
        this.requiredPermissionName = requiredPermissionName;
    }

    protected String requiredPermissionName;
}
