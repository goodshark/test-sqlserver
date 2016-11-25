package test;

import java.util.ArrayList;

/**
 * Created by dengrb1 on 11/24 0024.
 */

public class Scope {
    public enum Type { GLOBAL, BEGIN_END, LOOP };

    ArrayList<Var> vars = new ArrayList<Var>();
    Scope parent;
    Type type;

    Scope(Type type) {
        this.parent = null;
        this.type = type;
    }

    Scope(Scope parent, Type type) {
        this.parent = parent;
        this.type = type;
    }

    // testonly
    public void debugVars() {
        for (Var v: vars) {
            System.out.println(v);
        }
    }

    public void addVariable(Var v) {
        vars.add(v);
    }

    public Var findVariable(String name) {
        for (Var v: vars) {
            if (v.getName().equalsIgnoreCase(name)) {
                return v;
            }
        }
        return null;
    }

    public String toString() {
        return "scope <" + type.toString() + ">";
    }
}
