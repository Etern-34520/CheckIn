package indi.etern.checkIn.service.dao.verify;
import indi.etern.checkIn.throwable.entity.VerifyException;
import java.lang.reflect.Field;
import java.util.Collection;

public class ReflectionUtil {
    public static Object getFieldValue(Object obj, String fieldName) {
        if (fieldName.equals("length")) {
            if (obj instanceof Collection<?> collection) {
                return collection.size();
            } else if (obj instanceof String str) {
                return str.length();
            }
        }
        try {
            // 尝试通过getter方法获取
            String getterName = "get" + capitalize(fieldName);
            try {
                return obj.getClass().getMethod(getterName).invoke(obj);
            } catch (NoSuchMethodException e) {
                // 尝试直接访问字段
                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            }
        } catch (Exception e) {
            throw new VerifyException("Failed to access field: " + fieldName);
        }
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}