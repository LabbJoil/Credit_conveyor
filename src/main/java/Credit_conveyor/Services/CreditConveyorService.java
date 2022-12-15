package Credit_conveyor.Services;

import Credit_conveyor.Exceptions.IncorrectFieldOrRefusedException;
import Credit_conveyor.model.*;
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
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CreditConveyorService {

    private double mainRate = 22;
    private final Integer insurance = 450;
    private double totalPayments;
    private static LoanApplicationRequestDTO loanApplicationRequest;
    private ScoringDataDTO scoringData;
    private static long chiefId;
    private static final String namePattern = "^[a-zA-Z]*$";
    private static final String emailPattern = "[\\w\\.]{2,50}@[\\w\\.]{2,20}";
    private static final String digitPattern = "^[0-9]*$";

    public List generateOffers(LoanApplicationRequestDTO requestLoanApplication) {
        loanApplicationRequest = requestLoanApplication;

        Prescoring(false);
        createChiefAppId();

        List<LoanOfferDTO> list_loan_offers = new ArrayList<>();

        boolean insurance_bool = true, salary_bool = false;
        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) insurance_bool = !insurance_bool;
            else salary_bool = !salary_bool;

            double personalRate = mainRate, amountFromBank = loanApplicationRequest.getAmount().doubleValue();
            if (salary_bool) personalRate--;
            if (insurance_bool) {
                personalRate -= 2;
                amountFromBank += сalculateAmountWithInsuranceSalary(loanApplicationRequest.getAmount());
            }

            BigDecimal personalMonthlyPayment = calculateMonthlyPayment(amountFromBank, loanApplicationRequest.getTerm(), mainRate);

            list_loan_offers.add(new LoanOfferDTO().builder()
                    .requestedAmount(loanApplicationRequest.getAmount())
                    .term(loanApplicationRequest.getTerm())
                    .monthlyPayment(personalMonthlyPayment)
                    .totalAmount(personalMonthlyPayment.multiply(new BigDecimal(loanApplicationRequest.getTerm())))
                    .rate(new BigDecimal(personalRate))
                    .isInsuranceEnabled(insurance_bool)
                    .isSalaryClient(salary_bool)
                    .applicationId(createPersonalAppId(personalMonthlyPayment.doubleValue(), personalRate))
                    .build());
        }
        Collections.sort(list_loan_offers, (o2, o1) -> o1.getRate().subtract(o2.getRate()).intValue());

        return list_loan_offers;
    }

    public CreditDTO generateCredit(ScoringDataDTO requestScoringData) {
        scoringData = requestScoringData;

        Prescoring(true);
        double personalRate = Scoring(), amountFromBank = scoringData.getAmount().doubleValue();

        if (scoringData.getIsSalaryClient()) personalRate--;
        if (scoringData.getIsInsuranceEnabled()) {
            personalRate -= 2;
            amountFromBank += сalculateAmountWithInsuranceSalary(scoringData.getAmount());
        }

        BigDecimal personalMonthlyPayment = calculateMonthlyPayment(amountFromBank, scoringData.getTerm(), personalRate);
        List<PaymentScheduleElementDTO> listPaymentSchedule = createListPaymentSchedule(personalMonthlyPayment.doubleValue(),
                amountFromBank, scoringData.getTerm(), personalRate);

        BigDecimal personalPsk = new BigDecimal((totalPayments / amountFromBank - 1) / (scoringData.getTerm() / 12) * 100)
                .setScale(3, BigDecimal.ROUND_HALF_UP);

        return new CreditDTO().builder()
                .amount(new BigDecimal(totalPayments).setScale(3, BigDecimal.ROUND_HALF_UP))
                .term(scoringData.getTerm())
                .rate(new BigDecimal(personalRate))
                .psk(personalPsk)
                .monthlyPayment(personalMonthlyPayment)
                .isInsuranceEnabled(scoringData.getIsInsuranceEnabled())
                .isSalaryClient(scoringData.getIsSalaryClient())
                .paymentSchedule(listPaymentSchedule).build();
    }


    private void Prescoring(boolean isScoringData) {

        if (!isScoringData) {
            if (loanApplicationRequest.getAmount().compareTo(new BigDecimal(10000)) == -1 ||
                    loanApplicationRequest.getTerm() < 6 ||
                    loanApplicationRequest.getFirstName().length() > 29 || !loanApplicationRequest.getFirstName().matches(namePattern) ||
                    loanApplicationRequest.getMiddleName().length() > 29 || !loanApplicationRequest.getMiddleName().matches(namePattern) ||
                    loanApplicationRequest.getLastName().length() > 29 || !loanApplicationRequest.getLastName().matches(namePattern) ||
                    loanApplicationRequest.getPassportNumber().length() != 6 || !loanApplicationRequest.getPassportNumber().matches(digitPattern) ||
                    loanApplicationRequest.getPassportSeries().length() != 4 || !loanApplicationRequest.getPassportSeries().matches(digitPattern) ||
                    !loanApplicationRequest.getEmail().matches(emailPattern) ||
                    checkDate(loanApplicationRequest.getBirthdate()) < 18)
                throw new IncorrectFieldOrRefusedException("Incorrect data entered", 1001);
        }
        else if (scoringData.getAmount().compareTo(new BigDecimal(10000)) == -1 ||
                scoringData.getTerm() < 6 ||
                scoringData.getFirstName().length() > 29 || !scoringData.getFirstName().matches(namePattern) ||
                scoringData.getMiddleName().length() > 29 || !scoringData.getMiddleName().matches(namePattern) ||
                scoringData.getLastName().length() > 29 || !scoringData.getLastName().matches(namePattern) ||
                scoringData.getPassportNumber().length() != 6 || !scoringData.getPassportNumber().matches(digitPattern) ||
                scoringData.getPassportSeries().length() != 4 || !scoringData.getPassportSeries().matches(digitPattern) ||
                checkDate(scoringData.getBirthdate()) < 18)
            throw new IncorrectFieldOrRefusedException("Incorrect data entered", 1002);
    }

    private double Scoring() {
        double newPersonalRate = mainRate;
        EmploymentDTO newEmployment = scoringData.getEmployment();
        int personAge = checkDate(scoringData.getBirthdate());
        EmploymentStatus employmentStatusEnum = newEmployment.getEmploymentStatus();
        MaritalStatus maritalStatusEnum = scoringData.getMaritalStatus();
        Gender genderEnum = scoringData.getGender();
        Position positionEnum = newEmployment.getPosition();

        if (newEmployment.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED ||
                personAge > 60 || personAge < 20 ||
                newEmployment.getWorkExperienceTotal() < 12 || newEmployment.getWorkExperienceCurrent() < 3 ||
                scoringData.getAmount().compareTo(newEmployment.getSalary().multiply(new BigDecimal(20))) == 1)
            throw new IncorrectFieldOrRefusedException("Отказано", 1300);

        if (employmentStatusEnum == EmploymentStatus.SELFEMPLOYED) newPersonalRate++;
        else if (employmentStatusEnum == EmploymentStatus.BUSINESSOWNER) newPersonalRate += 3;
        if (positionEnum == Position.SIMPLEMANAGER) newPersonalRate -= 2;
        else if (positionEnum == Position.TOPMANAGER) newPersonalRate -= 4;
        if (maritalStatusEnum == MaritalStatus.MARRIED) newPersonalRate -= 3;
        else if (maritalStatusEnum == MaritalStatus.DIVORCED) newPersonalRate++;
        if (genderEnum == Gender.FEMALE && personAge > 35) newPersonalRate -= 3;
        else if (genderEnum == Gender.MALE && personAge > 30 && personAge < 55) newPersonalRate -= 3;
        else if (genderEnum == Gender.NONBINARY) newPersonalRate += 3;
        if (scoringData.getDependentAmount() > 1) newPersonalRate++;
        return newPersonalRate;
    }


    private static Integer checkDate(LocalDate birthday) {
        LocalDate nowDate = LocalDate.now();
        birthday = LocalDate.of(birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());
        nowDate = LocalDate.of(nowDate.getYear(), nowDate.getMonthValue(), nowDate.getDayOfMonth());
        LocalDate differenceDates = LocalDate.of(0, 1, 1).plusDays(ChronoUnit.DAYS.between(birthday, nowDate) - 1);

        return differenceDates.getYear();
    }


    private double сalculateAmountWithInsuranceSalary(BigDecimal amount) {

        int intAmount = amount.intValue(), amountForCount = amount.intValue(), countDigitalAmount = 0, minAmountLength = 5;
        while (amountForCount != 0) {
            amountForCount /= 10;
            countDigitalAmount++;
        }
        Integer firstDigitAmount = (int) (intAmount / Math.pow(10, (int) Math.log10(intAmount) - 1 - (countDigitalAmount - minAmountLength)));
        double newAmount = insurance * firstDigitAmount;

        return newAmount;
    }

    private long createPersonalAppId(double monthlyPayment, double personalRate) {
        long personalId = chiefId + (long) (monthlyPayment * personalRate);
        return personalId;
    }

    private static void createChiefAppId() {
        String all_str_fields_request = loanApplicationRequest.getFirstName() + loanApplicationRequest.getMiddleName() + loanApplicationRequest.getLastName() + loanApplicationRequest.getEmail() + loanApplicationRequest.getPassportNumber() + loanApplicationRequest.getPassportSeries() + loanApplicationRequest.getBirthdate();
        chiefId = (all_str_fields_request).chars().sum();
        chiefId += loanApplicationRequest.getTerm();
        chiefId += loanApplicationRequest.getAmount().intValue();
    }


    private BigDecimal calculateMonthlyPayment(double amount, Integer term, double rate) {
        double rateMonth = rate / 12 / 100;
        double ratioAnnuity = (rateMonth * Math.pow((1 + rateMonth), term)) / (Math.pow((1 + rateMonth), term) - 1);
        BigDecimal monthlyPayment = new BigDecimal(amount * ratioAnnuity);
        return monthlyPayment.setScale(2, BigDecimal.ROUND_HALF_UP);
    }


    private List createListPaymentSchedule(double monthlyPayment, double amount, Integer term, double rate) {
        List<PaymentScheduleElementDTO> paymentSchedule = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate nextPaymentDay = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        totalPayments = 0;
        double bodyCredit, amountOfInterest, rateInDigital = rate / 100;
        long yearDays, mounthDays;

        for (int i = 1; i <= term; i++) {

            mounthDays = ChronoUnit.DAYS.between(nextPaymentDay, nextPaymentDay.plusMonths(1));
            nextPaymentDay = nextPaymentDay.plusMonths(1);

            yearDays = nextPaymentDay.lengthOfYear();
            amountOfInterest = amount * rateInDigital * mounthDays / yearDays;
            if (i == term) monthlyPayment = amount + amountOfInterest;


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

        return paymentSchedule;
    }

}
