package Credit_conveyor.Services;

import Credit_conveyor.model.*;
import Credit_conveyor.model.enums.EmploymentStatus;
import Credit_conveyor.model.enums.Gender;
import Credit_conveyor.model.enums.MaritalStatus;
import Credit_conveyor.model.enums.Position;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreditConveyorServiceTest {

    private final CreditConveyorService creditConveyorServiceTest = new CreditConveyorService();
    private LoanApplicationRequestDTO loanApplicationRequestTest = new LoanApplicationRequestDTO().builder()
            .amount(new BigDecimal(12000))
            .term(36)
            .firstName("Lisa")
            .lastName("Volkova")
            .middleName("Alexandrovna")
            .email("12eqt@gmail.com")
            .birthdate(LocalDate.of(1985, 12, 1))
            .passportSeries("1234")
            .passportNumber("567890").build();

    private ScoringDataDTO scoringDataTest = new ScoringDataDTO().builder()
            .amount(new BigDecimal(100000))
            .term(7)
            .firstName("Lisa")
            .lastName("Volkova")
            .middleName("Alexandrovna")
            .gender(Gender.FEMALE)
            .birthdate(LocalDate.of(1985, 12, 1))
            .passportSeries("1234")
            .passportNumber("567890")
            .passportIssueDate(LocalDate.of(1999, 1, 9))
            .passportIssueBranch("отделом внутренних дел одинцовского района города МОСКВЫ")
            .maritalStatus(MaritalStatus.MARRIED)
            .dependentAmount(1)
            .employment(new EmploymentDTO().builder()
                    .employmentStatus(EmploymentStatus.SELFEMPLOYED)
                    .employerINN("1234567890")
                    .salary(BigDecimal.valueOf(123000))
                    .position(Position.TOPMANAGER)
                    .workExperienceTotal(36)
                    .workExperienceCurrent(3).build())
            .account("First1")
            .isInsuranceEnabled(true)
            .isSalaryClient(true).build();

    List<LoanOfferDTO> listLoanOfferTest = Arrays.asList(
            new LoanOfferDTO().builder()
                    .applicationId((long) 26906)
                    .requestedAmount(new BigDecimal(12000).setScale(1))
                    .term(36)
                    .monthlyPayment(new BigDecimal(458.29).setScale(2, RoundingMode.HALF_EVEN))
                    .totalAmount(new BigDecimal(16498.44).setScale(2, RoundingMode.HALF_EVEN))
                    .rate(new BigDecimal(22).setScale(1))
                    .isInsuranceEnabled(false)
                    .isSalaryClient(false)
                    .build(),
            new LoanOfferDTO().builder()
                    .applicationId((long) 26318)
                    .requestedAmount(new BigDecimal(12000).setScale(1))
                    .term(36)
                    .monthlyPayment(new BigDecimal(452.10).setScale(2, RoundingMode.HALF_EVEN))
                    .totalAmount(new BigDecimal(16275.60).setScale(2, RoundingMode.HALF_EVEN))
                    .rate(new BigDecimal(21).setScale(1))
                    .isInsuranceEnabled(false)
                    .isSalaryClient(true)
                    .build(),
            new LoanOfferDTO().builder()
                    .applicationId((long) 29459)
                    .requestedAmount(new BigDecimal(12000).setScale(1))
                    .term(36)
                    .monthlyPayment(new BigDecimal(631.78).setScale(2, RoundingMode.HALF_EVEN))
                    .totalAmount(new BigDecimal(22744.08).setScale(2, RoundingMode.HALF_EVEN))
                    .rate(new BigDecimal(20).setScale(1))
                    .isInsuranceEnabled(true)
                    .isSalaryClient(false)
                    .build(),
            new LoanOfferDTO().builder()
                    .applicationId((long) 28663)
                    .requestedAmount(new BigDecimal(12000).setScale(1))
                    .term(36)
                    .monthlyPayment(new BigDecimal(623.15).setScale(2, RoundingMode.HALF_EVEN))
                    .totalAmount(new BigDecimal(22433.40).setScale(2, RoundingMode.HALF_EVEN))
                    .rate(new BigDecimal(19).setScale(1))
                    .isInsuranceEnabled(true)
                    .isSalaryClient(true)
                    .build());

    private final LocalDate paymentDay = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
    private CreditDTO creditTest = new CreditDTO().builder()
            .amount(BigDecimal.valueOf(143952.160).setScale(3))
            .term(7)
            .monthlyPayment(BigDecimal.valueOf(20566.73))
            .rate(BigDecimal.valueOf(10.0))
            .psk(BigDecimal.valueOf(5.743))
            .isInsuranceEnabled(true)
            .isSalaryClient(true)
            .paymentSchedule(Arrays.asList(new PaymentScheduleElementDTO().builder()
                            .number(BigDecimal.valueOf(1))
                            .date(paymentDay.plusMonths(1))
                            .totalPayment(BigDecimal.valueOf(20566.73))
                            .interestPayment(BigDecimal.valueOf(1182.97))
                            .debtPayment(BigDecimal.valueOf(19383.76))
                            .remainingDebt(BigDecimal.valueOf(119901.96)).build(),
                    new PaymentScheduleElementDTO().builder()
                            .number(BigDecimal.valueOf(2))
                            .date(paymentDay.plusMonths(2))
                            .totalPayment(BigDecimal.valueOf(20566.73))
                            .interestPayment(BigDecimal.valueOf(1018.35))
                            .debtPayment(BigDecimal.valueOf(19548.38))
                            .remainingDebt(BigDecimal.valueOf(100353.58)).build(),
                    new PaymentScheduleElementDTO().builder()
                            .number(BigDecimal.valueOf(3))
                            .date(paymentDay.plusMonths(3))
                            .totalPayment(BigDecimal.valueOf(20566.73))
                            .interestPayment(BigDecimal.valueOf(769.84))
                            .debtPayment(BigDecimal.valueOf(19796.89))
                            .remainingDebt(BigDecimal.valueOf(80556.69)).build(),
                    new PaymentScheduleElementDTO().builder()
                            .number(BigDecimal.valueOf(4))
                            .date(paymentDay.plusMonths(4))
                            .totalPayment(BigDecimal.valueOf(20566.73))
                            .interestPayment(BigDecimal.valueOf(684.18))
                            .debtPayment(BigDecimal.valueOf(19882.55))
                            .remainingDebt(BigDecimal.valueOf(60674.14)).build(),
                    new PaymentScheduleElementDTO().builder()
                            .number(BigDecimal.valueOf(5))
                            .date(paymentDay.plusMonths(5))
                            .totalPayment(BigDecimal.valueOf(20566.73))
                            .interestPayment(BigDecimal.valueOf(498.69))
                            .debtPayment(BigDecimal.valueOf(20068.04))
                            .remainingDebt(BigDecimal.valueOf(40606.10).setScale(2)).build(),
                    new PaymentScheduleElementDTO().builder()
                            .number(BigDecimal.valueOf(6))
                            .date(paymentDay.plusMonths(6))
                            .totalPayment(BigDecimal.valueOf(20566.73))
                            .interestPayment(BigDecimal.valueOf(344.87))
                            .debtPayment(BigDecimal.valueOf(20221.86))
                            .remainingDebt(BigDecimal.valueOf(20384.24)).build(),
                    new PaymentScheduleElementDTO().builder()
                            .number(BigDecimal.valueOf(7))
                            .date(paymentDay.plusMonths(7))
                            .totalPayment(BigDecimal.valueOf(20551.78))
                            .interestPayment(BigDecimal.valueOf(167.54))
                            .debtPayment(BigDecimal.valueOf(20384.24))
                            .remainingDebt(BigDecimal.valueOf(0.00).setScale(2)).build()))
            .build();


    @Test
    void generateOffersTrue() {
        List<LoanOfferDTO> listLoanOffer = creditConveyorServiceTest.generateOffers(loanApplicationRequestTest);
        assertEquals(listLoanOfferTest, listLoanOffer);
    }

    @Test
    void generateOffersFalse() {
        loanApplicationRequestTest.setTerm(5);
        try {
            List<LoanOfferDTO> listLoanOffer = creditConveyorServiceTest.generateOffers(loanApplicationRequestTest);
            assertEquals(listLoanOfferTest, listLoanOffer);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void generateCreditTrue() {
        CreditDTO credit = creditConveyorServiceTest.generateCredit(scoringDataTest);
        assertEquals(creditTest, credit);
    }

    @Test
    void generateCreditFalse() {
        try {
            scoringDataTest.setBirthdate(LocalDate.now());
            CreditDTO credit = creditConveyorServiceTest.generateCredit(scoringDataTest);
            assertEquals(creditTest, credit);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}