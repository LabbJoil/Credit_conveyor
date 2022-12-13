package Credit_conveyor.Exceptions;

public class IncorrectFieldOrRefusedException extends RuntimeException {
    private int numberError;

    public int getNumberError() {
        return numberError;
    }

    public IncorrectFieldOrRefusedException(String message, int num) {
        super(message);
        numberError = num;
    }

}
