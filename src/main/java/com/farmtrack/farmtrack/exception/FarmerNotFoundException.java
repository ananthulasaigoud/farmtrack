package com.farmtrack.farmtrack.exception;

public class FarmerNotFoundException extends ResourceNotFoundException {

    public FarmerNotFoundException(Long id) {
        super("Farmer not found with id " + id);
    }
}
