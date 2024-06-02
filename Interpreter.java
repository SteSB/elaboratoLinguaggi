import value.ExpValue;
import value.StringValue;
import value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Interpreter extends GrammarBaseVisitor<Object> {

    private Map<String, Object> mem = new HashMap<>();

    @Override
    public Object visitProgram(GrammarParser.ProgramContext ctx) {
        //System.out.println("program");
        return visitChildren(ctx);
    }

    @Override
    public Object visitMain_function(GrammarParser.Main_functionContext ctx) {
        //System.out.println("main_function");
        return visitChildren(ctx);
    }

    @Override
    public Object visitStatements(GrammarParser.StatementsContext ctx) {
        //System.out.println("statements");
        return visitChildren(ctx);
    }

    @Override
    public Object visitStatement(GrammarParser.StatementContext ctx) {
        //System.out.println("statement");
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrint_stmt(GrammarParser.Print_stmtContext ctx) {
        //System.out.println("print_stmt");
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrint_sconst_stmt(GrammarParser.Print_sconst_stmtContext ctx) {
        //System.out.println("print_sconst_stmt");
        String text = ctx.getChild(1).getText();
        System.out.println(text);
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrint_var_stmt(GrammarParser.Print_var_stmtContext ctx) {
        //System.out.println("print_var_stmt");
        System.out.println(mem.get(ctx.getChild(1).getText()));
        return visitChildren(ctx);
    }

    @Override
    public Object visitVar_decl_stmt(GrammarParser.Var_decl_stmtContext ctx) {
        //System.out.println("var_decl_stmt");
        Object var = visit(ctx.getChild(3));
        mem.put(ctx.getChild(1).getText(), var);
        return visitChildren(ctx);
    }

    @Override
    public Object visitVar_assign_stmt(GrammarParser.Var_assign_stmtContext ctx) {
        String key = ctx.getChild(1).getText();
        Object value = visit(ctx.getChild(3));
        Function<Integer, Integer> func = (Function<Integer, Integer>) visit(ctx.getChild(4));
        mem.put(key, func.apply((Integer) value));
        //System.out.println("var_assign_stmt -> " + key + " = " + func.apply((Integer) value));
        return visitChildren(ctx);
    }

    @Override
    public Object visitIfelseendif(GrammarParser.IfelseendifContext ctx) {
        //System.out.println("ifElseEndif");
        if ((Integer) visit(ctx.getChild(1)) == 1) {
            return visit(ctx.getChild(2));
        } else {
            return visit(ctx.getChild(4));
        }
    }

    @Override
    public Object visitIfendid(GrammarParser.IfendidContext ctx) {
        //System.out.println("ifEndif");
        if ((Integer) visit(ctx.getChild(1)) == 1) {
            return visit(ctx.getChild(2));
        }
        return null;
    }

    @Override
    public Object visitWhile_stmt(GrammarParser.While_stmtContext ctx) {
        //System.out.println("while_stmt");
        while ((Integer) visit(ctx.getChild(1)) == 1) {
            visit(ctx.getChild(2));
        }
        return null;
    }

    @Override
    public Object visitVarexpr(GrammarParser.VarexprContext ctx) {
        return mem.get(ctx.getChild(0).getText());
    }

    @Override
    public Object visitNumberexpr(GrammarParser.NumberexprContext ctx) {
        return Integer.parseInt(ctx.getChild(0).getText());
    }

    @Override
    public Object visitTrueexpr(GrammarParser.TrueexprContext ctx) {
        return 1;
    }

    @Override
    public Object visitFalseexpr(GrammarParser.FalseexprContext ctx) {
        return 0;
    }

    @Override
    public Object visitOperations(GrammarParser.OperationsContext ctx) {
        if (ctx.getChildCount() > 1) {
            Function<Integer, Integer> func = (Function<Integer, Integer>) visit(ctx.getChild(0));
            Function<Integer, Integer> op = (Function<Integer, Integer>) visit(ctx.getChild(1));
            return (Function<Integer, Integer>) (x -> op.apply(func.apply(x)));
        } else {
            return visit(ctx.getChild(0));
        }
    }

    @Override
    public Object visitPlusop(GrammarParser.PlusopContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x + var);
    }

    @Override
    public Object visitMinusop(GrammarParser.MinusopContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x - var);
    }

    @Override
    public Object visitMultiplicationop(GrammarParser.MultiplicationopContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x * var);
    }

    @Override
    public Object visitDivisionop(GrammarParser.DivisionopContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x / var);
    }

    @Override
    public Object visitEqualop(GrammarParser.EqualopContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x == var ? 1 : 0);
    }

    @Override
    public Object visitGreaterop(GrammarParser.GreateropContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x > var ? 1 : 0);
    }

    @Override
    public Object visitOrop(GrammarParser.OropContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x == 0 && var == 0 ? 0 : 1);
    }

    @Override
    public Object visitAndop(GrammarParser.AndopContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x != 0 && var != 0 ? 1 : 0);
    }

    /*private final Conf conf;

    public Interpreter(Conf conf) {
        this.conf = conf;
    }

    @Override
    public Value visitProgram(GrammarParser.ProgramContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Value visitMain_function(GrammarParser.Main_functionContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Value visitStatements(GrammarParser.StatementsContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Value visitStatement(GrammarParser.StatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Value visitPrint_stmt(GrammarParser.Print_stmtContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Value visitPrint_sconst_stmt(GrammarParser.Print_sconst_stmtContext ctx) {
        System.out.println(ctx.STRING().getText());
        return visitChildren(ctx);
    }

    @Override
    public Value visitPrint_var_stmt(GrammarParser.Print_var_stmtContext ctx) {
        System.out.println(conf.get(ctx.IDENTIFIER().getText()));
        return visitChildren(ctx);
    }

    @Override
    public Value visitVar_decl_stmt(GrammarParser.Var_decl_stmtContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        Value v = visit(ctx.expression());

        conf.update(id, v);

        return visitChildren(ctx);
    }

    @Override
    public Value visitVar_assign_stmt(GrammarParser.Var_assign_stmtContext ctx) {

    }

    @Override
    public Value visitIfelseendif(GrammarParser.IfelseendifContext ctx) {

    }

    @Override
    public Value visitIfendid(GrammarParser.IfendidContext ctx) {

    }

    @Override
    public Value visitWhile_stmt(GrammarParser.While_stmtContext ctx) {
        while(visit(ctx.getChild(1)) == 1)
    }*/
}
