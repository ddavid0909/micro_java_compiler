package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.structure.*;

/**
 * Abstract Syntax Tree Visitor.
 * Performs Semantic analysis on the generated AST if no syntax errors were found.
 */
public class SemanticAnalyzer extends VisitorAdaptor {
    public static final Struct boolType = new Struct(Struct.Bool);
    public static final Struct setType = new Struct(8);

    Obj currentMethod = null;
    Obj currentClass = null;
    Obj currentInterface = null;
    Struct currentType = null;

    int n_vars;
    private final SemanticErrorDetector errorDetector = new SemanticErrorDetector();

    public void report_info(String message, SyntaxNode info) {
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0 : info.getLine();
        if (line != 0)
            msg.append(" on line ").append(line);
        //System.out.println(msg.toString());
    }

    /**
     * Checks whether semantic errors were found.
     *
     * @return true if there were semantic errors.
     */
    public boolean getErrorDetected() {
        return this.errorDetector.errorDetected;
    }

    // PROGRAM

    public void visit(ProgName progName) {
        progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
        report_info("Semantic analysis of program " + progName.getProgName() + " started", progName);
        Tab.openScope();
    }

    /**
     * Visits Program node (the root node of the AST). <br>
     * Requires that main exist. <br>
     * Requires that main return type be void. <br>
     * Requires that main be with no parameters.<br>
     *
     * @param program - the root node of the AST.
     */
    public void visit(Program program) {
        program.obj = Tab.noObj;
        Obj main_node = Tab.currentScope.findSymbol("main");
        if (this.errorDetector.mainPresent(program)) {
            // TO-DO make error messages for main clearer!
            this.errorDetector.typesMatchExactly(main_node.getType(), Tab.noType, program);
            this.errorDetector.HasWrongParamNumber(main_node.getLevel(), 0, program);
        }
        this.n_vars = Tab.currentScope.getnVars();
        Tab.chainLocalSymbols(program.getProgName().obj);
        Tab.closeScope();
    }

    // CONSTANT DECLARATION

    /**
     * Sets expected type for all constants in the line.
     *
     * @param node AST node that contains the type.
     */
    public void visit(CurConstType node) {
        // TO-DO decide whether to change const type in case of nestedness or ignore the case since it shouldn't happen
        this.errorDetector.nestedConstOrVariableDeclaration(this.currentType, node);
        this.currentType = node.getType().struct;
    }

    // ONE CONSTANT nodes
    // following three nodes check that actual type of constant matches the type specified
    // at the beginning of declaration. In case of mismatch, semantic analysis will proceed with type of the actual
    // constant (5, 'c', true) and not type that is defined at the beginning of declaration (int, bool, char).
    // See symbol table of semantic_analysis/const_decl/badTypes.mj

    /**
     * Requires that expected type be integer for number constant
     *
     * @param node
     */
    public void visit(NumberConstant node) {
        this.errorDetector.typesMatchExactly(this.currentType, Tab.intType, node);
        node.struct = Tab.intType;
    }

    /**
     * Requires that expected type be character for character constant
     *
     * @param node
     */
    public void visit(CharacterConstant node) {
        this.errorDetector.typesMatchExactly(this.currentType, Tab.charType, node);
        node.struct = Tab.charType;
    }

    /**
     * Requires that expected type be boolean for boolean constant
     *
     * @param node
     */
    public void visit(BooleanConstant node) {
        this.errorDetector.typesMatchExactly(this.currentType, SemanticAnalyzer.boolType, node);
        node.struct = boolType;
    }

    /**
     * Requires that there be no same identifier declaration in the same scope. <br>
     * Inserts a new object node for the constant. <br>
     * Type of the object node corresponds to the type of the OneConst
     * (number, character, boolean) and not to the expected type written at the beginning of the line. <br>
     *
     * @param node
     */
    public void visit(NextConstDeclaration node) {
        if (this.errorDetector.identifierRedeclaration(node.getConstName(), node)) {
            return;
        }
        node.obj = Tab.insert(Obj.Con, node.getConstName(), node.getOneConst().struct);
        String type;
        if (node.getOneConst().struct == Tab.intType) {
            node.obj.setAdr(((NumberConstant) node.getOneConst()).getValue());
            type = "integer";
        } else if (node.getOneConst().struct == Tab.charType) {
            node.obj.setAdr(((CharacterConstant) node.getOneConst()).getValue());
            type = "char";
        } else if (node.getOneConst().struct == boolType) {
            node.obj.setAdr(((BooleanConstant) node.getOneConst()).getValue() ? 1 : 0);
            type = "boolean";
        } else type = "";
        report_info("Declared constant " + node.getConstName() + " of type " + type, node);
    }

    /**
     * Requires that there be no same identifier declaration in the same scope. <br>
     * Inserts a new object node for the constant. <br>
     * Type of the object node corresponds to the type of the OneConst
     * (number, character, boolean) and not to the expected type written at the beginning of the line. <br>
     * Resets expected type to null, as this is the end of one line <br>
     *
     * @param node
     */
    public void visit(ConstDeclaration node) {
        this.currentType = null;
        if (this.errorDetector.identifierRedeclaration(node.getConstName(), node)) {
            return;
        }
        node.obj = Tab.insert(Obj.Con, node.getConstName(), node.getOneConst().struct);

        String type = "";
        if (node.getOneConst().struct == Tab.intType) {
            node.obj.setAdr(((NumberConstant) node.getOneConst()).getValue());
            type = "integer";
        } else if (node.getOneConst().struct == Tab.charType) {
            node.obj.setAdr(((CharacterConstant) node.getOneConst()).getValue());
            type = "char";
        } else if (node.getOneConst().struct == SemanticAnalyzer.boolType) {
            node.obj.setAdr(((BooleanConstant) node.getOneConst()).getValue() ? 1 : 0);
            type = "boolean";
        }
        report_info("Declared constant " + node.getConstName() + " of type " + type, node);
    }

    // VARIABLE DECLARATION

    /**
     * Sets expected type for all variables in the line.
     *
     * @param node AST node that contains the type.
     */
    public void visit(CurVarType node) {
        this.errorDetector.nestedConstOrVariableDeclaration(this.currentType, node);
        this.currentType = node.getType().struct;
    }

    // if not array variable, keeps the data type of the variable declaration.

    /**
     * ArrayVar nodes allow to distinguish between array and non-array variables. They define whether identifier
     * next to them is an array or a simple variable of type defined at the beginning of the line.
     *
     * @param node represents a non-array variable.
     */
    public void visit(IsNotArrayVar node) {
        node.struct = this.currentType != null ? this.currentType : Tab.noType;
    }

    // Changes the type of the array. Creates new struct node, does not create object node for array type.

    /**
     * ArrayVar nodes allow to distinguish between array and non-array variables. They define whether identifier
     * next to them is an array or a simple variable of type defined at the beginning of the line. <br>
     * ArrayVar creates a new struct node for each array, so they cannot be compared by reference equality.
     * No object node is created for new array type.
     *
     * @param node represents an array variable
     */
    public void visit(IsArrayVar node) {
        // TO-DO decide whether this check is appropriate at all based on syntax rules.
        if (this.errorDetector.noCurrentTypeSet(this.currentType, node)) {
            node.struct = new Struct(Struct.Array, Tab.noType);
            return;
        }
        if (this.errorDetector.isArrayable(this.currentType, node)) {
            node.struct = new Struct(Struct.Array, this.currentType);
        } else {
            node.struct = new Struct(Struct.Array, Tab.noType);
        }
    }

    /**
     * Requires that there be no same identifier declaration in the same scope. <br>
     * Inserts a new object node for the variable. <br>
     * Type of variable is defined by the expected type, it's array-ability by the corresponding Array node in the AST.
     *
     * @param node
     */
    public void visit(NextVariableDeclaration node) {
        if (this.errorDetector.identifierRedeclaration(node.getVarName(), node)) {
            return;
        }
        int kind = this.currentClass != null && this.currentMethod == null ? Obj.Fld : Obj.Var;
        node.obj = Tab.insert(kind, node.getVarName(), node.getArrayVar().struct);
        report_info("Declared variable " + node.getVarName(), node);
        node.obj.setLevel(this.currentMethod == null ? 0 : 1);
    }

    /**
     * Requires that there be no same identifier declaration in the same scope. <br>
     * Inserts a new object node for the variable. <br>
     * Type of variable is defined by the expected type, it's array-ability by the corresponding Array node in the AST. <br>
     * Resets the expected type as it represents the end of line.
     *
     * @param node
     */
    public void visit(VariableDeclaration node) {
        this.currentType = null;
        if (this.errorDetector.identifierRedeclaration(node.getVarName(), node)) {
            return;
        }
        int kind = this.currentClass != null && this.currentMethod == null ? Obj.Fld : Obj.Var;
        node.obj = Tab.insert(kind, node.getVarName(), node.getArrayVar().struct);
        report_info("Declared variable " + node.getVarName(), node);
        node.obj.setLevel(this.currentMethod == null ? 0 : 1);

    }

    /**
     * Requires that the type be already declared. <br>
     * Requires that the declared identifier represent a type. <br>
     * In case of absence, struct node set to noType.
     *
     * @param type
     */
    public void visit(Type type) {
        type.struct = Tab.noType;
        if (this.errorDetector.undeclaredIdentifier(type.getTypeName(), type)) return;
        Obj typeNode = Tab.find(type.getTypeName());
        if (this.errorDetector.badKind(typeNode.getKind(), Obj.Type, type.getTypeName(), type)) return;
        type.struct = typeNode.getType();
    }

    // METHOD DECLARATIONS

    /**
     * Requires that the function not be nested within another method. <br>
     * Requires that identifier not be already declared within the same scope. <br>
     * Declares a method within the current scope. <br>
     * Distinguishes between global method and class method through fpPos. <br>
     * Opens scope for the method variables. <br>
     * Adds this-parameter to the scope if within class declaration. <br>
     *
     * @param node
     */
    public void visit(FunctionTypeName node) {
        node.obj = Tab.noObj;
        if (this.errorDetector.isNested(node, this.currentMethod, null, null)) return;
        if (this.errorDetector.identifierRedeclaration(node.getFunctionName(), node)) return;
        this.currentMethod = Tab.insert(Obj.Meth, node.getFunctionName(), node.getType().struct);
        this.currentMethod.setFpPos(this.currentClass == null ? 0 : 1);
        node.obj = this.currentMethod;
        Tab.openScope();

        // No need to increment level when adding this-parameter as level is already incremented to 1 due to the method not
        // being in the global scope.
        if (this.currentClass != null) {
            Tab.insert(Obj.Var, "this", this.currentClass.getType());
            // this.currentMethod.setLevel(this.currentMethod.getLevel()+1);
            report_info("Recognized method  " + this.currentMethod.getName() +
                    " of class " + this.currentClass.getName(), node);
        }
        report_info("Processing function " + node.obj.getName(), node);
    }

    /**
     * Requires that the procedure not be nested within another procedure. <br>
     * Requires that identifier not be already declared within the same scope. <br>
     * Declares a method within the current scope. <br>
     * Distinguishes between global method and class method through fpPos. <br>
     * Opens scope for the method variables. <br>
     * Adds this-parameter to the scope if within class declaration. <br>
     *
     * @param node
     */
    public void visit(ProcedureTypeName node) {
        node.obj = Tab.noObj;
        if (this.errorDetector.isNested(node, this.currentMethod, null, null)) return;
        if (this.errorDetector.identifierRedeclaration(node.getProcedureName(), node)) return;
        this.currentMethod = Tab.insert(Obj.Meth, node.getProcedureName(), Tab.noType);
        this.currentMethod.setFpPos(this.currentClass == null ? 0 : 1);
        node.obj = this.currentMethod;
        Tab.openScope();
        if (this.currentClass != null) {
            Tab.insert(Obj.Var, "this", this.currentClass.getType());
            report_info("Recognized method  " + this.currentMethod.getName()
                    + " of class " + this.currentClass.getName(), node);
        }
        report_info("Processing the procedure " + node.getProcedureName(), node);
    }

    /**
     * Propagates method object node upwards.
     *
     * @param node
     */
    public void visit(FunctionSignature node) {
        node.obj = node.getFunctionTypeName().obj;
    }

    /**
     * Propagates method object node upwards
     *
     * @param node
     */
    public void visit(ProcedureSignature node) {
        node.obj = node.getProcedureTypeName().obj;
    }

    /**
     * Puts formal parameter type into the currentType variable. <br>
     * currentType will be used and saved locally within ArrayVar node.
     *
     * @param node
     */
    public void visit(FormParsType node) {
        this.currentType = node.getType().struct;
    }


    /**
     * Resets current type. <br>
     * Requires that identifier not be already declared within the scope. <br>
     * Inserts a new variable object node with type defined in ArrayVar <br>
     * Updates the number of formal parameters for the method. <br>
     *
     * @param node
     */
    public void visit(FormalParametersList node) {
        node.obj = Tab.noObj;
        this.currentType = null; // was needed by arrayVar, already used when visiting this node.
        if (this.errorDetector.identifierRedeclaration(node.getFormParName(), node))
            return; // PARAMETERS WITH SAME NAME
        node.obj = Tab.insert(Obj.Var, node.getFormParName(), node.getArrayVar().struct);
        this.currentMethod.setLevel(this.currentMethod.getLevel() + 1);
        report_info("Declared parameter " + node.getFormParName() + " of method " + this.currentMethod.getName(), node);
    }

    /**
     * Resets current type. <br>
     * Requires that identifier not be already declared within the scope. <br>
     * Inserts a new variable object node with type defined in ArrayVar <br>
     * Updates the number of formal parameters for the method. <br>
     *
     * @param node
     */
    public void visit(FormalParameter node) {
        node.obj = Tab.noObj;
        this.currentType = null;
        if (this.errorDetector.identifierRedeclaration(node.getFormParName(), node)) return;
        node.obj = Tab.insert(Obj.Var, node.getFormParName(), node.getArrayVar().struct);
        this.currentMethod.setLevel(this.currentMethod.getLevel() + 1);
        report_info("Declared parameter " + node.getFormParName() + " of method " + this.currentMethod.getName(), node);
    }

    /**
     * Requires that return statement be within a function. <br>
     * Requires that the type of return statement be assignable to the return type of the function <br>
     *
     * @param node
     */
    public void visit(ReturnStatement node) {
        Obj expression = node.getExprOrNone().obj;
        Obj my_method = this.currentMethod;
        if (this.errorDetector.strayReturn(my_method, node)) return;
        this.errorDetector.assignableTo(expression.getType(), my_method.getType(), node);
    }

    /**
     * Closes the scope of the method. <br>
     * Requires that overriding be correct, in case of overriding. <br>
     * Requires that the method contain return if function. <br>
     * Saves method object node.<br>
     * Resets current method variable. <br>
     *
     * @param node
     */
    public void visit(MethodDeclaration node) {
        node.obj = this.currentMethod;
        Tab.chainLocalSymbols(this.currentMethod);
        Tab.closeScope();
        if (this.currentClass != null && this.currentClass.getType().getElemType() != null) {
            this.errorDetector.isProperOverriding(this.currentClass, this.currentMethod, node);
        }
        this.errorDetector.functionContainsReturn(node);

        report_info("Finished processing of method " + node.obj.getName(), node);
        this.currentMethod = null;

    }

    /**
     * Requires that class not be nested in another method, class or interface. <br>
     * Creates a new type object node for class. <br>
     * Opens a new scope for the class. <br>
     * Adds a zeroth field for virtual pointer table. <br>
     *
     * @param node
     */
    public void visit(ClassDeclStart node) {
        if (this.errorDetector.isNested(node, this.currentMethod, this.currentClass, this.currentInterface)) return;
        this.currentClass = Tab.insert(Obj.Type, node.getClassName(), new Struct(Struct.Class, new HashTableDataStructure()));
        Tab.openScope();
        Tab.insert(Obj.Fld, "_VTP", Tab.intType);
        report_info("Processing class " + node.getClassName(), node);
    }

    /**
     * Requires that the type being extended be already declared. <br>
     * Requires that the type be either class or interface (extensible). <br>
     * If extending interface: adds interface to list of implemented interfaces. <br>
     * If extending class: adds base class as element type for the inherited class.
     * Copies the fields (not methods!).
     * Copies interfaces implemented in base class.
     * @param node
     */
    public void visit(ExtendsDeclaration node) {
        if (this.errorDetector.undeclaredIdentifier(node.getType().getTypeName(), node)) return;
        Obj type_node = Tab.find(node.getType().getTypeName());
        if (!this.errorDetector.isExtensible(type_node.getType(), node)) return;

        // if extending interface
        if (type_node.getType().getKind() == Struct.Interface) {
            this.currentClass.getType().addImplementedInterface(type_node.getType());
            report_info("Implementiran interfejs " + node.getType().getTypeName(), node);
            return;
        }

        // if extending class - mark in object node and add fields.
        this.currentClass.getType().setElementType(type_node.getType());
        for (Obj symbol : type_node.getType().getMembers()) {
            if (symbol.getKind() == Obj.Fld) {
                Tab.insert(symbol.getKind(), symbol.getName(), symbol.getType());
            }
        }
        // inherit implemented interfaces in base class.
        for (Struct interface_ : type_node.getType().getImplementedInterfaces()) {
            this.currentClass.getType().addImplementedInterface(interface_);
        }
        report_info("Inherited attributes of class " + node.getType().getTypeName(), node);
    }

    /**
     * Wraps up class declaration. <br>
     * Closes the scope. <br>
     * Adds all fields defined in the base and new class. <br>
     * Adds all methods. If inherited, modifies the type of this-parameter. <br>
     * If redefined, picks redefinitions over base-class definitions. <br>
     * Adds all other methods, specific to the new class. <br>
     * Requires that all methods from interface be implemented. <br>
     * @param node
     */

    public void visit(ClassDeclaration node) {
        node.obj = Tab.noObj;
        Tab.chainLocalSymbols(this.currentClass);
        var temp_table = new HashTableDataStructure();
        // add fields
        int offset = 0;
        for (Obj symbol : this.currentClass.getLocalSymbols()) {
            if ((symbol.getKind() == Obj.Fld || symbol.getKind() == Obj.Var)) {
                Obj new_node = new Obj(Obj.Fld, symbol.getName(), symbol.getType());
                new_node.setAdr(offset);
                if (temp_table.insertKey(new_node)) {
                    offset++;
                }
            }
        }

        // add methods from base class. Pick redefinitions if present.
        if (this.currentClass.getType().getElemType() != null) {
            for (Obj symbol : this.currentClass.getType().getElemType().getMembers()) {
                if (symbol.getKind() == Obj.Meth) {
                    boolean added = false;
                    // if this is an overriding, add the new definition
                    for (Obj local_symbol : this.currentClass.getLocalSymbols()) {
                        if (local_symbol.getName().equals(symbol.getName())) {
                            temp_table.insertKey(local_symbol);
                            added = true;
                            break;
                        }
                    }
                    // else add old
                    if (!added) {
                        // modify this-parameter only
                        Obj new_method = new Obj(Obj.Meth, symbol.getName(), symbol.getType(), 0, symbol.getLevel());
                        new_method.setFpPos(1);
                        HashTableDataStructure locals = new HashTableDataStructure();
                        // copy ALL variables, including local variables, as the exact same code will be used.
                        for (Obj elem : symbol.getLocalSymbols()) {
                            if (elem.getName().equals("this")) {
                                locals.insertKey(new Obj(elem.getKind(), elem.getName(), this.currentClass.getType(),
                                        elem.getAdr(), elem.getLevel()));
                            } else {
                                locals.insertKey(new Obj(elem.getKind(), elem.getName(), elem.getType(), elem.getAdr(),
                                        elem.getLevel()));
                            }
                        }
                        new_method.setLocals(locals);
                        temp_table.insertKey(new_method);
                    }
                }
            }
        }

        // Add all newly defined, not yet added methods.
        for (Obj method : this.currentClass.getLocalSymbols()) {
            if (!temp_table.symbols().contains(method)) {
                temp_table.insertKey(method);
            }
        }
        this.errorDetector.classImplementsInterfaces(this.currentClass, temp_table, node);
        this.currentClass.getType().setMembers(temp_table);
        this.currentClass.setLocals(null);
        Tab.closeScope();
        report_info("Successfully finished declaration of class " + this.currentClass.getName() + " with number of fields "
                + this.currentClass.getType().getNumberOfFields(), node);
        node.obj = this.currentClass;
        this.currentClass = null;
    }

    // INTERFACE DECLARATIONS

    /**
     * Creates a new object node for interface. Sets the current interface variable. <br>
     * Requires that interface declaration not be nested within function, class or interface. <br>
     * Requires that identifier not be already declared. <br>
     * Opens interface scope. <br>
     * @param node
     */
    public void visit(InterfaceDeclStart node) {
        node.obj = Tab.noObj;
        if (this.errorDetector.isNested(node, currentMethod, currentClass, currentInterface)) return;
        if (this.errorDetector.identifierRedeclaration(node.getInterfaceName(), node)) return;
        this.currentInterface = Tab.insert(Obj.Type, node.getInterfaceName(), new Struct(Struct.Interface));
        Tab.openScope();
        report_info("Started declaration of interface " + node.getInterfaceName(), node);
    }

    /**
     * Finishes interface declaration. <br>
     * Closes the scope. Clears the current interface variable. <br>
     * @param node
     */
    public void visit(InterfaceDecl node) {
        Tab.chainLocalSymbols(this.currentInterface.getType());
        Tab.closeScope();
        node.obj = this.currentInterface;
        this.currentInterface = null;
        report_info("Finsihed declaration of interface " + node.obj.getName(), node);
    }

    /**
     * Sets fpPos to 1 in the method node to mark it as implemented.
     * @param node
     */
    public void visit(InterfaceMethodDeclaration node) {
        // ovo znaci da je metod vec implementiran
        node.obj = node.getMethodDecl().obj;
        node.obj.setFpPos(1);
        report_info("Finished declaration of interface method " + node.obj.getName() + " " + node.obj.getFpPos(), node);
    }

    /**
     * Closes method scope. Sets fpPos to 0 in the method node to mark it as not implemented.
     * @param node
     */
    public void visit(InterfaceMethodSignature node) {
        // ovo znaci da metod nije implementiran.
        node.obj = node.getMethodSignature().obj;
        node.obj.setFpPos(0);
        this.currentMethod = null;
        Tab.chainLocalSymbols(node.obj);
        Tab.closeScope();
        report_info("Finished declaration of interface method " + node.obj.getName(), node);
    }

    // FACTOR
    // Postavlja strukturni cvor na int type
    public void visit(NumberFactor node) {
        node.obj = new Obj(Obj.Con, null, Tab.intType);
        node.obj.setAdr(node.getValue());
    }

    // Postavlja strukturni cvor na char type
    public void visit(CharacterFactor node) {
        node.obj = new Obj(Obj.Con, null, Tab.charType);
        node.obj.setAdr(node.getValue());
    }

    // Postavlja strukturni cvor na bool type
    public void visit(BooleanFactor node) {
        node.obj = new Obj(Obj.Con, null, boolType);
        node.obj.setAdr(node.getValue() ? 1 : 0);
    }

    public void visit(BracketedExpressionFactor node) {
        node.obj = node.getExpr().obj;
    }

    // Postavlja strukturni cvor na odgovarajucu vrstu niza ili skup.
    public void visit(AllocateArrayFactor node) {
        node.obj = Tab.noObj;
//		TO-DO this error does not explain that array size should be integer, just says there is a type mismatch.
//		Add later
        if (!this.errorDetector.typesMatchExactly(node.getExpr().obj.getType(), Tab.intType, node)) return;

        if (node.getType().struct == SemanticAnalyzer.setType) {
            node.obj = new Obj(Obj.Con, null, setType);
            report_info("Alocira se set", node);
            return;
        }

        node.obj = new Obj(Obj.Con, null, new Struct(Struct.Array, node.getType().struct));
        report_info("Alocira se niz tipa " + node.getType().getTypeName(), node);

    }

    // Postavlja strukturni cvor na odgovarajucu vrstu klase - trenutno se ne
    // koristi
    public void visit(AllocateVariableFactor node) {
        node.obj = Tab.noObj;
//		Tip za koji se alocira prostor nije klasa
        if (this.errorDetector.badKind(node.getType().struct.getKind(), Struct.Class, node.getType().getTypeName(), node))
            return;
        node.obj = new Obj(Obj.Con, null, node.getType().struct);
    }

    // Samo provjerava da je pronadjeni designator metod ili ne-metod, u skladu sa
    // zagradama.
    public void visit(DesignatorFactor node) {
        node.obj = node.getDesignatorList().obj;
        if (node.getBracedActParsList() instanceof NoBracedActualParametersList) {
            this.errorDetector.isCallable(node.obj, node);
            return;
        }
        if (this.errorDetector.isNotMethod(node.getDesignatorList().obj, node) ||
                this.errorDetector.isProcedure(node.getDesignatorList().obj, node)) return;

        ActParsList parameterList = ((BracedActualParametersList) node.getBracedActParsList()).getActParsList();
        this.errorDetector.checkActualAndFormalParameters(node, node.obj, parameterList);
    }

    // DESIGNATOR

    public void visit(DesignatorList node) {
        // designator list pamti tip.
        node.obj = DesignatorIterator.traverseDesignator(node);
        report_info("Zavrsetak obrade designatora ", node);
    }

    public void visit(ArrayIdentifier node) {
        node.obj = Tab.noObj;
        // sam objektni cvor mora da predstavlja niz, odnosno da bude tipa niza.
        Obj designator = DesignatorIterator.getPreviousDesignator(node);
        if (this.errorDetector.cannotContainArray(designator, node)) return;
        if (this.errorDetector.badKind(designator.getType().getKind(), Struct.Array, designator.getName(), node))
            return;
        if (!this.errorDetector.typesMatchExactly(node.getExpr().obj.getType(), Tab.intType, node)) return;
        // Ovo je za niz postavljeno u IsArrayVar.
        Struct searched_type = DesignatorIterator.searchType(designator);
        node.obj = new Obj(Obj.Elem, null, searched_type); // treba da dobije objektni cvor tipa niza, a da ga pretvori
        // u objektni cvor

    }

    public void visit(MemberIdentifier node) {
        node.obj = Tab.noObj;
        // postavlja svoj objektni cvor na cvor tipa
        Obj designator = DesignatorIterator.getPreviousDesignator(node);
        if (this.errorDetector.cannotContainClass(designator, node)) return;
        if (!this.errorDetector.isExtensible(designator.getType(), node)) return;

        Obj field_node;
        if (this.currentClass != null && this.currentClass.getType() == designator.getType()) {
            field_node = Tab.currentScope.getOuter().findSymbol(node.getFieldName());
        } else {
            field_node = designator.getType().getMembersTable().searchKey(node.getFieldName());
        }
        if (this.errorDetector.nonExistentClassMember(field_node, node)) return;
        node.obj = field_node;
    }

    public void visit(DesignatorIdentifier node) {
        node.obj = Tab.find(node.getObjectName());
        if (this.errorDetector.undeclaredIdentifier(node.getObjectName(), node)
                || this.errorDetector.isNotAccessible(node.obj, node)) {
            node.obj = Tab.noObj;
        }
    }

    public void visit(DesignatorListEnd node) {
        node.obj = DesignatorIterator.getPreviousDesignator(node);
    }

    // DESIGNATOR STATEMENTS

    public void visit(IncrementDesignatorStatement node) {
        if (this.errorDetector.isNotModifiable(node.getDesignatorList().obj, node)) return;
        this.errorDetector.typesMatchExactly(node.getDesignatorList().obj.getType(), Tab.intType, node);
    }

    public void visit(DualIncrementDesignatorStatement node) {
        if (this.errorDetector.isNotModifiable(node.getDesignatorList().obj, node)) return;
        this.errorDetector.typesMatchExactly(node.getDesignatorList().obj.getType(), Tab.intType, node);
    }

    public void visit(DecrementDesignatorStatement node) {
        if (this.errorDetector.isNotModifiable(node.getDesignatorList().obj, node)) return;
        this.errorDetector.typesMatchExactly(node.getDesignatorList().obj.getType(), Tab.intType, node);
    }

    public void visit(ExprAssignmentDesignatorStatement node) {
        // U nekoj gramatici bi se moglo dozvoliti da bude metod, ako taj metod poslije
        // vraca refType, ali u postavci pise
        // da to ne treba dozvoliti
        if (this.errorDetector.isNotModifiable(node.getDesignatorList().obj, node)) return;
        // Sa lijeve strane treba da bude izraz koji se dodjeljuje, a argument je
        // designator kome se dodjeljuje
        // Zato sto se za this provjerava da moze da bude null.

        this.errorDetector.assignableTo(node.getExpr().obj.getType(), node.getDesignatorList().obj.getType(), node);
    }
    // TERMS AND EXPRESSIONS
    // Expression and Term can be non-integer if they are standalone. If another term and expression appears concatenated
    // all terms and expressions must be integer as they can only be concatenated by multiplicative and additive operators.
    // TERM

    public void visit(NextFactorExpression node) {
        this.errorDetector.typesMatchExactly(node.getFactor().obj.getType(), Tab.intType, node);
        node.obj = node.getFactor().obj;
    }

    public void visit(NoNextFactorExpression node) {
        node.obj = Tab.noObj;
    }

    public void visit(Term node) {
        if (!(node.getTermNext() instanceof NoNextFactorExpression)) {
            this.errorDetector.typesMatchExactly(node.getFactor().obj.getType(), Tab.intType, node);
        }
        node.obj = node.getFactor().obj;
    }

    // EXPRESSION

    public void visit(ExpressionExists node) {
        node.obj = node.getExpr().obj;
    }

    public void visit(NoExpression node) {
        node.obj = Tab.noObj;
    }

    public void visit(Expression node) {
        if (!(node.getExprNext() instanceof NoNextTermExpression)) {
            this.errorDetector.typesMatchExactly(node.getTerm().obj.getType(), Tab.intType, node);
        }
        node.obj = node.getTerm().obj;
    }

    // can only negate number
    public void visit(NegativeExpression node) {
        this.errorDetector.typesMatchExactly(node.getTerm().obj.getType(), Tab.intType, node);
        node.obj = node.getTerm().obj;
    }

    public void visit(NextTermExpression node) {
        this.errorDetector.typesMatchExactly(node.getTerm().obj.getType(), Tab.intType, node);
        node.obj = node.getTerm().obj;
    }

    public void visit(NoNextTermExpression node) {
        node.obj = Tab.noObj;
    }

    // PREDEFINED METHODS

    public void visit(ReadStatement node) {
        Obj my_designator = node.getDesignatorList().obj;
        // There was Obj.Type as well.
        this.errorDetector.isNotModifiable(my_designator, node);
        this.errorDetector.isNotBaseType(my_designator, node);
    }

    public void visit(PrintStatementNoNumber node) {
        Obj my_expr = node.getExpr().obj;
        this.errorDetector.isNotPrintable(my_expr, node);
    }

    public void visit(PrintStatementYesNumber node) {
        // TO-DO highly likely number constant cannot be negative
        this.errorDetector.isNonPositiveNumber(node.getNumber(), node);
        this.errorDetector.isNotPrintable(node.getExpr().obj, node);
    }

    // SET

    public void visit(SetAssignmentDesignatorStatement node) {
        Obj dest = node.getDesignatorList().obj;
        Obj left = node.getDesignatorList1().obj;
        Obj right = node.getDesignatorList2().obj;
        // Type was also Modifiable (accepted as good).
        this.errorDetector.isNotModifiable(dest, node);
        this.errorDetector.typesMatchExactly(dest.getType(), SemanticAnalyzer.setType, node);
        this.errorDetector.typesMatchExactly(left.getType(), SemanticAnalyzer.setType, node);
        this.errorDetector.typesMatchExactly(right.getType(), SemanticAnalyzer.setType, node);
    }

    // METHOD CALL

    public void visit(ActParam node) {
        node.obj = node.getExpr().obj;
    }

    public void visit(MethodCallDesignatorStatement node) {
        Obj method_called = node.getDesignatorList().obj;
        if (this.errorDetector.badKind(method_called.getKind(), Obj.Meth, method_called.getName(), node)) return;
        ActParsList parameterList = node.getActParsList();
        this.errorDetector.checkActualAndFormalParameters(node, method_called, parameterList);
    }

    // Identicno kao u MethodCallDesignatorStatement treba i za DesignatorFactor

    // CONDITIONAL OPERATIONS

    public void visit(ConditionFactor node) {
        Obj leftOperand = node.getExpr().obj;
        // No right operand - must be boolean
        if (node.getRelExprOrNone() instanceof NoComparisonExpression) {
            this.errorDetector.typesMatchExactly(leftOperand.getType(), SemanticAnalyzer.boolType, node);
            return;
        }
        // if there is right operand, operands must be compatible and relational operator must be valid.
        Obj rightOperand = ((ComparisonExpression) node.getRelExprOrNone()).getExpr().obj;
        this.errorDetector.compatibleWith(leftOperand.getType(), rightOperand.getType(), node);
        this.errorDetector.badRelOp(rightOperand, node);
    }

    public void visit(BreakStatement node) {
        this.errorDetector.notWithinDoWhile("Break", node);
    }

    public void visit(BreakNumStatement node) {
        if (this.errorDetector.isNonPositiveNumber(node.getNumber(), node)) return;
        this.errorDetector.notWithinDoWhile("Break", node, node.getNumber());
    }

    public void visit(ContinueStatement node) {
        this.errorDetector.notWithinDoWhile("Continue", node);
    }

    public void visit(MapExpression node) {
        DesignatorList function = node.getDesignatorList();
        DesignatorList array = node.getDesignatorList1();

        this.errorDetector.badKind(function.obj.getKind(), Obj.Meth, function.obj.getName(), node);
        this.errorDetector.typesMatchExactly(function.obj.getType(), Tab.intType, node);
        this.errorDetector.HasWrongParamNumber(function.obj.getLevel(), 1, node);
        this.errorDetector.isNotMappable(function.obj, node);
        this.errorDetector.badKind(array.obj.getType().getKind(), Struct.Array, array.obj.getName(), node);
        this.errorDetector.typesMatchExactly(array.obj.getType().getElemType(), Tab.intType, node);

        node.obj = new Obj(Obj.Con, null, Tab.intType);
    }

    public void visit(MaxArrayElementDesignatorFactor node) {
        node.obj = Tab.noObj;
        // operator works only on array element or integer array
        this.errorDetector.hashOperatorErrors(node);
        node.obj = new Obj(Obj.Con, null, Tab.intType);
    }

}
