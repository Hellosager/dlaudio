package dlaudio;

import org.springframework.beans.factory.annotation.Autowired;

public class StorageException extends RuntimeException {

	@Autowired
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
