package test;

import java.math.BigDecimal;

/**
 * Created by dengrb1 on 11/24 0024.
 */

public class Var {

    public enum Type {BOOL, CURSOR, DATE, DECIMAL, DERIVED_TYPE, DERIVED_ROWTYPE, DOUBLE, FILE, IDENT, BIGINT, INTERVAL, ROW,
        RS_LOCATOR, STRING, STRINGLIST, TIMESTAMP, NULL};
    public static Var Empty = new Var();
    public static Var Null = new Var(Type.NULL);

    public String name;
    public Type type;
    public Object value;

    int len;

    boolean constant = false;

    public Var() {
        type = Type.NULL;
    }

    public Var(Var var) {
        name = var.name;
        type = var.type;
        value = var.value;
        len = var.len;
    }

    public Var(String n, Type t, Object v) {
        name = n;
        type = t;
        value = v;
    }

    public Var(Long value) {
        this.type = Type.BIGINT;
        this.value = value;
    }

    public Var(BigDecimal value) {
        this.type = Type.DECIMAL;
        this.value = value;
    }

    public Var(String name, Long value) {
        this.type = Type.BIGINT;
        this.name = name;
        this.value = value;
    }

    public Var(String value) {
        this.type = Type.STRING;
        this.value = value;
    }

    public Var(Boolean value) {
        this.type = Type.BOOL;
        this.value = value;
    }

    public Var(Type t) {
        type = t;
    }

    public void attach(Var v) {
        type = v.getType();
        value = v.getValue();
    }

    public int compare(Var v) {
        if (value == v.value) {
            return 0;
        }
        if (v == null) {
            return 1;
        }
        else if (type == Type.BIGINT && v.type == Type.BIGINT) {
            return ((Long)value).compareTo((Long)v.value);
        }
        else if (type == Type.STRING && v.type == Type.STRING) {
            return ((String)value).compareTo((String)v.value);
        }
        return -1;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isTrue() {
        if (type != Type.BOOL && value == null) {
            return false;
        } else {
            return ((Boolean)value).booleanValue();
        }
    }

    public String toString() {
        return "name: " + name + ", type: " + type.toString() + ", val: [" + value + "]";
    }

    public String toNameString() {
        return "";
    }

    public String toTypeString() {
        return type.toString();
    }

    public String toValString() {
        return "";
    }
}
