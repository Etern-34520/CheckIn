package indi.etern.checkIn.service.dao.verify;

import indi.etern.checkIn.throwable.entity.VerifyException;
import indi.etern.checkIn.utils.ApplicationContextUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
                MethodHandle handle = getGetterHandle(obj.getClass(), getterName).bindTo(obj);
                return handle.invoke();
//                return obj.getClass().getMethod(getterName).invoke(obj);
            } catch (NoSuchMethodException e) {
                // 尝试直接访问字段
                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Throwable e) {
                throw new VerifyException("Failed to access field via getter: " + fieldName);
            }
        } catch (Exception e) {
            throw new VerifyException("Failed to access field: " + fieldName);
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static final Cache HANDLE_CACHE = ApplicationContextUtils.getApplicationContext()
            .getBean(CacheManager.class).getCache("handleCache");

    public static MethodHandle getGetterHandle(Class<?> clazz, String getterName) throws Exception {
        String key = clazz.getName() + "." + getterName;
        assert HANDLE_CACHE != null;
        Cache.ValueWrapper valueWrapper = HANDLE_CACHE.get(key);
        if (valueWrapper != null && valueWrapper.get() instanceof MethodHandle methodHandle) {
            return methodHandle;
        } else {
            Method getter = clazz.getMethod(getterName);
            MethodHandle handle = MethodHandles.lookup().unreflect(getter);
            HANDLE_CACHE.put(key, handle);
            return handle;
        }
    }
}