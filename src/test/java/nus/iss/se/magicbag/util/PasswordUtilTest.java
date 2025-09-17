package nus.iss.se.magicbag.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void encode() {
        PasswordUtil util = new PasswordUtil();

        String pwd = "123456";

        String encoded1 = util.encode(pwd);
        String encoded2 = util.encode(pwd);

        System.out.println("明文: " + pwd);
        System.out.println("加密1: " + encoded1);
        System.out.println("加密2: " + encoded2);
        System.out.println("是否匹配: " + util.matches(pwd, encoded1));
    }
}