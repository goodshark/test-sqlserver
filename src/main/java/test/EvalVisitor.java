package test;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.Interval;

import java.util.HashMap;
import java.util.Stack;

/**
 * Created by dengrb1 on 11/17 0017.
 */

class SqlVar {
    private String key;
    private String varType;
    private String val;

    SqlVar() {
    }

    public void setKey(String k) {
        key = k;
    }

    public String getKey() {
        return key;
    }

    public void setType(String type) {
        varType = type;
        if (type == "int") {
            val = "0";
        }
    }

    public void setVal(String v) {
        val = v;
    }

    public String getVal() {
        if (varType == "varchar") {
            return "\"" + val + "\"";
        } else {
            return val;
        }
    }
}

public class EvalVisitor extends TsqlBaseVisitor<Integer>  {
    private StringBuilder sql = new StringBuilder();
    private HashMap<String, SqlVar> varMap = new HashMap<String, SqlVar>();

    private Stack<Var> stack = new Stack<Var>();
    private Stack<Scope> scopes = new Stack<Scope>();
    private Scope curScope = null;

    private Query query = new Query();

    public String getSql() {
        return sql.toString();
    }

    public void print(String str) {
        System.out.println(str);
    }
    public void testMap() {
        for (String key: varMap.keySet()) {
            System.out.println(varMap.get(key).getKey() + "-> " + varMap.get(key).getVal());
        }
    }

    public void debugSql() {
        for (Scope sc: scopes) {
            print(sc.toString());
            for (Var v: sc.vars) {
                print("Var -- " + v.toString());
            }
        }
        query.execute(sql.toString());
    }

    public void enterGlobalScope() {
        curScope = new Scope(Scope.Type.GLOBAL);
        enterScope(curScope);
    }

    public void leaveGlobalScope() {}

    public void enterScope(Scope scope) {
        scopes.push(scope);
    }

    public void enterScope(Scope.Type type) {
        curScope = new Scope(curScope, type);
        enterScope(curScope);
    }

    public void leaveSocpe() {
        if (!scopes.empty()) {
            scopes.pop();
        }
    }

    public Var evalSubTree(ParseTree tree) {
        visit(tree);
        if (stack.empty()) {
            return new Var();
        }
        return stack.pop();
    }

    public void run(ParseTree tree) {
        enterGlobalScope();
        visit(tree);
        leaveGlobalScope();
    }

    public Var findVariable(String name) {
        Var var = null;
        Scope cur = curScope;
        while (cur != null) {
            var = cur.findVariable(name);
            if (var == null) {
                cur = cur.parent;
            } else {
                return var;
            }
        }
        return var;
    }

    public void setVariable(String name, Var val) {
        Var v = findVariable(name);
        if (v != null) {
            v.attach(val);
        }
    }

    @Override
    public Integer visitBlock_statement(TsqlParser.Block_statementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Integer visitWhile_statement(TsqlParser.While_statementContext ctx) {
        print("visit while stmt");
        while (true) {
            if (evalSubTree(ctx.search_condition()).isTrue()) {
                enterScope(Scope.Type.LOOP);
                visit(ctx.sql_clause());
                leaveSocpe();
            } else {
                print("leave while stmt");
                break;
            }
        }
        return 0;
    }

    public Var operate(String op, Var v1, Var v2) {
        Var res = new Var(false);
        int tmpRes = v1.compare(v2);
        if (tmpRes == 0 && op.equalsIgnoreCase("=")) {
                res.attach(new Var(true));
        } else if (tmpRes > 0 && op.equalsIgnoreCase(">")) {
                res.attach(new Var(true));
        } else if (tmpRes < 0 && op.equalsIgnoreCase("<")) {
                res.attach(new Var(true));
        }
        return res;
    }

    @Override
    public Integer visitPredicate(TsqlParser.PredicateContext ctx) {
        String op = null;
        Var v1 = null;
        Var v2 = null;
        // TODO implement more operator, only implement "x op y"
        if (ctx.comparison_operator() != null && ctx.getChildCount() == 3) {
            op = ctx.comparison_operator().getText();
            v1 = evalSubTree(ctx.expression(0));
            v2 = evalSubTree(ctx.expression(1));
        }
        if (op != null && v1 != null && v2 != null) {
            stack.push(operate(op, v1, v2));
        }
        // no need to visit subTree
        // return visitChildren(ctx);
        return 0;
    }

    @Override
    public Integer visitPrint_statement(TsqlParser.Print_statementContext ctx) {
        return visitChildren(ctx);
    }


    public String subsitute() {
        String sql = getSql();
        for (String key : varMap.keySet()) {
            if (sql.contains(key)) {
                sql = sql.replaceAll(key, varMap.get(key).getVal());
            }
        }
        return sql;
    }

    public String getFormatText(ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(
                new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }

    @Override
    public Integer visitPrimitive_expression(TsqlParser.Primitive_expressionContext ctx) {
        // find variable like @a, b
        if (ctx.LOCAL_ID() != null) {
            Var v = findVariable(ctx.LOCAL_ID().getText());
            stack.push(v);
        }
        // get constant like 12, "foo"
        if (ctx.constant() != null) {
            if (ctx.constant().STRING() != null)
                stack.push(new Var(ctx.constant().STRING().getText()));
            if (ctx.constant().DECIMAL() != null)
                stack.push(new Var(Long.parseLong(ctx.constant().DECIMAL().getText())));
        }
        return 0;
    }

    @Override
    public Integer visitBinary_operator_expression(TsqlParser.Binary_operator_expressionContext ctx) {
        String op = ctx.op.getText();
        Var v1 = evalSubTree(ctx.expression(0));
        Var v2 = evalSubTree(ctx.expression(1));
        stack.push(new Var(binaryOp(op, v1, v2)));
        return 0;
    }

    public Long binaryOp(String op, Var v1, Var v2) {
        if (op.equalsIgnoreCase("+")) {
            return (Long)v1.getValue() + (Long)v2.getValue();
        } else if (op.equalsIgnoreCase("-")) {
            return (Long)v1.getValue() - (Long)v2.getValue();
        } else if (op.equalsIgnoreCase("*")) {
            return (Long)v1.getValue() * (Long)v2.getValue();
        } else if (op.equalsIgnoreCase("/")) {
            return (Long)v1.getValue() / (Long)v2.getValue();
        } else if (op.equalsIgnoreCase("%")) {
            return (Long)v1.getValue() % (Long)v2.getValue();
        }
        return 0L;
    }

    /*@Override
    public Integer visitConstant(TsqlParser.ConstantContext ctx) {
        if (ctx.STRING() != null) {
            stack.push(new Var(ctx.STRING().getText()));
        }
        if (ctx.DECIMAL() != null) {
            stack.push(new Var(Long.parseLong(ctx.DECIMAL().getText())));
        }
        return 0;
    }*/

    /*@Override
    public Integer visitDeclare_statement(TsqlParser.Declare_statementContext ctx) {
        return visitChildren(ctx);
    }*/

    // decalre var
    /*@Override
    public Integer visitDeclare_local(TsqlParser.Declare_localContext ctx) {
        SqlVar var = new SqlVar();
        String key = ctx.start.getText();
        String type = ctx.data_type().getText();
        var.setKey(key);
        var.setType(type);
        varMap.put(key, var);
        return visitChildren(ctx);
    }*/

    @Override
    public Integer visitDeclare_local(TsqlParser.Declare_localContext ctx) {
        // get variable
        String key = ctx.start.getText();
        Var.Type type = null;
        // TODO modify g4 let data_type equal real types
        // Var.Type type = evalSubTree(ctx.data_type()).getType();
        String typeStr = ctx.data_type().getText();
        if (typeStr.equalsIgnoreCase("string")) {
            type = Var.Type.STRING;
        } else if (typeStr.equalsIgnoreCase("int")) {
            type = Var.Type.BIGINT;
        }
        Object val = null;
        if (ctx.expression() != null) {
            // TODO declare assigment
            val = evalSubTree(ctx.expression());
        }
        Var var = new Var(key, type, val);
        curScope.addVariable(var);
        // testonly
        curScope.debugVars();
        // no need to visit subTree again
        return 0;
        // return visitChildren(ctx);
    }

    @Override
    public Integer visitSimple_id(TsqlParser.Simple_idContext ctx) {
        String id = getFormatText(ctx);
        Var var = new Var(id);
        stack.push(var);
        // no need to visit subTree again
        // return visitChildren(ctx);
        return 0;
    }

    @Override
    public Integer visitSet_statement(TsqlParser.Set_statementContext ctx) {
        String name = null;
        Var val = null;
        if (ctx.LOCAL_ID() != null) {
            name = ctx.LOCAL_ID().getText();
            // TODO implement assignment_operator
            if (ctx.expression() != null) {
                val = evalSubTree(ctx.expression());
            }
        }
        if (name != null) {
            setVariable(name, val);
        }
        // no need to visit subTree again
        // return visitChildren(ctx);
        return 0;
    }


    /*@Override
    public Integer visitSet_statement(TsqlParser.Set_statementContext ctx) {
        if (ctx.LOCAL_ID() != null) {
            String key = ctx.LOCAL_ID().getText();
            if (varMap.containsKey(key)) {
                String val = ctx.expression().getText();
                varMap.get(key).setVal(val);
            }
        }
        return visitChildren(ctx);
    }*/


    @Override
    public Integer visitSelect_statement(TsqlParser.Select_statementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Integer visitDml_clause(TsqlParser.Dml_clauseContext ctx) {
        if (ctx.delete_statement() != null) {
            System.out.println("del stmt");
        }
        if (ctx.select_statement() != null) {
            System.out.println("select stmt: " + ctx.select_statement());
            System.out.println("select child cnt: " + ctx.select_statement().getChildCount());
        }
        if (ctx.insert_statement() != null) {
            System.out.println("insert stmt");
        }
        if (ctx.update_statement() != null) {
            System.out.println("update stmt");
        }
        return visitChildren(ctx);
    }

    @Override
    public Integer visitQuery_specification(TsqlParser.Query_specificationContext ctx) {
        // select
        if (sql.capacity() != 0) {
            sql.append("\n");
        }
        sql.append(ctx.start.getText());
        sql.append(" ");
        return visitChildren(ctx);
    }

    @Override
    public Integer visitSelect_list(TsqlParser.Select_listContext ctx) {
        sql.append(ctx.getText());
        sql.append(" ");
        return visitChildren(ctx);
    }

    @Override
    public Integer visitTable_sources(TsqlParser.Table_sourcesContext ctx) {
        sql.append("from ");
        sql.append(ctx.getText());
        sql.append(" ");
        return visitChildren(ctx);
    }

    /*@Override
    public Integer visitSearch_condition(TsqlParser.Search_conditionContext ctx) {
        sql.append("where ");
        sql.append(ctx.getText());
        return visitChildren(ctx);
    }*/
}
