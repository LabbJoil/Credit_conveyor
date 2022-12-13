package Credit_conveyor.Services;

import Credit_conveyor.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CreditConveyorService {

    public List generateOffers(LoanApplicationRequestDTO requestLoanApplication) {
        СheckCorrectReceiveData.CheckReceiveData(requestLoanApplication);
        List<LoanOfferDTO> list_loan_offers = new ArrayList<>();

        CreateLoanOffer.setRequest(requestLoanApplication);
        CreateLoanOffer.createChiefAppId();
        boolean insurance_bool = true, salary_bool = false;
        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) insurance_bool = !insurance_bool;
            else salary_bool = !salary_bool;
            CreateLoanOffer nextLoanOffer = new CreateLoanOffer();
            list_loan_offers.add(nextLoanOffer.newLoanOffer(insurance_bool, salary_bool));
        }

        Collections.sort(list_loan_offers, (o1, o2) -> o1.getRate().subtract(o2.getRate()).intValue());

        return list_loan_offers;
    }

    public CreditDTO generateCredit(ScoringDataDTO requestScoringData) {
        Integer personAge = СheckCorrectReceiveData.CheckReceiveData(requestScoringData);
        CalculationCredit newCredit = new CalculationCredit();
        return newCredit.calculateCredit(requestScoringData, personAge);
    }
}
