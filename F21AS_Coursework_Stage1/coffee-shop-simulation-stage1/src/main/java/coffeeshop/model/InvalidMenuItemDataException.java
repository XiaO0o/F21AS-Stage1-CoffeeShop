package coffeeshop.model;

public class InvalidMenuItemDataException extends Exception {

    public InvalidMenuItemDataException(String message) {
        super(message);
    }

    public InvalidMenuItemDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
