package com.farmtrack.farmtrack.repository;

import com.farmtrack.farmtrack.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CropRepository extends JpaRepository<Crop, Long> {

    List<Crop> findByFarmerIdOrderByIdDesc(Long farmerId);

    long countByFarmerId(Long farmerId);
}
