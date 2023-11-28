package indi.etern.checkIn.entities.user;

import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "Users")
public class User {
    protected String name;
    @Id
    protected long QQNumber;
    protected String password;
    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH,CascadeType.REFRESH,CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_TYPE")
    @NotFound(action= NotFoundAction.IGNORE)
    protected Role role;
    public User(String name, long QQNumber, String password) {
        this.name = name;
        this.QQNumber = QQNumber;
        this.password = password;
        role = Role.getInstance("user");
    }
    
    protected User() {
    }
    
    public static User exampleOfName(String name) {
        final User user = new User();
        user.name = name;
        return user;
    }
    
    public static User exampleOfQQNumber(int qqNumber){
        final User user = new User();
        user.QQNumber = qqNumber;
        return user;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    public long getQQNumber() {
        return QQNumber;
    }
    public void setQQNumber(int QQNumber) {
        this.QQNumber = QQNumber;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
    int getRole() {
        return role.hashCode();
    }
    public void setRole(Role role) {
        this.role.getUsers().remove(this);
        this.role = role;
        role.getUsers().add(this);
    }
//    @Override
    public String getStaticHash() {
        return String.valueOf(QQNumber);
    }
    @Override
    public String toString() {
        return "name:" + name + ";QQ:" + QQNumber + ";password:" + password;
    }
    @Override
    public boolean equals(Object object) {
        if (object instanceof User user) {
            return user.QQNumber == this.QQNumber;
        } else {
            return false;
        }
    }
}
