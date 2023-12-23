package indi.etern.checkIn.entities.question;

import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
@Getter
@MappedSuperclass
@Table(name = "MULTI_PARTITIONABLE_QUESTIONS")
public abstract class Question implements Serializable {
    @Column(name = "content")
    protected String content;
    
//    protected int hashcode;
    
    @JoinColumn(name = "AUTHOR_QQNUMBER")
    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH,CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    protected User author = null;// = User.exampleOfName("unknown");
    
    protected Question() {
    }
    
    @Id
    @Column(name = "id")
    protected String md5;
    
    public void initMD5(){
//        hashcode = super.hashCode();
        String input = toString();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(input.getBytes());
            byte[] byteArray = md5.digest();
            
            BigInteger bigInt = new BigInteger(1, byteArray);
            // 参数16表示16进制
            StringBuilder result = new StringBuilder(bigInt.toString(16));
            // 不足32位高位补零
            while (result.length() < 32) {
                result.insert(0, "0");
            }
            this.md5 = result.toString();
//            this.hashcode = Integer.parseInt(new BigInteger(result.toString(),16).toString().substring(0,9));
        } catch (NoSuchAlgorithmException e) {
            // impossible
        }
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof Question question) {
            return question.toString().equals(this.toString());
        } else {
            return false;
        }
    }
    
    abstract public String toJsonData();
}
