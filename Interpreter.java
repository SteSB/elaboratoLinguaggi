import java.util.Map;
import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.function.Function;


public class Interpreter extends GrammarBaseVisitor<Object> {

    private Map<String, FunctionInfo> functions = new HashMap<>();
    private Deque<FunctionInfo> callStack = new ArrayDeque<>();
    private Object returnValue = null;

    private static class FunctionInfo {
        String name;
        Map<String, Object> variables;
        int staticDepth;
        GrammarParser.StatementsContext functionBody;
        List<String> parameters;

        FunctionInfo(String name, int staticDepth, GrammarParser.StatementsContext functionBody, List<String> parameters) {
            this.name = name;
            this.variables = new HashMap<>();
            this.staticDepth = staticDepth;
            this.functionBody = functionBody;
            this.parameters = parameters;
        }
    }

    @Override
    public Object visitProgram(GrammarParser.ProgramContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitMain_function(GrammarParser.Main_functionContext ctx) {
        FunctionInfo mainFunction = new FunctionInfo("main", 0, ctx.statements(), new ArrayList<>());
        functions.put("main", mainFunction);

        callStack.push(mainFunction);

        visitChildren(ctx.statements());

        callStack.pop();

        return null;
    }

    @Override
    public Object visitVoidfunction(GrammarParser.VoidfunctionContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        int staticDepth = callStack.isEmpty() ? 0 : callStack.peek().staticDepth + 1;
        FunctionInfo function = new FunctionInfo(id, staticDepth, ctx.statements(), new ArrayList<>());
        functions.put(id, function);
        return null;
    }

    @Override
    public Object visitNovoidfunction(GrammarParser.NovoidfunctionContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        int staticDepth = callStack.isEmpty() ? 0 : callStack.peek().staticDepth + 1;
        List<String> parameters = ctx.arg().stream().map(arg -> arg.IDENTIFIER().getText()).collect(Collectors.toList());
        FunctionInfo function = new FunctionInfo(id, staticDepth, ctx.statements(), parameters);
        functions.put(id, function);
        return null;
    }

    @Override
    public Object visitCall_function(GrammarParser.Call_functionContext ctx) {
        String id = ctx.IDENTIFIER().getText();

        if (functions.containsKey(id)) {
            FunctionInfo function = functions.get(id);
            FunctionInfo newContext = new FunctionInfo(function.name, function.staticDepth + 1, function.functionBody, function.parameters);
            callStack.push(newContext);

            List<GrammarParser.ExpressionContext> args = ctx.expression();
            if (args.size() != function.parameters.size()) {
                throw new RuntimeException("Numero di argomenti non corrisponde per la funzione " + id);
            }

            for (int i = 0; i < args.size(); i++) {
                String paramName = function.parameters.get(i);
                Object value = visit(args.get(i));
                newContext.variables.put(paramName, value);
            }

            // Esegui il corpo della funzione
            visit(newContext.functionBody);

            callStack.pop();

            // Restituisci il valore di ritorno
            Object ret = returnValue;
            returnValue = null; // Resetta il valore di ritorno per la prossima chiamata di funzione
            return ret;
        } else {
            throw new RuntimeException("Errore nella chiamata alla funzione " + id);
        }
    }

    @Override
    public Object visitReturn_function(GrammarParser.Return_functionContext ctx) {
        String id = ctx.IDENTIFIER().getText();

        if (!callStack.isEmpty() && callStack.peek().variables.containsKey(id)) {
            returnValue = callStack.peek().variables.get(id);
        } else {
            throw new RuntimeException("Variable " + id + " not defined.");
        }

        return returnValue;
    }

    @Override
    public Object visitStatements(GrammarParser.StatementsContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitStatement(GrammarParser.StatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrint_stmt(GrammarParser.Print_stmtContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrint_sconst_stmt(GrammarParser.Print_sconst_stmtContext ctx) {
        String text = ctx.STRING().getText();
        if ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("'") && text.endsWith("'"))) {
            text = text.substring(1, text.length() - 1);
        }
        System.out.println(text);
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrint_var_stmt(GrammarParser.Print_var_stmtContext ctx) {
        String id = ctx.getChild(1).getText();
        if (callStack.peek().variables.containsKey(id)) {
            System.out.println(callStack.peek().variables.get(id));
        } else {
            throw new RuntimeException("Variable " + id + " not defined.");
        }
        return visitChildren(ctx);
    }

    @Override
    public Object visitVar_decl_stmt(GrammarParser.Var_decl_stmtContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        int value = (Integer) visit(ctx.expression());
        callStack.peek().variables.put(id, value);
        return null;
    }

    @Override
    public Object visitVar_assign_stmt(GrammarParser.Var_assign_stmtContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        int value = (Integer) visit(ctx.expression());
        value = applyOperations(value, ctx.operations());
        callStack.peek().variables.put(id, value);
        return null;
    }

    @Override
    public Object visitVar_assign_from_func_stmt(GrammarParser.Var_assign_from_func_stmtContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        int value = (Integer) visit(ctx.call_function());
        callStack.peek().variables.put(id, value);
        return null;
    }

    private int applyOperations(int initialValue, GrammarParser.OperationsContext ctx) {
        Function<Integer, Integer> operation = (Function<Integer, Integer>) visit(ctx);
        return operation.apply(initialValue);
    }

    @Override
    public Object visitVarexpr(GrammarParser.VarexprContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        for (FunctionInfo elm : callStack)
            if(elm.variables.containsKey(id))
                return elm.variables.get(id);
        throw new RuntimeException("Variable " + id + " not defined.");
    }

    @Override
    public Object visitNumberexpr(GrammarParser.NumberexprContext ctx) {
        return Integer.parseInt(ctx.NUMBER().getText());
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
    public Object visitIfelseendif(GrammarParser.IfelseendifContext ctx) {
        boolean condition = (Integer) visit(ctx.expression()) != 0;
        if (condition) {
            visit(ctx.statements(0));
        } else {
            visit(ctx.statements(1));
        }
        return null;
    }

    @Override
    public Object visitIfendid(GrammarParser.IfendidContext ctx) {
        boolean condition = (Integer) visit(ctx.expression()) != 0;
        if (condition) {
            visit(ctx.statements());
        }
        return null;
    }

    @Override
    public Object visitWhile_stmt(GrammarParser.While_stmtContext ctx) {
        while ((Integer) visit(ctx.expression()) != 0) {
            visit(ctx.statements());
        }
        return null;
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
        return (Function<Integer, Integer>) (x -> (x > var) ? 1 : 0);
    }

    @Override
    public Object visitAndop(GrammarParser.AndopContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x != 0 && var != 0 ? 1 : 0);
    }

    @Override
    public Object visitOrop(GrammarParser.OropContext ctx) {
        int var = (Integer) visit(ctx.getChild(1));
        return (Function<Integer, Integer>) (x -> x != 0 || var != 0 ? 1 : 0);
    }

}
