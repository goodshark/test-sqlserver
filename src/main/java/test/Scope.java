package test;

import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dengrb1 on 11/24 0024.
 */

public class Scope {
    public enum Type { GLOBAL, BEGIN_END, LOOP };

    ArrayList<Var> vars = new ArrayList<Var>();
    HashMap<String, ParseTree> labels = new HashMap<String, ParseTree>();
    Scope parent;
    Type type;

    // try-mark
    boolean tryFlag = false;

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

    public void addGotoLabel(String label, ParseTree ctx) {
        if (!labels.containsKey(label)) {
            labels.put(label, ctx);
        }
    }

    public ParseTree findLabel(String label) {
        return labels.get(label);
    }

    public void setTryFlag(boolean flag) {
        tryFlag = flag;
    }

    public boolean getTryFlag() {
        return tryFlag;
    }

    public String toString() {
        return "scope <" + type.toString() + ">";
    }
}
