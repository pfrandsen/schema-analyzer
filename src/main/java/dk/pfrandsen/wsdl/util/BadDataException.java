package dk.pfrandsen.wsdl.util;

public class BadDataException extends Exception {

    public BadDataException() {
    }

    public BadDataException(String message) {
        super(message);
    }
}