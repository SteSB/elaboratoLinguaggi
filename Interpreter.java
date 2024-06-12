import java.util.Map;
import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.function.Function;


public class Interpreter extends GrammarBaseVisitor<Object> {

    private final Map<String, FunctionInfo> functions = new HashMap<>();    // memorizzo ogni funzione
    private final Deque<FunctionInfo> callStack = new ArrayDeque<>();       // memorizzo l'ordine di chiamata per sapere quale funzione è in esecuzione
    private Object returnValue = null;                                      // valore di ritorno delle funzioni

    private static class FunctionInfo {         // per ogni funzione salvo il nome, le variabili, il corpo (da eseguire in un secondo momento) e gli eventuali parametri
        String name;
        Map<String, Object> variables;
        GrammarParser.StatementsContext functionBody;
        List<String> parameters;

        FunctionInfo(String name, GrammarParser.StatementsContext functionBody, List<String> parameters) {
            this.name = name;
            this.variables = new HashMap<>();
            this.functionBody = functionBody;
            this.parameters = parameters;
        }
    }

    @Override
    public Object visitMain_function(GrammarParser.Main_functionContext ctx) {
        FunctionInfo mainFunction = new FunctionInfo("main", ctx.statements(), new ArrayList<>());
        functions.put("main", mainFunction);

        callStack.push(mainFunction);

        visitChildren(ctx.statements());            // il main viene subito eseguito

        callStack.pop();

        return null;
    }

    // una funzione viene semplicemente memorizzata, con o senza parametri a seconda della tipologia di funzione
    /*--------------------------------------------------*/
    @Override
    public Object visitVoidfunction(GrammarParser.VoidfunctionContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        FunctionInfo function = new FunctionInfo(id, ctx.statements(), new ArrayList<>());
        functions.put(id, function);
        return null;
    }

    @Override
    public Object visitNovoidfunction(GrammarParser.NovoidfunctionContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        List<String> parameters = ctx.arg().stream().map(arg -> arg.IDENTIFIER().getText()).collect(Collectors.toList());
        FunctionInfo function = new FunctionInfo(id, ctx.statements(), parameters);
        functions.put(id, function);
        return null;
    }
    /*--------------------------------------------------*/

    @Override
    public Object visitCall_function(GrammarParser.Call_functionContext ctx) {
        String id = ctx.IDENTIFIER().getText();

        if (functions.containsKey(id)) {        // controllo che la funzione sia stata dichiarata
            FunctionInfo function = functions.get(id);      // la recupero
            FunctionInfo newContext = new FunctionInfo(function.name, function.functionBody, function.parameters);
            callStack.push(newContext);         // aggiungo la funzione allo stack

            List<GrammarParser.ExpressionContext> args = ctx.expression();      // controllo che ci siano tutti i parametri richiesti dalla dichiarazione
            if (args.size() != function.parameters.size()) {
                throw new RuntimeException("Numero di argomenti non corrisponde per la funzione " + id);
            }

            for (int i = 0; i < args.size(); i++) {     // memorizzo ogni parametro come una variabile locale (copia dei valori)
                String paramName = function.parameters.get(i);
                Object value = visit(args.get(i));
                newContext.variables.put(paramName, value);
            }

            visit(newContext.functionBody);         // eseguo la funzione

            callStack.pop();

            Object ret = returnValue;
            returnValue = null;         // Resetta il valore di ritorno per la prossima chiamata di funzione
            return ret;
        } else {
            throw new RuntimeException("Errore nella chiamata alla funzione " + id);
        }
    }

    @Override
    public Object visitReturn_function(GrammarParser.Return_functionContext ctx) {
        String id = ctx.IDENTIFIER().getText();

        if (!callStack.isEmpty() && callStack.peek().variables.containsKey(id)) {       // recupero il valore da ritornare nello stack e lo salvo in una variaible globale (returnValue)
            returnValue = callStack.peek().variables.get(id);
        } else {
            throw new RuntimeException("Variable " + id + " not defined.");
        }

        return returnValue;
    }

    @Override
    public Object visitPrint_sconst_stmt(GrammarParser.Print_sconst_stmtContext ctx) {      // stampa di una stringa
        String text = ctx.STRING().getText();
        if ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("'") && text.endsWith("'"))) {       // elimino le ""
            text = text.substring(1, text.length() - 1);
        }
        System.out.println(text);
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrint_var_stmt(GrammarParser.Print_var_stmtContext ctx) {    // stampa variabili
        String id = ctx.getChild(1).getText();
        if (!callStack.isEmpty() && callStack.peek().variables.containsKey(id)) {   // recupero il valore dall'ambiente
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
        if(!callStack.isEmpty())
            callStack.peek().variables.put(id, value);      // creo una nuova variaible locale (aggiungo all'ambiente)
        return null;
    }

    @Override
    public Object visitVar_assign_stmt(GrammarParser.Var_assign_stmtContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        int value = (Integer) visit(ctx.expression());
        int v = applyOperations(value, ctx.operations());
        if(!callStack.isEmpty())
            callStack.peek().variables.put(id, v);      // modifico il valore di una variabile
        return null;
    }

    @Override
    public Object visitVar_assign_from_func_stmt(GrammarParser.Var_assign_from_func_stmtContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        int value = (Integer) visit(ctx.call_function());
        if(!callStack.isEmpty())
            callStack.peek().variables.put(id, value);          // ottengo il valore da una funzione e lo salvo nella variaibile
        return null;
    }

    private int applyOperations(int initialValue, GrammarParser.OperationsContext ctx) {
        Object result = visit(ctx);
        if (result instanceof Function) {
            @SuppressWarnings("unchecked")
            Function<Integer, Integer> operation = (Function<Integer, Integer>) result;
            return operation.apply(initialValue);
        } else {
            throw new RuntimeException("Expected a function operation, but got: " + result.getClass().getName());
        }
    }

    @Override
    public Object visitVarexpr(GrammarParser.VarexprContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        for (FunctionInfo elm : callStack)          // ottengo il valore di una variabile
            if(elm.variables.containsKey(id))       // ciclo tutte le variaibili dell'ambiente finchè non trovo quella con l'id corretto
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
