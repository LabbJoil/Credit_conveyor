package Credit_conveyor.Services;

import Credit_conveyor.Exceptions.IncorrectFieldOrRefusedException;
import Credit_conveyor.model.CreditDTO;
import Credit_conveyor.model.EmploymentDTO;
import Credit_conveyor.model.PaymentScheduleElementDTO;
import Credit_conveyor.model.ScoringDataDTO;
import Credit_conveyor.model.enums.EmploymentStatus;
import Credit_conveyor.model.enums.Gender;
import Credit_conveyor.model.enums.MaritalStatus;
import Credit_conveyor.model.enums.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CalculationCredit extends MonthlyPaymentAndInsurance {
    private double personalRate = 22;
    private BigDecimal personalPsk;
    private BigDecimal personalTotalAmount;
    private ScoringDataDTO scoringData;

    public CreditDTO calculateCredit(ScoringDataDTO request, Integer personAge) {
        scoringData = request;
        personalTotalAmount = request.getAmount();

        calculateRate(request.getEmployment().getEmploymentStatus(),
                request.getMaritalStatus(),
                request.getGender(),
                request.getEmployment().getPosition(), personAge,
                request.getIsInsuranceEnabled(), request.getIsSalaryClient());

        if (request.getIsInsuranceEnabled())
            personalTotalAmount = personalTotalAmount.add(calculateAmountWithInsurance(request.getAmount()));

        BigDecimal personalMonthlyPayment = calculateMonthlyPayment(personalTotalAmount, request.getTerm(), personalRate);
        List<PaymentScheduleElementDTO> listPaymentSchedule = createListPaymentSchedule(personalMonthlyPayment.doubleValue(),
                personalTotalAmount.doubleValue(), request.getTerm());

        return new CreditDTO().builder()
                .amount(this.personalTotalAmount)
                .term(request.getTerm())
                .rate(new BigDecimal(personalRate))
                .psk(personalPsk)
                .monthlyPayment(personalMonthlyPayment)
                .isInsuranceEnabled(request.getIsInsuranceEnabled())
                .isSalaryClient(request.getIsSalaryClient())
                .paymentSchedule(listPaymentSchedule).build();
    }

    private void calculateRate(Enum employmentStatusEnum, Enum maritalStatusEnum, Enum genderEnum, Enum positionEnum,
                               Integer personAge, boolean isInsurance, boolean isSalary) {
        EmploymentDTO newEmployment = scoringData.getEmployment();

        if (employmentStatusEnum == EmploymentStatus.UNEMPLOYED)
            throw new IncorrectFieldOrRefusedException("Отказано", 1301);
        if (personAge > 60 || personAge < 20) throw new IncorrectFieldOrRefusedException("Отказано", 1302);
        if (newEmployment.getWorkExperienceTotal() < 12 || newEmployment.getWorkExperienceCurrent() < 3)
            throw new IncorrectFieldOrRefusedException("Отказано", 1303);
        if (personalTotalAmount.compareTo(newEmployment.getSalary().multiply(new BigDecimal(20))) == 1)
            throw new IncorrectFieldOrRefusedException("Отказано", 1304);

        if (employmentStatusEnum == EmploymentStatus.SELFEMPLOYED) personalRate++;
        else if (employmentStatusEnum == EmploymentStatus.BUSINESSOWNER) personalRate += 3;
        if (positionEnum == Position.SIMPLEMANAGER) personalRate -= 2;
        else if (positionEnum == Position.TOPMANAGER) personalRate -= 4;
        if (maritalStatusEnum == MaritalStatus.MARRIED) personalRate -= 3;
        else if (maritalStatusEnum == MaritalStatus.DIVORCED) personalRate++;
        if (genderEnum == Gender.FEMALE && personAge > 35) personalRate -= 3;
        else if (genderEnum == Gender.MALE && personAge > 30 && personAge < 55) personalRate -= 3;
        else if (genderEnum == Gender.NONBINARY) personalRate += 3;
        if (scoringData.getDependentAmount() > 1) personalRate++;
        if (isSalary) personalRate--;
        if (isInsurance) personalRate -= 2;
    }

    private List createListPaymentSchedule(double monthlyPayment, double amount, Integer term) {
        List<PaymentScheduleElementDTO> paymentSchedule = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate nextPaymentDay = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        double bodyCredit, amountOfInterest, differenceMonthlyPayment = monthlyPayment, totalPayments = 0, rateInDigital = personalRate / 100;
        long yearDays, mounthDays;

        for (int i = 1; i <= term; i++) {

            mounthDays = ChronoUnit.DAYS.between(nextPaymentDay, nextPaymentDay.plusMonths(1));
            nextPaymentDay = nextPaymentDay.plusMonths(1);

            yearDays = nextPaymentDay.lengthOfYear();
            amountOfInterest = amount * rateInDigital * mounthDays / yearDays;
            if (i == term) {
                monthlyPayment = amount + amountOfInterest;
                differenceMonthlyPayment -= monthlyPayment;
            }

            bodyCredit = monthlyPayment - amountOfInterest;
            amount -= bodyCredit;
            totalPayments += monthlyPayment;

            paymentSchedule.add(new PaymentScheduleElementDTO().builder()
                    .number(new BigDecimal(i))
                    .date(nextPaymentDay)
                    .totalPayment(new BigDecimal(monthlyPayment).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .interestPayment(new BigDecimal(amountOfInterest).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .debtPayment(new BigDecimal(bodyCredit).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .remainingDebt(new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .build());
        }

        personalPsk = new BigDecimal(personalRate - differenceMonthlyPayment * (200 + personalRate) / totalPayments)
                .setScale(3, BigDecimal.ROUND_HALF_UP);
        personalTotalAmount = new BigDecimal(totalPayments)
                .setScale(3, BigDecimal.ROUND_HALF_UP);
        ;

        return paymentSchedule;
    }
}
