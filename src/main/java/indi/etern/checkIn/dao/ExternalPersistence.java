package indi.etern.checkIn.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a field that should be persisted externally.
 * <br/>
 * Remember to use transient with it.
 * <br/>
 * <br/>
 * 这个注解用于标记一个应该被外部持久化的字段。
 * <br/>
 * 记得和 transient 一起使用。
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExternalPersistence {
}
