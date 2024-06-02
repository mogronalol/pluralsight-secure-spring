package pluralsight.m13.security;

import lombok.SneakyThrows;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;

public class ToStringWithMasking extends ToStringStyle {

    private final Object targetObject;

    public ToStringWithMasking(Object targetObject) {
        this.targetObject = targetObject;
    }

    @SneakyThrows
    @Override
    public void append(StringBuffer buffer, String fieldName, Object value,
                       Boolean fullDetail) {

        final Field field = targetObject.getClass().getDeclaredField(fieldName);
        if (!field.isAnnotationPresent(Unmasked.class)) {
            value = maskValue(value);
        }
        super.append(buffer, fieldName, value, fullDetail);
    }

    private String maskValue(Object value) {
        return "(masked)";
    }
}