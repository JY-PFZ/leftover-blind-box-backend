package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    INACTIVE(0, "Account Not Activated"),
    ACTIVE(1, "Account Activated"),
    LOCKED(2, "Account Lock"),
    DELETED(3, "Account Deleted");

    private final int code;
    private final String description;
}
