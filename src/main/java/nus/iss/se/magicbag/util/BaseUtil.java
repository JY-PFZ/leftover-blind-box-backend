package nus.iss.se.magicbag.util;

import jakarta.servlet.http.HttpServletRequest;

public class BaseUtil {
    /**
     * 从请求头中提取 Bearer Token
     */
    public static String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
