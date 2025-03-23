package indi.etern.checkIn.action.interfaces;

import indi.etern.checkIn.entities.user.User;

public interface ExecuteContext<I extends InputData,O extends OutputData> {
    void requirePermission(String permissionName);
    
    boolean hasPermission(String permissionName);
    
    void requireIsCurrentUser(User user);
    
    void requireIsCurrentUserByQQ(long qqNumber);
    
    boolean isCurrentUser(User user);
    
    boolean isCurrentUserByQQ(long qqNumber);
    
    I getInput();
    
    void resolve(O output);
    
    User getCurrentUser();
}
