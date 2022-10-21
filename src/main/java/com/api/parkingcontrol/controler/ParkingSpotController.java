package com.api.parkingcontrol.controler;

import com.api.parkingcontrol.dto.ParkingSpotDto;
import com.api.parkingcontrol.model.ParkingSpotModel;
import com.api.parkingcontrol.service.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    @Autowired
    ParkingSpotService parkingSpotService;

    /* Para poder usar dois gets com o mesmo path, é necessário acrescentar o "params" dentro da anotação
     *  @GetMapping, para diferenciar qual método GET está sendo chamado. */
    @GetMapping(params = "parkingSpotNumber")
    public ResponseEntity<Object> getParkingSpotByNumber(@RequestParam String parkingSpotNumber) {
        List<ParkingSpotModel> parkingSpotModelList = parkingSpotService.getParkingSpotByNumber(parkingSpotNumber);
        if (parkingSpotModelList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não foi encontrada nenhuma vaga contendo o código informado.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelList);
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.getAllParkingSpots(pageable));
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
        if (parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito. A placa informada já está em uso.");
        }
        if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito. A vaga informada já está sendo utilizada.");
        }
        if(parkingSpotService.existsByApartmentAndBLock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito. O apartamento e bloco informados já estão em uso.");
        }

        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));

        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }

    @DeleteMapping
    public ResponseEntity deleteParkingSpotByNumber(@RequestParam String parkingSpotNumber) {
        List<ParkingSpotModel> parkingSpotModelList = parkingSpotService.getParkingSpotByNumber(parkingSpotNumber);
        if (parkingSpotModelList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O código informado não existe. Nenhuma vaga foi excluída.");
        }
        parkingSpotService.deleteParkingSpotByNumber(parkingSpotNumber);
        return ResponseEntity.status(HttpStatus.OK).body(String.format("A vaga %s foi deletada com sucesso.", parkingSpotNumber));
    }

    @PutMapping
    public ResponseEntity<Object> updateParkingSpot(@RequestParam UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if (parkingSpotModelOptional.isPresent()) {
            var parkingSpotModel = parkingSpotModelOptional.get();
            BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
            parkingSpotModel.setId(parkingSpotModelOptional.get().getId());

            return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A vaga não foi encontrada.");
    }

}
