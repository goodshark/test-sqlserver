package test;

import java.util.HashMap;

/**
 * Created by dengrb1 on 11/18 0018.
 */
public class EvalListener extends TsqlBaseListener {
    private StringBuilder sql = new StringBuilder();
    private HashMap<String, String> sqlVar = new HashMap<String, String>();

    public String getSql() {
        return sql.toString();
    }

    @Override
    public void enterQuery_specification(TsqlParser.Query_specificationContext ctx) {
        sql.append(ctx.start.getText());
        sql.append(" ");
    }

    @Override
    public void enterSelect_statement(TsqlParser.Select_statementContext ctx) {
        System.out.println("enter select statement");
    }

    @Override
    public void enterDeclare_statement(TsqlParser.Declare_statementContext ctx) {
        System.out.println("enter decalre statement: " + ctx.getText());
    }

    public void enterSet_statement(TsqlParser.Set_statementContext ctx) {
        System.out.println("enter set statement: " + ctx.getText());
    }
}
