package Credit_conveyor.Services;

import Credit_conveyor.Exceptions.IncorrectFieldOrRefusedException;
import Credit_conveyor.model.*;
import Credit_conveyor.model.enums.EmploymentStatus;
import Credit_conveyor.model.enums.Gender;
import Credit_conveyor.model.enums.MaritalStatus;
import Credit_conveyor.model.enums.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.math.BigDecimal.ROUND_HALF_UP;

@RequiredArgsConstructor
@Service
public class CreditConveyorService {
    private final double mainRate = 22;

    public List generateOffers(LoanApplicationRequestDTO requestLoanApplication) {

        prescoring(requestLoanApplication);

        long chiefId = createChiefAppId(requestLoanApplication);
        double personalAmount = requestLoanApplication.getAmount().doubleValue();
        Integer personalTerm = requestLoanApplication.getTerm();
        List<LoanOfferDTO> listLoanOffers = new ArrayList<>();
        boolean isInsurance = true, isSalary = false;

        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) isInsurance = !isInsurance;
            else isSalary = !isSalary;

            double personalRate = mainRate, amountFromBank = personalAmount;
            if (isSalary) personalRate--;
            if (isInsurance) {
                personalRate -= 2;
                amountFromBank += сalculateInsurance(personalAmount);
            }

            BigDecimal personalMonthlyPayment = calculateMonthlyPayment(amountFromBank, personalTerm, personalRate);
            long personalAppId = chiefId + (long) (personalMonthlyPayment.doubleValue() * personalRate);
            BigDecimal personalTotalAmount = personalMonthlyPayment.multiply(BigDecimal.valueOf(personalTerm));

            listLoanOffers.add(new LoanOfferDTO().builder()
                    .requestedAmount(BigDecimal.valueOf(personalAmount))
                    .term(personalTerm)
                    .monthlyPayment(personalMonthlyPayment)
                    .totalAmount(personalTotalAmount)
                    .rate(BigDecimal.valueOf(personalRate))
                    .isInsuranceEnabled(isInsurance)
                    .isSalaryClient(isSalary)
                    .applicationId(personalAppId)
                    .build());
        }
        Collections.sort(listLoanOffers, (o2, o1) -> o1.getRate().subtract(o2.getRate()).intValue());

        return listLoanOffers;
    }

    public CreditDTO generateCredit(ScoringDataDTO requestScoringData) {

        double personalRate = scoring(requestScoringData),
                requestAmount = requestScoringData.getAmount().doubleValue();
        boolean personIsInsurance = requestScoringData.getIsInsuranceEnabled(),
                personIsSalary = requestScoringData.getIsSalaryClient();
        Integer personalTerm = requestScoringData.getTerm();

        if (personIsSalary) personalRate--;
        if (personIsInsurance) {
            personalRate -= 2;
            requestAmount += сalculateInsurance(requestScoringData.getAmount().doubleValue());
        }

        BigDecimal personalMonthlyPayment = calculateMonthlyPayment(requestAmount, personalTerm, personalRate);
        List<PaymentScheduleElementDTO> personPaymentSchedule = createListPaymentSchedule(personalMonthlyPayment.doubleValue(),
                requestAmount, personalTerm, personalRate);

        double personTotalAmount = personPaymentSchedule.stream().mapToDouble(x -> x.getTotalPayment().doubleValue()).sum();
        BigDecimal personalPsk = BigDecimal.valueOf((personTotalAmount / requestAmount - 1) / (personalTerm / 12.0) * 100)
                .setScale(3, ROUND_HALF_UP);

        return new CreditDTO().builder()
                .amount(BigDecimal.valueOf(personTotalAmount).setScale(3, ROUND_HALF_UP))
                .term(personalTerm)
                .rate(BigDecimal.valueOf(personalRate))
                .psk(personalPsk)
                .monthlyPayment(personalMonthlyPayment)
                .isInsuranceEnabled(personIsInsurance)
                .isSalaryClient(personIsSalary)
                .paymentSchedule(personPaymentSchedule).build();
    }

    private void prescoring(LoanApplicationRequestDTO loanApplicationRequest) {

        BigDecimal minAmount = BigDecimal.valueOf(10000);
        if (loanApplicationRequest.getAmount().compareTo(minAmount) == -1)
            throw new IncorrectFieldOrRefusedException("Сумма кредита должна быть больше 10000", 1301);
        if (loanApplicationRequest.getTerm() < 6)
            throw new IncorrectFieldOrRefusedException("Сумма кредита должна быть больше 10000", 1302);
        if (!loanApplicationRequest.getFirstName().matches("[a-zA-Z]{2,30}"))
            throw new IncorrectFieldOrRefusedException("Некорректно заполнено имя", 1303);
        if (!loanApplicationRequest.getMiddleName().matches("[a-zA-Z]{2,30}"))
            throw new IncorrectFieldOrRefusedException("Некорректно заполнено отчество", 1304);
        if (!loanApplicationRequest.getLastName().matches("[a-zA-Z]{2,30}"))
            throw new IncorrectFieldOrRefusedException("Некорректно заполнено фамилия", 1305);
        if (!loanApplicationRequest.getPassportNumber().matches("[0-9]{6}"))
            throw new IncorrectFieldOrRefusedException("Некорректно заполнен номер паспорта", 1306);
        if (!loanApplicationRequest.getPassportSeries().matches("[0-9]{4}"))
            throw new IncorrectFieldOrRefusedException("Некорректно заполнена серия паспорта", 1307);
        if (!loanApplicationRequest.getEmail().matches("[\\w\\.]{2,50}@[\\w\\.]{2,20}"))
            throw new IncorrectFieldOrRefusedException("Некорректно заполнен Email", 1308);
        if (calculatePersonAge(loanApplicationRequest.getBirthdate()) < 18)
            throw new IncorrectFieldOrRefusedException("Клиенту должно быть больше 18 лет", 1309);
    }

    private double scoring(ScoringDataDTO scoringData) {
        double newPersonalRate = mainRate;

        EmploymentDTO newEmployment = scoringData.getEmployment();
        int personAge = calculatePersonAge(scoringData.getBirthdate());
        EmploymentStatus employmentStatusEnum = newEmployment.getEmploymentStatus();
        MaritalStatus maritalStatusEnum = scoringData.getMaritalStatus();
        Gender genderEnum = scoringData.getGender();
        Position positionEnum = newEmployment.getPosition();

        if (newEmployment.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED)
            throw new IncorrectFieldOrRefusedException("Отказано. Безработный", 1301);
        if (personAge > 60) throw new IncorrectFieldOrRefusedException("Отказано. Старше 60", 1302);
        if (personAge < 20) throw new IncorrectFieldOrRefusedException("Отказано. Младше 20", 1303);
        if (newEmployment.getWorkExperienceTotal() < 12)
            throw new IncorrectFieldOrRefusedException("Отказано. Общий стаж менее 12 месяцев", 1304);
        if (newEmployment.getWorkExperienceCurrent() < 3)
            throw new IncorrectFieldOrRefusedException("Отказано. Текущий стаж менее 3 месяцев", 1305);
        if (scoringData.getAmount().compareTo(newEmployment.getSalary().multiply(BigDecimal.valueOf(20))) == 1)
            throw new IncorrectFieldOrRefusedException("Отказано. Сумма кредита боьше 20 зарплат клиента", 1306);

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

    private Integer calculatePersonAge(LocalDate birthday) {
        LocalDate nowDate = LocalDate.now();
        birthday = LocalDate.of(birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());
        nowDate = LocalDate.of(nowDate.getYear(), nowDate.getMonthValue(), nowDate.getDayOfMonth());
        LocalDate differenceDates = LocalDate.of(0, 1, 1).plusDays(ChronoUnit.DAYS.between(birthday, nowDate) - 1);

        return differenceDates.getYear();
    }

    private long createChiefAppId(LoanApplicationRequestDTO loanApplicationRequest) {
        String all_str_fields_request = loanApplicationRequest.getFirstName() + loanApplicationRequest.getMiddleName() + loanApplicationRequest.getLastName() + loanApplicationRequest.getEmail() + loanApplicationRequest.getPassportNumber() + loanApplicationRequest.getPassportSeries() + loanApplicationRequest.getBirthdate();
        long appId = (all_str_fields_request).chars().sum();
        appId += loanApplicationRequest.getTerm();
        appId += loanApplicationRequest.getAmount().intValue();
        return appId;
    }

    private double сalculateInsurance(double amount) {

        double insurance = amount / 4;
        int intAmount = (int) amount, amountForCount = (int) amount, countDigitalAmount = 0;
        while (amountForCount != 0) {
            amountForCount /= 10;
            countDigitalAmount++;
        }
        Integer firstDigitAmount = (int) (intAmount / Math.pow(10, (int) Math.log10(intAmount)));
        insurance += amount / (countDigitalAmount + firstDigitAmount);
        return Math.ceil(insurance * 100) / 100;
    }

    private BigDecimal calculateMonthlyPayment(double amount, Integer term, double rate) {
        double rateMonth = rate / 12 / 100;
        double ratioAnnuity = (rateMonth * Math.pow((1 + rateMonth), term)) / (Math.pow((1 + rateMonth), term) - 1);
        BigDecimal monthlyPayment = BigDecimal.valueOf(amount * ratioAnnuity);
        return monthlyPayment.setScale(2, ROUND_HALF_UP);
    }

    private List createListPaymentSchedule(double monthlyPayment, double amount, Integer term, double rate) {
        List<PaymentScheduleElementDTO> paymentSchedule = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate paymentDay = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        double bodyCredit, percentageAmount, rateInDigital = rate / 100;
        long yearDays, monthDays;

        for (int i = 1; i <= term; i++) {

            monthDays = ChronoUnit.DAYS.between(paymentDay, paymentDay.plusMonths(1));
            paymentDay = paymentDay.plusMonths(1);

            yearDays = paymentDay.lengthOfYear();
            percentageAmount = amount * rateInDigital * monthDays / yearDays;
            if (i == term) monthlyPayment = amount + percentageAmount;

            bodyCredit = monthlyPayment - percentageAmount;
            amount -= bodyCredit;

            paymentSchedule.add(new PaymentScheduleElementDTO().builder()
                    .number(BigDecimal.valueOf(i))
                    .date(paymentDay)
                    .totalPayment(BigDecimal.valueOf(monthlyPayment).setScale(2, ROUND_HALF_UP))
                    .interestPayment(BigDecimal.valueOf(percentageAmount).setScale(2, ROUND_HALF_UP))
                    .debtPayment(BigDecimal.valueOf(bodyCredit).setScale(2, ROUND_HALF_UP))
                    .remainingDebt(BigDecimal.valueOf(amount).setScale(2, ROUND_HALF_UP))
                    .build());
        }
        return paymentSchedule;
    }
}
