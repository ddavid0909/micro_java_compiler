package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class SemanticErrorDetector {

    boolean errorDetected = false;
    Logger log = Logger.getLogger(this.getClass());

    /**
     * Checks whether node is at the line where no method, class or interface scope is already open
     * @param node The syntax node. Provides the line of occurrence.
     * @param currentMethod Flag to check for open method scope. Either SemanticAnalyzer.currentMethod or null
     * @param currentClass Flag to check for open class scope. Either SemanticAnalyzer.currentClass or null
     * @param currentInterface Flag to check for open interface scope. Either SemanticAnalyzer.currentInterface or null
     * @return False if nested, True if not nested. Side effect - detects semantic error and reports it.
     */
    public boolean isNested(SyntaxNode node, Obj currentMethod, Obj currentClass, Obj currentInterface) {
        if (currentMethod != null) {
            report_error("Semantic error. Definition nested within function definition", node);
            return true;
        }
        if (currentClass != null) {
            report_error("Semantic error. Definition nested within class definition", node);
            return true;
        }
        if (currentInterface != null) {
            report_error("Semantic error. Definition nested within interface definition", node);
            return true;
        }
        return false;
    }

    /**
     * Logs the error message
     * @param message The precise error message text
     * @param info The syntax node. Provides line of occurrence.
     */
    public void report_error(String message, SyntaxNode info) {
        this.errorDetected = true;
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0 : info.getLine();
        if (line != 0)
            msg.append(" on line ").append(line);
        this.log.error(msg.toString());
    }

    public void report_info(String message, SyntaxNode info) {
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0 : info.getLine();
        if (line != 0)
            msg.append(" on line ").append(line);
        this.log.info(msg.toString());
    }

    /**
     * Checks whether type of src can be assigned to the type of dest.
     * Covers interfaces, classes and basic types.
     * @param src Struct node of the source object
     * @param dest Struct node of the destination object
     * @param node Node that provides line of occurrence.
     * @return True if src can be assigned to dest, else False. Side effect - detects semantic error and reports it
     */

    public boolean assignableTo(Struct src, Struct dest, SyntaxNode node) {
        if (src == dest) return true;
        if (this.isRefType(dest.getKind()) && src == Tab.nullType) return true;
        if (src.getKind() == Struct.Class && dest.getKind() == Struct.Class) {
            while (src.getElemType() != null) {
                if (src.getElemType() == dest) return true;
                src = src.getElemType();
            }
            report_error("Semantic error. Assignment between classes from differing hierarchies", node);
            return false;
        }
        if (dest.getKind() == Struct.Interface && src.getKind() == Struct.Class) {
            if (src.getImplementedInterfaces().contains(dest)) return true;
            report_error("Semantic error. Assignment between interface and class that does not implement it", node);
            return false;
        }
        if (dest.getKind() == Struct.Array && src.getKind() == Struct.Array) {
            if (dest.getElemType() == Tab.noType) return true;
            return this.assignableTo(src.getElemType(), dest.getElemType(), node);
        }
        report_error("Semantic error. Erroneous assignment", node);
        return false;
    }

    /**
     * Checks whether two struct nodes are compatible,
     * i.e. whether it is possible for two object nodes with types s_1 and s_2 to store the same object. Covers classes,
     * interfaces and basic types
     * @param s_1 One Struct node
     * @param s_2 Other Struct node
     * @param node Syntax Node. Provides line of occurrence
     * @return True if compatible, else False. Side effect - detects semantic error and reports it.
     */
    public boolean compatibleWith(Struct s_1, Struct s_2, SyntaxNode node) {
        // Ako je isti strukturni cvor, sve je u redu
        // Ovo pokriva skoro sve slucajeve, osim nizova, uporedjivanja klasa u hijerarhiji i uporedijvanja klase i interfejsa u kom je.
        if (s_1 == s_2) return true;
        // Ako je referenca, moze jedna od vrijednosti da bude null.
        if (this.isRefType(s_2.getKind()) && s_1 == Tab.nullType || this.isRefType(s_1.getKind()) && s_2 == Tab.nullType) return true;
        // Ako su obje klase, moraju biti u istoj hijerarhiji
        if (s_1.getKind() == Struct.Class && s_2.getKind() == Struct.Class) {
            Struct s_1_temp = s_1;
            while (s_1_temp.getElemType() != null) {
                if (s_1_temp.getElemType() == s_2) return true;
                s_1_temp = s_1_temp.getElemType();
            }
            Struct s_2_temp = s_2;
            while (s_2_temp.getElemType() != null) {
                if (s_2_temp.getElemType() == s_1) return true;
                s_2_temp = s_2_temp.getElemType();
            }
            report_error("Semantic error. Comparison of classes that are not within the same hierarchy", node);
            return false;
        }
        // Ako je jedna referenca interfejs a druga klasa
        if (s_2.getKind() == Struct.Interface && s_1.getKind() == Struct.Class) {
            if (s_1.getImplementedInterfaces().contains(s_2)) return true;
            report_error("Semantic error. Comparison of incompatible class and interface", node);
            return false;
        }
        if (s_1.getKind() == Struct.Interface && s_2.getKind() == Struct.Class) {
            if (s_2.getImplementedInterfaces().contains(s_1)) return true;
            report_error("Semantic error. Comparison of incompatible class and interface", node);
            return false;
        }
        // Ako su nizovi obje, moraju da budu kompatibilni elementi
        if (s_2.getKind() == Struct.Array && s_1.getKind() == Struct.Array) {
            return this.compatibleWith(s_1.getElemType(), s_2.getElemType(), node);
        }
        report_error("Semantic error. Erroneous comparison", node);
        return false;
    }

    public boolean mainPresent(Program program) {
        Obj main_node = Tab.currentScope.findSymbol("main");
        if (main_node == null) {
            report_error("Semantic error. Procedure 'main' is missing from the program", program);
            return false;
        }
        return true;
    };

    public boolean typesMatchExactly(Struct type_1, Struct type_2, SyntaxNode node) {
        if (type_1 != type_2) {
            report_error("Semantic error. Basic types mismatch", node);
            return false;
        }
        return true;
    }

    public boolean identifierRedeclaration(String identifier_name, SyntaxNode node) {
        if (Tab.currentScope.findSymbol(identifier_name) != null) {
            report_error("Semantic error. Identifier '" + identifier_name + "' redeclared", node);
            return true;
        }
        return false;
    }

    public boolean nestedConstOrVariableDeclaration(Struct type_node, SyntaxNode node) {
        if (type_node != null) {
            report_error("Semantic error. One type declaration starts before another one ended", node);
            return true;
        }
        return false;
    }

    // TO-DO add array of interfaces.
    public boolean isArrayable(Struct type_node, SyntaxNode node) {
        if (type_node.getKind() == Struct.Int || type_node.getKind() == Struct.Char
                || type_node.getKind() == Struct.Bool || type_node.getKind() == Struct.Class) {
            return true;
        }
        report_error("Semantic error. Type of array not supported", node);
        return false;
    }

    public boolean isExtensible(Struct type_node, SyntaxNode node) {
        if (type_node.getKind() == Struct.Class || type_node.getKind() == Struct.Interface) {
            return true;
        }
        report_error("Semantic error. Type neither class nor interface", node);
        return false;
    }

    public boolean undeclaredIdentifier(String name, SyntaxNode node) {
        if (Tab.find(name) == Tab.noObj) {
            report_error("Semantic error. Undeclared identifier " + name + " used", node);
            return true;
        }
        return false;
    }

    public boolean badKind(int actual_kind, int expected_kind, String name, SyntaxNode node) {
        if (actual_kind != expected_kind) {
            report_error("Semantic error. Improper use of identifier " + name, node);
            return true;
        }
        return false;
    }

    private boolean methodIsReimplementationOf(Obj meth_1, Obj meth_2) {
        Collection<Obj> meth_1_symbols = meth_1.getLocalSymbols(), meth_2_symbols = meth_2.getLocalSymbols();
        Iterator<Obj> itH1 = meth_1_symbols.iterator(), itH2 = meth_2_symbols.iterator();

        if (meth_1.getLevel() != meth_2.getLevel()) {
            return false;
        }
        int counter = meth_1.getLevel();
        while (itH1.hasNext() && itH2.hasNext() && counter > 0) {
            Obj itH1_next = itH1.next();
            Obj itH2_next = itH2.next();
            counter--;
            if (Objects.equals(itH1_next.getName(), "this")) continue;
            if (!Objects.equals(itH1_next.getName(), itH2_next.getName())) return false;
            if (itH1_next.getType() != itH2_next.getType()) return false;
        }
        return true;
    }

    public boolean classImplementsInterfaces(Obj class_node, SymbolDataStructure temp_table, SyntaxNode node) {
        boolean correct = true;
        for (Struct interface_: class_node.getType().getImplementedInterfaces()) {
            for (Obj method : interface_.getMembers()) {
                if (temp_table.searchKey(method.getName()) == null) {
                    if (method.getFpPos() == 0) {
                        report_error("Semantic error. Not implemented interface method " + method.getName(), node);
                        correct = false;
                    } else {
                        temp_table.insertKey(method);
                    }
                } else {
                    Obj class_method = temp_table.searchKey(method.getName());
                    if (class_method.getLevel() != method.getLevel()) {
                        report_error("Semantic error. Interface method " +  method.getName() +  " reimplemented with a bad number of parameters", node);
                        correct = false;
                    }
                    // POSSIBLE BUG - this used to be pure equality between two struct nodes. Assignability makes more sense, but it is not tested
                    if (!this.assignableTo(class_method.getType(), method.getType(), node)) {
                        report_error("Semantic error. Interface method " + method.getName() + " reimplemented with different type", node);
                        correct = false;
                    }
                    if (!this.methodIsReimplementationOf(class_method, method)) {
                        report_error("Semantic error. Bad reimplementation of interface method " + method.getName(), node);
                        correct = false;
                    }
                }
            }
        }
        return correct;
    }

    public boolean isProperOverriding(Obj class_node, Obj method_node, SyntaxNode node) {
        boolean correct = true;
        Struct super_class = class_node.getType().getElemType();
        Collection<Obj> symbols = super_class.getMembers();
        for (Obj symbol : symbols) {
            if (symbol.getName().equals(method_node.getName())) {
                if (symbol.getKind() == method_node.getKind() && this.assignableTo(method_node.getType(), symbol.getType(), node)) {
                    Collection<Obj> h1Obj = symbol.getLocalSymbols(), h2Obj = method_node.getLocalSymbols();
                    Iterator<Obj> itH1 = h1Obj.iterator(), itH2 = h2Obj.iterator();

                    if (symbol.getLevel() != method_node.getLevel()) {
                        correct = false;
                        report_error("Semantic error. Incoherent parameter count after overriding "
                                + method_node.getName(), node);
                        break;
                    }

                    int counter = symbol.getLevel();
                    while (itH1.hasNext() && itH2.hasNext() && counter > 0) {
                        Obj itH1_next = itH1.next();
                        Obj itH2_next = itH2.next();
                        counter--;
                        if (!(itH1_next.getType() == itH2_next.getType() ||
                                (itH1_next.getType().getKind() == Struct.Array && itH2_next.getType().getKind() == Struct.Array
                                        && itH1_next.getType().getElemType() == itH2_next.getType().getElemType())
                                || (itH1_next.getName().equals(itH2_next.getName())
                                && itH1_next.getName().equals("this")))) {
                            correct = false;
                            report_error("Semantic error. Inappropriate types of parameter " + itH2_next.getName()
                                    + " with parameter " + itH1_next.getName() + " while overriding method "
                                    + symbol.getName(), node);
                        }
                    }
                } else {
                    correct = false;
                    report_error(
                            "Semantic error. Inappropriate identifier redeclaration of  " + method_node.getName() + " in class " + class_node.getName(),
                            node);
                }
                break;
            }
        }
        return correct;
    }

    private boolean containsReturn(MethodDeclaration node) {
        StatementList statements = node.getStatementList();
        if (statements instanceof NoStatementList) {
            return false;
        }
        while (statements instanceof StatementListExists) {
            StatementListExists exists = (StatementListExists) statements;
            if (exists.getStatement() instanceof ReturnStatement) {
                return true;
            }
            statements = exists.getStatementList();
        }
        return false;
    }

    public boolean functionContainsReturn(MethodDeclaration node) {
        if (node.getMethodSignature() instanceof FunctionSignature && !this.containsReturn(node)) {
            report_error("Function does not contain a return statement", node);
            return false;
        }
        return true;
    }

    public boolean isProcedure(Obj meth, SyntaxNode node) {
        if (meth.getKind() == Obj.Meth && meth.getType() == Tab.noType) {
            report_error("Semantic error. Method " + meth.getName() + " has no return value", node);
            return true;
        }
        return false;
    }

    public boolean isNotMethod(Obj meth, SyntaxNode node) {
        if (meth.getKind() != Obj.Meth) {
            report_error("Semantic error. " + meth.getName() + " is not a method", node);
            return true;
        }
        return false;
    }

    public boolean cannotContainArray(Obj arr, SyntaxNode node) {
        if (arr.getKind() != Obj.Var && arr.getKind() != Obj.Fld) {
            report_error("Semantic error. " + arr.getName() + " cannot contain array reference", node);
            return true;
        }
        return false;
    }

    public boolean cannotContainClass(Obj cls, SyntaxNode node) {
        if (cls.getKind() != Obj.Var && cls.getKind() != Obj.Fld && cls.getKind() != Obj.Elem) {
            report_error("Semantic error. " + cls.getName() + " cannot contain a class object", node);
            return true;
        }
        return false;
    }

    public boolean isNotAccessible(Obj obj_node, SyntaxNode node) {
        if (obj_node.getKind() != Obj.Var && obj_node.getKind() != Obj.Meth && obj_node.getKind() != Obj.Con && obj_node.getKind() != Obj.Fld) {
            report_error("Semantic error. " + obj_node.getName() + " cannot be accessed", node);
            return true;
        }
        return false;
    }

    public boolean isNotModifiable(Obj obj_node, SyntaxNode node) {
        if (obj_node.getKind() == Obj.Meth || obj_node.getKind() == Obj.Con || obj_node.getKind() == Obj.Type || obj_node.getKind() == Obj.Prog) {
            report_error("Semantic error. " + obj_node.getName() + " cannot be modified", node);
            return true;
        }
        return false;
    }

    public boolean isNotBaseType(Obj type_node, SyntaxNode node) {
        Struct type = type_node.getType();
        if (type.getKind() != Struct.Bool && type.getKind() != Struct.Char && type.getKind() != Struct.Int) {
            report_error("Semantic error. Identifier " + type_node.getName() + " not of base type", node);
            return true;
        }
        return false;
    }

    public boolean isNotPrintable(Obj type_node, SyntaxNode node) {
        Struct type = type_node.getType();
        if (type.getKind() != Struct.Bool && type.getKind() != Struct.Char && type.getKind() != Struct.Int && type.getKind() != SemanticAnalyzer.setType.getKind()) {
            report_error("Semantic error. Identifier " + type_node.getName() + " not printable", node);
            return true;
        }
        return false;
    }

    public boolean HasWrongParamNumber(int actual_number, int expected_number, SyntaxNode node) {
        if (actual_number != expected_number) {
            report_error("Semantic error. Expected number of parameters " + expected_number, node);
            return true;
        }
        return false;
    }

    public boolean isNotMappable(Obj meth, SyntaxNode node) {
        for (Obj parameter : meth.getLocalSymbols()) {
            if (parameter.getAdr() == 0) {
                if (parameter.getType() != Tab.intType) {
                    report_error("Semantic error. Function used in MAP expression misses an integer parameter",
                            node);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean notWithinDoWhile(String expr_type, SyntaxNode node) {
        SyntaxNode current = node;
        while (current != null && !(current instanceof DoWhileStatement)) {
            current = current.getParent();
        }
        if (current == null) {
            report_error("Semantic error. " + expr_type + " expression outside do-while loop", node);
            return true;
        }
        return false;
    }

    public boolean notWithinDoWhile(String expr_type, SyntaxNode node, int level) {
        int i = 0;
        while (node != null && i != level) {
            if (node instanceof DoWhileStatement) {
                i++;
            }
            node = node.getParent();
        }
        if (node == null) {
            report_error("Semantic error. " + expr_type + " expression not within " + level + "do-while loops", node);
            return true;
        }
        return false;
    }

    public boolean isNonPositiveNumber(int number, SyntaxNode node) {
        if (number <= 0) {
            report_error("Semantic error. Number " + number + " is non-positive", node);
            return true;
        }
        return false;
    }

    private boolean isRefType(int kind) {
        return kind == Struct.Array || kind == Struct.Class || kind == Struct.Interface || kind == SemanticAnalyzer.setType.getKind();
    }
    // It is checked before invocation that left and right operands are compatible
    public boolean badRelOp(Obj compatibleOperand, ConditionFactor node) {
        if (this.isRefType(compatibleOperand.getType().getKind())) {
            Relop relop = ((ComparisonExpression) node.getRelExprOrNone()).getRelop();
            if (!(relop instanceof IsEqual) && !(relop instanceof IsNotEqual)) {
                report_error("Semantic error. Reference types compared using operators other than == i !=",
                        node);
                return true;
            }
        }
        return false;
    }

    public void checkActualAndFormalParameters(SyntaxNode node, Obj method_called, ActParsList parameterList) {
        int parameters = method_called.getLevel();
        Struct[] parameter_types = new Struct[parameters];
        if (parameterList instanceof ActualParametersList) {
            parameter_types[parameters
                    - 1] = ((ActualParameter) ((ActualParametersList) parameterList).getActPars()).getActParam().obj
                    .getType();
            parameters -= 1;
            ActParsNext nextParameter = ((ActualParameter) ((ActualParametersList) parameterList).getActPars())
                    .getActParsNext();
            while (nextParameter instanceof NextActualParameter) {
                if (parameters == 0) {
                    report_error("Semantic error. Too many arguments passed", node);
                    return;
                }
                parameter_types[parameters - 1] = ((NextActualParameter) nextParameter).getActParam().obj.getType();
                nextParameter = ((NextActualParameter) nextParameter).getActParsNext();
                parameters -= 1;
            }
        }
        Collection<Obj> locals = method_called.getLocalSymbols();
        for (Obj form_par : locals) {
            if (form_par.getAdr() < method_called.getLevel()) {
                if (Objects.equals(form_par.getName(), "this")) {
                    if (parameter_types[0] != null) {
                        report_error("Semantic error. Argument passed in place of the default this", node);
                    }
                    continue;
                } else {
                    if (parameter_types[form_par.getAdr()] == null) {
                        report_error("Semantic error. Not enough arguments passed", node);
                        return;
                    }
                }

                if (!this.assignableTo(parameter_types[form_par.getAdr()], form_par.getType(), node)) {
                    report_error("Semantic error. Parameter " + form_par.getName() + " of inappropriate type", node);
                }
            }
        }
    }

    public boolean strayReturn(Obj outside_method, SyntaxNode node) {
        if (outside_method == null) {
            report_error("Semantic error. Return statement outside function", node);
            return true;
        }
        return false;
    }

    public boolean nonExistentClassMember(Obj field_node, MemberIdentifier node) {
        if (field_node == null) {
            report_error("Semantic error. Non existent class member "
                    + node.getFieldName(), node);
            return true;
        }
        return false;
    }

    public boolean noCurrentTypeSet(Struct currentType, SyntaxNode node) {
        if (currentType == null) {
            report_error("Semantic error. Type unknown", node);
            return true;
        }
        return false;
    }

    public boolean hashOperatorErrors(MaxArrayElementDesignatorFactor node) {
        Obj argument = node.getDesignatorList().obj;
        boolean errors = false;
        if (argument.getType().getKind() != Struct.Array && argument.getKind() != Obj.Elem) {
            report_error("Semantic error. Type of argument in # operator must be array or array element", node);
            errors = true;
        }
        if (argument.getType().getKind() == Struct.Array && argument.getType().getElemType() != Tab.intType) {
            report_error("Semantic error. Maximum array operator on non-integer array", node);
            errors = true;
        }
        return errors;
    }

    public boolean isCallable(Obj obj_node, SyntaxNode node) {
        if (obj_node.getKind() == Obj.Meth) {
            report_error("Semantic error. " + obj_node.getName() + " is a method", node);
            return true;
        }
        return false;
    }

}
