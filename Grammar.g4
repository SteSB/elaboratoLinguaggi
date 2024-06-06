grammar Grammar;

program                     : (function)*  main_function (function)*
                            ;

main_function               : BEGINMAIN ENDMAIN
                            | BEGINMAIN statements ENDMAIN
                            ;

function                    : BEGINFUNCTION IDENTIFIER statements ENDFUNCTION   #voidfunction
                            | BEGINFUNCTION IDENTIFIER (arg)* ENDARGS statements ENDFUNCTION #novoidfunction
                            ;

arg                         : ARG IDENTIFIER
                            ;

statements                  : statement*
                            ;

statement                   : var_decl_stmt
                            | var_assign_stmt
                            | print_stmt
                            | condition_stmt
                            | while_stmt
                            | var_assign_from_func_stmt
                            | call_function
                            | return_function
                            ;

var_assign_from_func_stmt   : VARASSIGNFROMFUN IDENTIFIER call_function
                            ;

call_function               : CALLFUNC IDENTIFIER (expression)*
                            ;

return_function             : RETURN IDENTIFIER
                            ;

print_stmt                  : print_var_stmt
                            | print_sconst_stmt
                            ;

print_sconst_stmt           : PRINT STRING
                            ;

print_var_stmt              : PRINT IDENTIFIER
                            ;

var_decl_stmt               : DECLAREINT IDENTIFIER SETINITIALVALUE expression
                            ;

condition_stmt              : IF expression statements ELSE statements ENDIF #ifelseendif
                            | IF expression statements ENDIF                 #ifendid
                            ;

while_stmt                  : WHILE expression statements ENDWHILE
                            ;

expression                  : IDENTIFIER #varexpr
                            | NUMBER     #numberexpr
                            | AT TRUE    #trueexpr
                            | AT FALSE   #falseexpr
                            ;

var_assign_stmt             : ASSIGNVARIABLE IDENTIFIER SETVALUE expression operations ENDASSIGNVARIABLE
                            ;

operations                  : operation | operations operation
                            ;

operation                   : PLUSOPERATOR expression           #plusop
                            | MINUSOPERATOR expression          #minusop
                            | MULTIPLICATIONOPERATOR expression #multiplicationop
                            | DIVISIONOPERATOR expression       #divisionop
                            | EQUALTO expression                #equalop
                            | GREATERTHAN expression            #greaterop
                            | OR expression                     #orop
                            | AND expression                    #andop
                            ;

BEGINMAIN                   : 'IT\'S SHOWTIME' ;
ENDMAIN                     : 'YOU HAVE BEEN TERMINATED' ;
BEGINFUNCTION               : 'LISTEN TO ME VERY CAREFULLY' ;
ENDFUNCTION                 : 'HASTA LA VISTA, BABY' ;
VARASSIGNFROMFUN            : 'GET YOUR ASS TO MARS' ;
CALLFUNC                    : 'DO IT NOW' ;
ARG                         : 'I NEED YOUR CLOTHES YOUR BOOTS AND YOUR MOTORCYCLE' ;
ENDARGS                     : 'GIVE THESE PEOPLE AIR' ;
RETURN                      : 'I\'LL BE BACK' ;
AT                          : '@' ;
TRUE                        : 'NO PROBLEMO' ;
FALSE                       : 'I LIED' ;
PLUSOPERATOR                : 'GET UP' ;
MINUSOPERATOR               : 'GET DOWN' ;
MULTIPLICATIONOPERATOR      : 'YOU\'RE FIRED' ;
DIVISIONOPERATOR            : 'HE HAD TO SPLIT' ;
EQUALTO                     : 'YOU ARE NOT YOU YOU ARE ME' ;
GREATERTHAN                 : 'LET OFF SOME STEAM BENNET' ;
OR                          : 'CONSIDER THAT A DIVORCE' ;
AND                         : 'KNOCK KNOCK' ;
PRINT                       : 'TALK TO THE HAND' ;
DECLAREINT                  : 'HEY CHRISTMAS TREE' ;
SETINITIALVALUE             : 'YOU SET US UP' ;
ASSIGNVARIABLE              : 'GET TO THE CHOPPER' ;
SETVALUE                    : 'HERE IS MY INVITATION' ;
ENDASSIGNVARIABLE           : 'ENOUGH TALK' ;
IF                          : 'BECAUSE I\'M GOING TO SAY PLEASE' ;
ELSE                        : 'BULLSHIT' ;
ENDIF                       : 'YOU HAVE NO RESPECT FOR LOGIC' ;
WHILE                       : 'STICK AROUND' ;
ENDWHILE                    : 'CHILL' ;
IDENTIFIER                  : [a-zA-Z] [a-zA-Z0-9]* ;
NUMBER                      : DIGIT+ ;
STRING                      : STRING_F ;
WS                          : [ \r\n\t]+ -> skip ;

fragment DIGIT              : ('0'..'9')+ ;

fragment STRING_F           : '\'' ( STRING_ESCAPE_SEQ | ~[\\\r\n'] )* '\''
                            | '"' ( STRING_ESCAPE_SEQ | ~[\\\r\n"] )* '"'
                            ;

fragment LONG_STRING_CHAR   : ~'\\' ;

fragment STRING_ESCAPE_SEQ  : '\\' . ;
