package pluralsight.m6.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class InSecureMD5PasswordEncoder {
    public String encodeWithSalt(final CharSequence rawPassword, final String salt) {
        return "{" + salt + "}" + DigestUtils.md5Hex(salt + rawPassword.toString());
    }

    public String encodeWithoutSalt(final CharSequence rawPassword) {
        return DigestUtils.md5Hex(rawPassword.toString());
    }
};
