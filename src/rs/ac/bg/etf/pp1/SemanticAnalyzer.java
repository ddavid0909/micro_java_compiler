package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.structure.*;

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

    public boolean getErrorDetected() {
        return this.errorDetector.errorDetected;
    }

    // PROGRAM

    public void visit(ProgName progName) {
        progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
        report_info("Vrsi se semanticka analiza programa cije je ime " + progName.getProgName() + "dato", progName);
        Tab.openScope();
    }

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

    // Postavlja tip konstante za cijeli red.
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
    public void visit(NumberConstant node) {
        this.errorDetector.typesMatchExactly(this.currentType, Tab.intType, node);
        node.struct = Tab.intType;
    }

    public void visit(CharacterConstant node) {
        this.errorDetector.typesMatchExactly(this.currentType, Tab.charType, node);
        node.struct = Tab.charType;
    }

    public void visit(BooleanConstant node) {
        this.errorDetector.typesMatchExactly(this.currentType, SemanticAnalyzer.boolType, node);
        node.struct = boolType;
    }

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
    // Postavlja tip promjenljive za cijeli red.
    public void visit(CurVarType node) {
        this.errorDetector.nestedConstOrVariableDeclaration(this.currentType, node);
        this.currentType = node.getType().struct;
    }

    // if not array variable, keeps the data type of the variable declaration.
    public void visit(IsNotArrayVar node) {
        node.struct = this.currentType != null ? this.currentType : Tab.noType;
    }

    // Changes the type of the array. Creates new struct node, does not create object node for array type.
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


    public void visit(NextVariableDeclaration node) {
        if (this.errorDetector.identifierRedeclaration(node.getVarName(), node)) {
            return;
        }
        int kind = this.currentClass != null && this.currentMethod == null ? Obj.Fld : Obj.Var;
        node.obj = Tab.insert(kind, node.getVarName(), node.getArrayVar().struct);
        report_info("Declared variable " + node.getVarName(), node);
        node.obj.setLevel(this.currentMethod == null ? 0 : 1);
    }

    // Provjerava da li je promjenljijva deklarisana u opsegu
    // i ako nije dodaje je
    // oslobadja this.constType.
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

    public void visit(Type type) {
        type.struct = Tab.noType;
        // type node must exist
        if (this.errorDetector.undeclaredIdentifier(type.getTypeName(), type)) return;
        Obj typeNode = Tab.find(type.getTypeName());
        // type node must represent a type
        if (this.errorDetector.badKind(typeNode.getKind(), Obj.Type, type.getTypeName(), type)) return;
        // only then set struct node's value
        type.struct = typeNode.getType();
    }

    // METHOD DECLARATIONS

    // Provjerava da ime funkcije nije zauzeto i da nema ugnjezdjavanja funkcija
    // Dodaje objektni cvor za funkciju i otvara novi opseg
    // Po potrebi dodaje this
    public void visit(FunctionTypeName node) {
        node.obj = Tab.noObj;
        if (this.errorDetector.isNested(node, this.currentMethod, null, null)) return;
        if (this.errorDetector.identifierRedeclaration(node.getFunctionName(), node)) return;
        this.currentMethod = Tab.insert(Obj.Meth, node.getFunctionName(), node.getType().struct);
        this.currentMethod.setFpPos(this.currentClass == null ? 0 : 1);
        node.obj = this.currentMethod;
        Tab.openScope();

        // Podrska za klase. Ako je metod unutar klase, dodaje se kao prvi parametar
        // this
        if (this.currentClass != null) {
            Tab.insert(Obj.Var, "this", this.currentClass.getType());
            // this.currentMethod.setLevel(this.currentMethod.getLevel()+1);
            report_info("Recognized method  " + this.currentMethod.getName() +
                    " of class " + this.currentClass.getName(), node);
        }
        report_info("Processing function " + node.getFunctionName(), node);
    }

    // Provjerava da ime procedure nije zauzeto i da nema ugnjezdjavanja procedura
    // Dodaje objektni cvor za proceduru i otvara novi opseg
    // Po potrebi dodaje this
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

    public void visit(FunctionSignature node) {
        node.obj = node.getFunctionTypeName().obj;
    }

    public void visit(ProcedureSignature node) {
        node.obj = node.getProcedureTypeName().obj;
    }

    // Postavlja tip formalnog parametra u constType.
    public void visit(FormParsType node) {
        this.currentType = node.getType().struct;
    }

    // Resetuje se constType.
    // Dodaje se novi parametar ako nije potrebno redeklarisanje
    public void visit(FormalParametersList node) {
        node.obj = Tab.noObj;
        this.currentType = null; // was needed by arrayVar, already used when visiting this node.
        if (this.errorDetector.identifierRedeclaration(node.getFormParName(), node)) return;
        node.obj = Tab.insert(Obj.Var, node.getFormParName(), node.getArrayVar().struct);
        this.currentMethod.setLevel(this.currentMethod.getLevel() + 1);
        report_info("Declared parameter " + node.getFormParName() + " of method " + this.currentMethod.getName(), node);
    }

    // Resetuje se constType
    // Dodaje se novi parametar ako nije potrebno redeklarisanje
    public void visit(FormalParameter node) {
        node.obj = Tab.noObj;
        this.currentType = null;
        if (this.errorDetector.identifierRedeclaration(node.getFormParName(), node)) return;
        node.obj = Tab.insert(Obj.Var, node.getFormParName(), node.getArrayVar().struct);
        this.currentMethod.setLevel(this.currentMethod.getLevel() + 1);
        report_info("Declared parameter " + node.getFormParName() + " of method " + this.currentMethod.getName(), node);
    }

    // Vraca objektni cvor metode kojoj pripada cvor
	/*private Obj findMethod(SyntaxNode node) {
		SyntaxNode parent = node.getParent();
		while (!(parent instanceof MethodDeclaration) && (parent != null)) {
			parent = parent.getParent();
		}
		if (parent == null) {
			return Tab.noObj;
		}
		MethodDeclaration method = (MethodDeclaration)parent;
		if (method.getMethodSignature() instanceof FunctionSignature)
			return ((FunctionSignature)method.getMethodSignature()).getFunctionTypeName().obj;
		if (method.getMethodSignature() instanceof ProcedureSignature) 
			return ((ProcedureSignature)method.getMethodSignature()).getProcedureTypeName().obj;
		report_error("Greska", node);
		return Tab.noObj;
	} */


    public void visit(ReturnStatement node) {
        Obj expression = node.getExprOrNone().obj;
        Obj my_method = this.currentMethod;
        if (this.errorDetector.strayReturn(my_method, node)) return;
        this.errorDetector.assignableTo(expression.getType(), my_method.getType(), node);
    }

    public void visit(MethodDeclaration node) {
        node.obj = Tab.noObj;
        Tab.chainLocalSymbols(this.currentMethod);
        Tab.closeScope();

        if (this.currentClass != null && this.currentClass.getType().getElemType() != null) {
            // correct override?
            this.errorDetector.isProperOverriding(this.currentClass, this.currentMethod, node);
        }

        node.obj = this.currentMethod;

        this.errorDetector.functionContainsReturn(node);

        report_info("Finished processing of method " + this.currentMethod.getName(), node);
        this.currentMethod = null;

    }

    // CLASS DECLARATION
    public void visit(ClassDeclStart node) {
        if (this.errorDetector.isNested(node, this.currentMethod, this.currentClass, this.currentInterface)) return;
        this.currentClass = Tab.insert(Obj.Type, node.getClassName(), new Struct(Struct.Class, new HashTableDataStructure()));
        Tab.openScope();
        Tab.insert(Obj.Fld, "_VTP", Tab.intType);
        report_info("Processing class " + node.getClassName(), node);
    }

    public void visit(ExtendsDeclaration node) {
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

    public void visit(ClassDeclaration node) {
        node.obj = Tab.noObj;
        Tab.chainLocalSymbols(this.currentClass);
        var temp_table = new HashTableDataStructure();
        // dodavanje polja u members
        int offset = 0;
        for (Obj symbol : this.currentClass.getLocalSymbols()) {
            if ((symbol.getKind() == Obj.Fld || symbol.getKind() == Obj.Var)) {
                Obj new_node = new Obj(Obj.Fld, symbol.getName(), symbol.getType());
                new_node.setAdr(offset);
                if (temp_table.insertKey(new_node)) {
                    offset++;
                }
                ;
            }
        }

        // dodavanje metoda
        // ako je klasa naslijedjena
        if (this.currentClass.getType().getElemType() != null) {
            // za svaki od metoda natklase, ako takvi postoje
            for (Obj symbol : this.currentClass.getType().getElemType().getMembers()) {
                if (symbol.getKind() == Obj.Meth) {
                    boolean added = false;
                    // ukoliko je definisan metod istog imena u potklasi, dodaj taj dodefinisani
                    // ispravnost konkretnog metoda je obavljena pri deklaraciji.
                    for (Obj local_symbol : this.currentClass.getLocalSymbols()) {
                        if (local_symbol.getName().equals(symbol.getName())) {
                            temp_table.insertKey(local_symbol);
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        // ukoliko se radi o metodu koji nije promijenjen u novoj definiciji, potrebno
                        // je
                        // samo ga prepisati, uz promjenu vrijednosti this.
                        Obj new_method = new Obj(Obj.Meth, symbol.getName(), symbol.getType(), 0, symbol.getLevel());
                        new_method.setFpPos(1);
                        HashTableDataStructure locals = new HashTableDataStructure();
                        // ovdje je potrebno prepisati sve lokalne promjenljive, ukljucujuci one koje
                        // nisu formalni argumenti
                        // zato sto ce se koristiti isti kod.
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

        // Prethodni kod će da doda sve metode koji pripadaju natklasi i izabere
        // odgovarajuću njihovu definiciju.
        // Naredni kod dodaje samo preostale, novodefinisane metode.
        for (Obj method : this.currentClass.getLocalSymbols()) {
            if (!temp_table.symbols().contains(method)) {
                temp_table.insertKey(method);
            }
        }
        this.errorDetector.classImplementsInterfaces(this.currentClass, temp_table, node);
        this.currentClass.getType().setMembers(temp_table);
        this.currentClass.setLocals(null);
        Tab.closeScope();
        report_info("Zavrsena deklaracija klase " + this.currentClass.getName() + " sa brojem polja "
                + this.currentClass.getType().getNumberOfFields(), node);
        node.obj = this.currentClass;
        this.currentClass = null;
    }

    // INTERFACE DECLARATIONS

    public void visit(InterfaceDeclStart node) {
        node.obj = Tab.noObj;
        if (this.errorDetector.isNested(node, currentMethod, currentClass, currentInterface)) return;
        if (this.errorDetector.identifierRedeclaration(node.getInterfaceName(), node)) return;
        this.currentInterface = Tab.insert(Obj.Type, node.getInterfaceName(), new Struct(Struct.Interface));
        Tab.openScope();
        report_info("Started declaration of interface " + node.getInterfaceName(), node);
    }

    public void visit(InterfaceDecl node) {
        Tab.chainLocalSymbols(this.currentInterface.getType());
        Tab.closeScope();
        node.obj = this.currentInterface;
        this.currentInterface = null;
        report_info("Finsihed declaration of interface " + node.obj.getName(), node);
    }

    public void visit(InterfaceMethodDeclaration node) {
        // ovo znaci da je metod vec implementiran
        node.obj = node.getMethodDecl().obj;
        node.obj.setFpPos(1);
    }


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
        if (this.errorDetector.nonExistentClassField(field_node, node)) return;
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

    // PREDEFINISANI METODI

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
