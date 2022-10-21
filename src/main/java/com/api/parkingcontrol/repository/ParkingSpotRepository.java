package com.api.parkingcontrol.repository;

import com.api.parkingcontrol.model.ParkingSpotModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpotModel, UUID> {

    public boolean existsByLicensePlateCar(String licensePlateCar);
    public boolean existsByParkingSpotNumber(String parkingSpotNumber);
    public boolean existsByApartmentAndBlock(String apartment, String block);
    public List<ParkingSpotModel> findByParkingSpotNumber (String parkingSpotNumber);

    //public List<ParkingSpotModel> findAll(); Não precisa desse pq já é padrão

    public void deleteByParkingSpotNumber(String parkingSpotNumber);
}
