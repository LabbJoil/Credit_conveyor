package Credit_conveyor.Services;

import Credit_conveyor.model.LoanApplicationRequestDTO;
import Credit_conveyor.model.LoanOfferDTO;

import java.math.BigDecimal;

public class CreateLoanOffer extends MonthlyPaymentAndInsurance {
    private double mainRate = 22;
    private static LoanApplicationRequestDTO loanApplicationRequest;
    private static long chiefId;

    public static void setRequest(LoanApplicationRequestDTO request) {
        loanApplicationRequest = request;
    }

    public LoanOfferDTO newLoanOffer(boolean isInsuranceEnabled, boolean isSalaryClient) {

        BigDecimal intermediateTotalAmount = сalculateMainRate(loanApplicationRequest.getAmount(), isInsuranceEnabled, isSalaryClient);
        BigDecimal personalMonthlyPayment = calculateMonthlyPayment(intermediateTotalAmount, loanApplicationRequest.getTerm(), mainRate);

        return new LoanOfferDTO().builder()
                .requestedAmount(loanApplicationRequest.getAmount())
                .term(loanApplicationRequest.getTerm())
                .monthlyPayment(personalMonthlyPayment)
                .totalAmount(personalMonthlyPayment.multiply(new BigDecimal(loanApplicationRequest.getTerm())))
                .rate(new BigDecimal(mainRate))
                .isInsuranceEnabled(isInsuranceEnabled)
                .isSalaryClient(isSalaryClient)
                .applicationId(createPersonalAppId(personalMonthlyPayment.doubleValue()))
                .build();
    }

    private BigDecimal сalculateMainRate(BigDecimal amount, boolean isInsurance, boolean isSalary) {

        if (isSalary) mainRate -= 1;
        if (isInsurance) {
            mainRate -= 2;
            amount.add(calculateAmountWithInsurance(amount));
        }
        return amount;
    }

    private long createPersonalAppId(double monthlyPayment) {
        long personalId = chiefId + (long) (monthlyPayment * mainRate);
        return personalId;
    }

    public static void createChiefAppId() {
        String all_str_fields_request = loanApplicationRequest.getFirstName() + loanApplicationRequest.getMiddleName() + loanApplicationRequest.getLastName() + loanApplicationRequest.getEmail() + loanApplicationRequest.getPassportNumber() + loanApplicationRequest.getPassportSeries() + loanApplicationRequest.getBirthdate();
        chiefId = (all_str_fields_request).chars().sum();
        chiefId += loanApplicationRequest.getTerm();
        chiefId += loanApplicationRequest.getAmount().intValue();
    }

}
