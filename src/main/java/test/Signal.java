package test;

/**
 * Created by dengrb1 on 11/28 0028.
 */

public class Signal {
    public enum Type { LEAVE_LOOP, CONTINUE, GOTO, LEAVE_ROUTINE, LEAVE_PROGRAM, SQLEXCEPTION, STOPEXEPTION, NOTFOUND, UNSUPPORTED_OPERATION, USERDEFINED };
    private Type type;
    private String value = "";
    private Exception exception = null;

    Signal(Type type, String value) {
        this.type = type;
        this.value = value;
        this.exception = null;
    }

    Signal(Type type, String value, Exception exception) {
        this.type = type;
        this.value = value;
        this.exception = exception;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}
