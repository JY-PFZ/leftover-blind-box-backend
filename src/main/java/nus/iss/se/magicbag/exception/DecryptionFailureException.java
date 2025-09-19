package nus.iss.se.magicbag.exception;

public class DecryptionFailureException extends RuntimeException {
    public DecryptionFailureException(String message) {
        super(message);
    }
}
