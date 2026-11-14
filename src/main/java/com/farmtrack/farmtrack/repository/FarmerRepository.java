package com.farmtrack.farmtrack.repository;

import com.farmtrack.farmtrack.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data JPA creates the implementation for us at runtime, so we don't write the basic SQL ourselves.
public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    // "First" + "OrderByIdAsc" so old data with a repeated phone number does not cause an error
    Optional<Farmer> findFirstByPhoneOrderByIdAsc(String phone);

    boolean existsByPhone(String phone);

    // Is this phone used by any OTHER farmer?
    boolean existsByPhoneAndIdNot(String phone, Long id);
}
