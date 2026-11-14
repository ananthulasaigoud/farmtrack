package com.farmtrack.farmtrack.exception;

public class CropNotFoundException extends ResourceNotFoundException {

    public CropNotFoundException(Long id) {
        super("Crop not found with id " + id);
    }
}
