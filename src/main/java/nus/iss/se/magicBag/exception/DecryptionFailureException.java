package nus.iss.se.magicBag.exception;

public class DecryptionFailureException extends RuntimeException {
    public DecryptionFailureException(String message) {
        super(message);
    }
}
