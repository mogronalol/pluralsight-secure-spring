package pluralsight.m15.security;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class MaskedToString {
    final public String toString(){
        return ToStringBuilder.reflectionToString(this, new ToStringWithMasking(this));
    }
}
