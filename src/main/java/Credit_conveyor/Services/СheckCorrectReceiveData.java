package Credit_conveyor.Services;

import Credit_conveyor.Exceptions.IncorrectFieldOrRefusedException;
import Credit_conveyor.model.LoanApplicationRequestDTO;
import Credit_conveyor.model.ScoringDataDTO;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;


public class СheckCorrectReceiveData {

    private static final String namePattern = "^[a-zA-Z]*$";
    private static final String emailPattern = "[\\w\\.]{2,50}@[\\w\\.]{2,20}";
    private static final String digitPattern = "^[0-9]*$";


    public static void CheckReceiveData(LoanApplicationRequestDTO request) {
        checkAmount(request.getAmount(), 1001);
        checkTerm(request.getTerm(), 6, 1002);
        checkName(request.getFirstName(), 29, 1003);
        checkName(request.getLastName(), 29, 1004);
        checkName(request.getMiddleName(), 29, 1005);
        checkEmail(request.getEmail(), 1006);

        if (checkDate(request.getBirthdate()) < 18)
            throw new IncorrectFieldOrRefusedException("Incorrect data entered", 1007);
        checkPassportINN(request.getPassportSeries(), 4, 1008);
        checkPassportINN(request.getPassportNumber(), 6, 1009);
    }

    public static Integer CheckReceiveData(ScoringDataDTO request) {
        checkAmount(request.getAmount(), 1101);
        checkTerm(request.getTerm(), 6, 1102);
        checkName(request.getFirstName(), 29, 1103);
        checkName(request.getLastName(), 29, 1104);
        checkName(request.getMiddleName(), 29, 1105);

        Integer age = checkDate(request.getBirthdate());
        if (age < 18) throw new IncorrectFieldOrRefusedException("Incorrect data entered", 1007);
        checkPassportINN(request.getPassportSeries(), 4, 1107);
        checkPassportINN(request.getPassportNumber(), 6, 1108);
        checkPassportINN(request.getEmployment().getEmployerINN(), 10, 1109);

        return age;
    }

    private static void checkAmount(BigDecimal val, int errorNumber) {
        if (val.compareTo(new BigDecimal(10000)) == -1)
            throw new IncorrectFieldOrRefusedException("Incorrect data entered", errorNumber);
    }

    private static void checkTerm(Integer val, int required_number, int errorNumber) {
        if (val < required_number)
            throw new IncorrectFieldOrRefusedException("Incorrect data entered", errorNumber);
    }

    private static void checkName(String val, int required_number, int errorNumber) {
        if (val.length() > required_number || !val.matches(namePattern))
            throw new IncorrectFieldOrRefusedException("Incorrect data entered", errorNumber);
    }

    private static void checkPassportINN(String val, int required_number, int errorNumber) {
        if (val.length() != required_number || !val.matches(digitPattern))
            throw new IncorrectFieldOrRefusedException("Incorrect data entered", errorNumber);
    }

    private static void checkEmail(String val, int errorNumber) {
        if (!val.matches(emailPattern))
            throw new IncorrectFieldOrRefusedException("Incorrect data entered", errorNumber);
    }

    private static Integer checkDate(LocalDate birthday) {
        LocalDate nowDate = LocalDate.now();
        birthday = LocalDate.of(birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());
        nowDate = LocalDate.of(nowDate.getYear(), nowDate.getMonthValue(), nowDate.getDayOfMonth());
        LocalDate differenceDates = LocalDate.of(0, 1, 1).plusDays(ChronoUnit.DAYS.between(birthday, nowDate) - 1);

        return differenceDates.getYear();
    }
}
