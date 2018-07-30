package dlaudio;

import org.springframework.beans.factory.annotation.Autowired;


public class StorageFileNotFoundException extends StorageException {

	@Autowired
    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}