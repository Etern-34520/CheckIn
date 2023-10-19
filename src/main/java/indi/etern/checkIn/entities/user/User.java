package indi.etern.checkIn.entities.user;

import indi.etern.checkIn.dao.PersistableWithStaticHash;

public class User implements PersistableWithStaticHash {
    protected String name;
    protected int QQNumber;
    protected String password;
    protected Role role;
    public User(String name, int QQNumber, String password) {
        this.name = name;
        this.QQNumber = QQNumber;
        this.password = password;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    public int getQQNumber() {
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
    @Override
    public String getStaticHash() {
        return String.valueOf(QQNumber);
    }
    @Override
    public String toString() {
        return name + QQNumber + password + role.hashCode();
    }
    @Override
    public boolean equals(Object object) {
        if (object instanceof User user) {
            return user.name.equals(this.name) && user.QQNumber == this.QQNumber && user.password.equals(this.password);
        } else {
            return false;
        }
    }
}
