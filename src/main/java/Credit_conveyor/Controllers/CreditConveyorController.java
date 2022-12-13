package Credit_conveyor.Controllers;

import Credit_conveyor.Services.Facade;
import Credit_conveyor.model.LoanApplicationRequestDTO;
import Credit_conveyor.model.LoanOfferDTO;
import Credit_conveyor.model.ScoringDataDTO;
import Credit_conveyor.model.CreditDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RequiredArgsConstructor
@Component
@RestController
@RequestMapping("/conveyor")
public class CreditConveyorController {
    private final CreditConveyorService service;

    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDTO>> offerController(@RequestBody LoanApplicationRequestDTO request) {
        return ResponseEntity.ok(service.generateOffers(request));
    }

    @PostMapping("/calculation")
    public ResponseEntity<CreditDTO> calculationController(@RequestBody ScoringDataDTO request) {
        return ResponseEntity.ok(service.generateCredit(request));
    }
}
