package Credit_conveyor.Controllers;

import Credit_conveyor.Services.CreditConveyorService;
import Credit_conveyor.model.LoanApplicationRequestDTO;
import Credit_conveyor.model.LoanOfferDTO;
import Credit_conveyor.model.ScoringDataDTO;
import Credit_conveyor.model.CreditDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Credit conveyor", description = "Credit conveyor")
@RequestMapping("/conveyor")
public class CreditConveyorController {
    private final CreditConveyorService service;

    @Operation(summary = "Создание 4 предложений")
    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDTO>> offerController(@RequestBody LoanApplicationRequestDTO request) {
        return ResponseEntity.ok(service.generateOffers(request));
    }

    @Operation(summary = "Расчёт кредита")
    @PostMapping("/calculation")
    public ResponseEntity<CreditDTO> calculationController(@RequestBody ScoringDataDTO request) {
        return ResponseEntity.ok(service.generateCredit(request));
    }
}
