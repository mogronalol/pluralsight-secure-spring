package pluralsight.m15.security;

import lombok.SneakyThrows;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;

public class ToStringWithMasking extends ToStringStyle {
    private final Object maskedToString;

    public ToStringWithMasking(final Object maskedToString) {
        this.maskedToString = maskedToString;
    }

    @SneakyThrows @Override
    public void append(final StringBuffer buffer, final String fieldName, Object value,
                       final Boolean fullDetail) {

        final Field declaredField = maskedToString.getClass().getDeclaredField(fieldName);

        if (!declaredField.isAnnotationPresent(Unmasked.class)) {
            value = "(masked)";
        }

        super.append(buffer, fieldName, value, fullDetail);
    }
}
