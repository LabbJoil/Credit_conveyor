package Credit_conveyor.Services;

import java.math.BigDecimal;

public class MonthlyPaymentAndInsurance {
    private Integer insurance = 450;

    public BigDecimal calculateAmountWithInsurance(BigDecimal amount) {
        int intAmount = amount.intValue(), amountForCount = amount.intValue(), countDigitalAmount = 0, minAmountLength = 5;
        while (amountForCount != 0) {
            amountForCount /= 10;
            countDigitalAmount++;
        }
        Integer firstDigitAmount = (int) (intAmount / Math.pow(10, (int) Math.log10(intAmount) - 1 - (countDigitalAmount - minAmountLength)));
        BigDecimal newInsurance = new BigDecimal(insurance * firstDigitAmount);
        return newInsurance;
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, double rate) {
        double rateMonth = rate / 12 / 100;
        double ratioAnnuity = (rateMonth * Math.pow((1 + rateMonth), term)) / (Math.pow((1 + rateMonth), term) - 1);
        BigDecimal monthlyPayment = amount.multiply(new BigDecimal(ratioAnnuity));
        return monthlyPayment.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
