package indi.etern.checkIn.entities.question;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
@MappedSuperclass
@Table(name = "MULTI_PARTITIONABLE_QUESTIONS")
public abstract class Question implements Serializable {
    @Column(name = "content")
    protected String content;
    @Transient
    protected int hashcode = super.hashCode();
    
    @Override
    public int hashCode() {
        return hashcode;
    }
    
    protected Question() {
    }
    @Id
    @Column(name = "id")
    protected String md5;
    public String getMd5() {
        return md5;
    }
    protected void initMD5(){
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
        } catch (NoSuchAlgorithmException e) {
            // impossible
        }
    }
    public String getContent() {
        return content;
    }
    public String getStaticHash() {
        return md5;
    }
    @Override
    public boolean equals(Object object) {
        if (object instanceof Question question) {
            return question.toString().equals(this.toString());
        } else {
            return false;
        }
    }
}
