package rs.ac.bg.etf.pp1;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.structure.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	Obj currentMethod = null;
	Obj currentClass = null;
	Obj currentInterface = null;
	public static final Struct boolType = new Struct(Struct.Bool);
	public static final Struct setType = new Struct(8);
	Struct constType = null;
	boolean errorDetected = false;
	int n_vars;
	private final Logger log = Logger.getLogger(this.getClass());
	private final SemanticErrorDetector errorDetector = new SemanticErrorDetector();
	
	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		//log.info(msg.toString());
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
			if (main_node.getType() != Tab.noType) {
			report_error("Semanticka greska. Povratna vrijednost funkcije main razlicita od void", program);
		}
			if (main_node.getLevel() != 0) {
			report_error("Semanticka greska. Funkcija main je sa parametrima u programu", program);
		}
		}
		this.n_vars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}

	// CONSTANT DECLARATION

	// Postavlja tip konstante za cijeli red.
	public void visit(CurConstType node) {
		// TO-DO decide whether to change const type in case of nestedness or ignore the case since it shouldn't happen
		this.errorDetector.nestedConstOrVariableDeclaration(this.constType, node);
		this.constType = node.getType().struct;
	}

	// Postavlja brojnu vrijednost numericke konstante.
	public void visit(NumberConstant node) {
		// There was check for this.constType == null, should be impossible based on syntax rules.
		this.errorDetector.constTypesMatch(this.constType, Tab.intType, node);
		node.struct = Tab.intType;
	}

	public void visit(CharacterConstant node) {
		this.errorDetector.constTypesMatch(this.constType, Tab.charType, node);
		node.struct = Tab.charType;
	}

	public void visit(BooleanConstant node) {
		this.errorDetector.constTypesMatch(this.constType, SemanticAnalyzer.boolType, node);
		node.struct = boolType;
	}

	public void visit(NextConstDeclaration node) {
		// TO-DO is null check for constType missing? Syntax analysis would not allow CurConstType to be missing
		this.errorDetector.constTypesMatch(node.getOneConst().struct, this.constType, node);
		if (this.errorDetector.identifierRedeclaration(node.getConstName(), node)) {
			return;
		}
		node.obj = Tab.insert(Obj.Con, node.getConstName(), node.getOneConst().struct);
		String type;
		if (node.getOneConst().struct == Tab.intType) {
			node.obj.setAdr(((NumberConstant) node.getOneConst()).getValue());
			type = "integer";
		}
		else if (node.getOneConst().struct == Tab.charType) {
			node.obj.setAdr(((CharacterConstant) node.getOneConst()).getValue());
			type = "char";
		}
		else if (node.getOneConst().struct == boolType) {
			node.obj.setAdr(((BooleanConstant) node.getOneConst()).getValue() ? 1 : 0);
			type = "boolean";
		}
		else type = "";
		report_info("Declared constant " + node.getConstName() + " of type " + type, node);
	}

	public void visit(ConstDeclaration node) {
		this.errorDetector.constTypesMatch(node.getOneConst().struct, this.constType, node);
		// constant type variable must be reset after declaration line is completely processed.
		this.constType = null;
		if (this.errorDetector.identifierRedeclaration(node.getConstName(), node)) {
			return;
		}
		node.obj = Tab.insert(Obj.Con, node.getConstName(), node.getCurConstType().getType().struct);

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
		this.errorDetector.nestedConstOrVariableDeclaration(this.constType, node);
		this.constType = node.getType().struct;
	}

	// if not array variable, keeps the data type of the variable declaration.
	public void visit(IsNotArrayVar node) {
		node.struct = this.constType;
	}

	// Changes the type of the array. Creates new struct node, does not create object node for array type.
	public void visit(IsArrayVar node) {
		// TO-DO decide whether this check is appropriate at all based on syntax rules.
		if (this.constType == null) {
			report_error("Semanticka greska 9. Tip niza nije definisan ", node);
			return;
		}
		if (this.errorDetector.isArrayable(this.constType, node)) {
			node.struct = new Struct(Struct.Array, this.constType);
		} else {
			node.struct = new Struct(Struct.Array, Tab.noType);
		}
	}

	// Provjerava da li je promjenljiva deklarisana u opsegu
	// i ako nije dodaje je.
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
		this.constType = null;
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
		this.constType = node.getType().struct;
	}

	// Resetuje se constType.
	// Dodaje se novi parametar ako nije potrebno redeklarisanje
	public void visit(FormalParametersList node) {
		node.obj = Tab.noObj;
		this.constType = null; // was needed by arrayVar, already used when visiting this node.
		if (this.errorDetector.identifierRedeclaration(node.getFormParName(), node)) return;
		node.obj = Tab.insert(Obj.Var, node.getFormParName(), node.getArrayVar().struct);
		this.currentMethod.setLevel(this.currentMethod.getLevel() + 1);
		report_info("Declared parameter " + node.getFormParName() + " of method " + this.currentMethod.getName(), node);
	}

	// Resetuje se constType
	// Dodaje se novi parametar ako nije potrebno redeklarisanje
	public void visit(FormalParameter node) {
		node.obj = Tab.noObj;
		this.constType = null;
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
	
	private Boolean containsReturn(MethodDeclaration node) {
		node.obj = Tab.noObj;
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
	
	public void visit(ReturnStatement node) {
		Obj expression = node.getExprOrNone().obj;
		Obj my_method = this.currentMethod;
		if (my_method == null) {
			report_error("Return expression outside of method", node);
			return;
		}
		this.errorDetector.assignableTo(expression.getType(), my_method.getType(), node);
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
	
	public void visit(MethodDeclaration node) {
		node.obj = Tab.noObj;
		Tab.chainLocalSymbols(this.currentMethod);
		Tab.closeScope();

		if (this.currentClass != null && this.currentClass.getType().getElemType() != null) {
			// U ovoj metodi provjeravam direktno da li je metod, ukoliko je u pitanju
			// override, pravilno redefinisan, da li ima dobar broj argumenata dobrog tipa,
			// upisanih dobrim redoslijedom.
			// Ta greska ce biti uhvacena ovdje, i korisniku ce se reci na kojoj liniji je
			// problem.
			Struct super_class = this.currentClass.getType().getElemType();
			Collection<Obj> symbols = super_class.getMembers();
			for (Obj symbol : symbols) {
				if (symbol.getName().equals(this.currentMethod.getName())) {
					if (symbol.getKind() == this.currentMethod.getKind()
							&& this.errorDetector.assignableTo(this.currentMethod.getType(), symbol.getType(), node)) {
						// potrebno je uporediti sve argumente.
						Collection<Obj> h1Obj = symbol.getLocalSymbols(), h2Obj = this.currentMethod.getLocalSymbols();
						Iterator<Obj> itH1 = h1Obj.iterator(), itH2 = h2Obj.iterator();

						if (symbol.getLevel() != this.currentMethod.getLevel()) {
							report_error("Semanticka greska 20. Neslaganje broja argumenata pri overridingu metode "
									+ this.currentMethod.getName(), node);
							break;
						}
						int counter = symbol.getLevel();
						while (itH1.hasNext() && itH2.hasNext() && counter > 0) {
							Obj itH1_next = itH1.next();
							Obj itH2_next = itH2.next();
							counter--;
							// tipovi this-ova se nece slagati
							if (!(itH1_next.getType() == itH2_next.getType() || 
									(itH1_next.getKind() == Struct.Array && itH2_next.getKind() == Struct.Array 
									&& itH1_next.getType().getElemType() == itH2_next.getType().getElemType())
									|| (itH1_next.getName().equals(itH2_next.getName())
											&& itH1_next.getName().equals("this")))) {
								report_error("Semanticka greska 21. Neslaganje tipa parametra " + itH2_next.getName()
										+ " sa parametrom " + itH1_next.getName() + " pri overridingu metode "
										+ symbol.getName(), node);
							}
						}
					} else {
						report_error(
								"Izvrseno nedozvoljeno redeklarisanje identifikatora " + this.currentMethod.getName() + " u klasi " + this.currentClass.getName(),
								node);
					}
					report_info("Prepoznat overriding metoda " + this.currentMethod.getName()
							+ " sa brojem promjenljivih " + this.currentMethod.getLevel(), node);
					break;
				}
			}
		}

		node.obj = this.currentMethod;
		
		if (node.getMethodSignature() instanceof FunctionSignature && !this.containsReturn(node)) {
			report_error("Funkcija ne sadrzi povratnu vrijednost", node);
		}
		
		report_info("Zavrsena obrada funkcije " + this.currentMethod.getName(), node);
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
		if (type_node.getType().getKind() != Struct.Class && type_node.getType().getKind() != Struct.Interface) {
			report_error("Semanticka greska 22. Pokusaj izvodjenja iz neklasnog tipa " + type_node.getName(), node);
			return;
		}
		
		if (type_node.getType().getKind() == Struct.Interface) {
			this.currentClass.getType().addImplementedInterface(type_node.getType());
			report_info("Implementiran interfejs " + node.getType().getTypeName(), node);
			return;
		}
		
		// Ukoliko postoji ispravna superklasa, potrebno je to obiljeziti u cvoru, i
		// prepisati sva POLJA.
		this.currentClass.getType().setElementType(type_node.getType());
		for (Obj symbol : type_node.getType().getMembers()) {
			if (symbol.getKind() == Obj.Fld) {
				Tab.insert(symbol.getKind(), symbol.getName(), symbol.getType());
			}
		}
		// Ako klasa koju nasljedjujemo implementira neke interfejse, treba da ih implementiramo i mi.
		for (Struct interface_ : type_node.getType().getImplementedInterfaces()) {
			this.currentClass.getType().addImplementedInterface(interface_);
		}
		report_info("Naslijedjeni atributi klase " + node.getType().getTypeName(), node);
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
				};
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
		
		for (Struct interface_: this.currentClass.getType().getImplementedInterfaces()) {
			for (Obj method : interface_.getMembers()) {
				if (temp_table.searchKey(method.getName()) == null) {
					if (method.getFpPos() == 0) {
						report_error("Semanticka greska. Nije implementiran metod interfejsa " + method.getName(), node);
					} else {
						temp_table.insertKey(method);
					}
				} else {
					Obj class_method = temp_table.searchKey(method.getName());
					if (class_method.getLevel() != method.getLevel()) {
						report_error("Semanticka greska. Metod interfejsa " +  method.getName() +  " reimplementiran sa drugacijim brojem parametara", node);
					}
					if (class_method.getType() != method.getType()) {
						report_error("Semanticka greska. Metod interfejsa " + method.getName() + " reimplementiran sa drugacijim tipom", node);
					}
					// potrebno je da su svi parametri isti
					
					if (!this.methodIsReimplementationOf(class_method, method)) {
						report_error("Semanticka greska. Neispravna reimplementacija metoda " + method.getName(), node);
					}
				}
			}
		}
		
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
		if (Tab.currentScope.findSymbol(node.getInterfaceName()) != null) {
			report_error("Semanticka greska. Zauzet identifikator u opsegu", node);
			return;
		}
		this.currentInterface = Tab.insert(Obj.Type, node.getInterfaceName(), new Struct(Struct.Interface));
		Tab.openScope();
		report_info("Dodat objektni cvor za interfejs " + node.getInterfaceName(), node);
	}
	
	public void visit(InterfaceDecl node) {
		Tab.chainLocalSymbols(this.currentInterface.getType());
		Tab.closeScope();
		node.obj = this.currentInterface;
		this.currentInterface = null;
		report_info("Zavrsena obrada interfejsa " + node.obj.getName(), node);
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
		report_info("Zatvorena deklaracija metoda " + node.obj.getName(), node);
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

		if (node.getExpr().obj.getType() != Tab.intType) {
			report_error("Semanticka greska 48. Velicina niza nije cijeli broj", node);
			return;
		}

		if (node.getType().struct == setType) {
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
		if (node.getType().struct.getKind() != Struct.Class) {
			report_error("Semanticka greska 49. Tip za koji se alocira prostor nije klasa", node);
			return;
		}
		node.obj = new Obj(Obj.Con, null, node.getType().struct);
	}

	// Samo provjerava da je pronadjeni designator metod ili ne-metod, u skladu sa
	// zagradama.
	public void visit(DesignatorFactor node) {
		node.obj = node.getDesignatorList().obj;
		if (node.getBracedActParsList() instanceof NoBracedActualParametersList) {
			if (node.getDesignatorList().obj.getKind() == Obj.Meth) {
				report_error("Semanticka greska 38. Pokusaj pristupa metodu kao promjenljivoj", node);
			}
		} else {
			if (node.getDesignatorList().obj.getKind() != Obj.Meth) {
				report_error("Semanticka greska 39. Pokusaj upotrebe promjenljive kao funkcije", node);
				if (node.getDesignatorList().obj.getType() == Tab.noType) {
					report_error("Semanticka greska. Procedura se koristi kao da ima povratnu vrijednost", node);
				}
			} else {
				ActParsList parameterList = ((BracedActualParametersList) node.getBracedActParsList()).getActParsList();
				this.checkActualAndFormalParameters(node, node.obj, parameterList);
			}
		}
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
		if (designator.getKind() != Obj.Var && designator.getKind() != Obj.Fld) {
			report_error("Semanticka greska 26. Nizovski pristup identifikatoru " + designator.getName()
					+ " koji nije promjenljiva ni polje klase", node);
			return;
		}
		if (designator.getType().getKind() != Struct.Array) {
			report_error(
					"Semanticka greska 27 prilikom analize designatora. Pokusaj pristupa elementu niza dok promjenljiva nije niz "
							+ designator.getName(),
					node);
			return;
		}
		if (node.getExpr().obj.getType() != Tab.intType) {
			report_error("Semanticka greska 48 prilikom analize designatora. Indeks niza nije tipa int", node);
			return;
		}
		// Ovo je za niz postavljeno u IsArrayVar.
		Struct searched_type = DesignatorIterator.searchType(designator);
		node.obj = new Obj(Obj.Elem, null, searched_type); // treba da dobije objektni cvor tipa niza, a da ga pretvori
															// u objektni cvor

	}

	public void visit(MemberIdentifier node) {
		node.obj = Tab.noObj;
		// postavlja svoj objektni cvor na cvor tipa
		Obj designator = DesignatorIterator.getPreviousDesignator(node);
		if (designator.getKind() != Obj.Var && designator.getKind() != Obj.Fld && designator.getKind() != Obj.Elem) {
			report_error("Semanticka greska 26. Klasni pristup identifikatoru " + designator.getName()
					+ " koji nije promjenljiva, polje klase ni element niza", node);
			return;
		}
		if (designator.getType().getKind() != Struct.Class && designator.getType().getKind() != Struct.Interface) {
			report_error(
					"Semanticka greska 27. prilikom analize designatora. Pokusaj pristupa polju klase dok promjenljiva nije objekat klase "
							+ designator.getName(),
					node);
			return;
		}
		Obj field_node = null;
		if (this.currentClass != null && this.currentClass.getType() == designator.getType()) {
			field_node = Tab.currentScope.getOuter().findSymbol(node.getFieldName());
			//field_node = new Obj(Obj.Fld, field_node.getName(), field_node.getType(), field_node.getAdr(), 0);
		} else {
			field_node = ((HashTableDataStructure) designator.getType().getMembersTable()).searchKey(node.getFieldName());
		}
		if (field_node == null) {
			report_error("Semanticka greska 28. prilikom analize designatora. Nepostojece polje klase "
					+ node.getFieldName(), node);
			return;
		}
		// ovo
		node.obj = field_node;
	}

	public void visit(DesignatorIdentifier node) {
		if (node.obj != null && node.obj != Tab.noObj) {
			report_info("Ugnjezdjeni designatori", node);
		}
		// pronaci objektni cvor zadatog imena.
		Obj obj_node = Tab.find(node.getObjectName());
		int error_detected = 0;
		if (obj_node == Tab.noObj) {
			report_error(
					"Semanticka greska 28. Identifikator " + node.getObjectName() + " nije deklarisan, a koristi se",
					node);
			error_detected++;
		}

		// smije da bude i funkcija i promjenljiva, ali ne program ili tip NA POCETKU
		// moze postati tipski objektni cvor ako se pristupa elementu niza.
		if (obj_node.getKind() != Obj.Var && obj_node.getKind() != Obj.Meth && obj_node.getKind() != Obj.Con && obj_node.getKind() != Obj.Fld) {
			report_error("Semanticka greska 29. Identifikator " + node.getObjectName()
					+ " ne predstavlja nesto cemu se moze pristupiti", node);
			obj_node = Tab.noObj;
			error_detected++;
		}

		// Ako je ispravno, obj_node ce biti postavljen na odgovarajuci objektni cvor, a
		// ako nije, bice postavljen na Tab.noObj, ciji
		// je strukturni cvor noType. Svakako kod u nastavku zbog toga nece puci.
		node.obj = obj_node;
		if (error_detected == 0)
			report_info("Pocetak analize designatora promjenljivom " + node.getObjectName(), node);
	}

	public void visit(DesignatorListEnd node) {
		node.obj = DesignatorIterator.getPreviousDesignator(node);
	}

	// DESIGNATOR STATEMENTS

	public void visit(IncrementDesignatorStatement node) {
		// Designator moze da bude promjenljiva, polje ili tip, ali je vazno da nije
		// metod.
		if (node.getDesignatorList().obj.getKind() == Obj.Meth) {
			report_error("Semanticka greska 30. Rezultat referenciranja je metod, a ne promjenljiva", node);
			return;
		} else if (node.getDesignatorList().obj.getKind() == Obj.Con) {
			report_error("Semanticka greska 40. Pokusaj promjene konstante", node);
			return;
		}
		if (node.getDesignatorList().obj.getType() != Tab.intType) {
			report_error("Semanticka greska 31. Inkrementiranje radi samo sa cjelobrojnim tipovima", node);
			return;
		}
		report_info("Uspjesno inkrementiranje " + node.getDesignatorList().obj.getName(), node);
	}
	
	public void visit(DualIncrementDesignatorStatement node) {
		if (node.getDesignatorList().obj.getKind() == Obj.Meth) {
			report_error("Semanticka greska 30. Rezultat referenciranja je metod, a ne promjenljiva", node);
			return;
		} else if (node.getDesignatorList().obj.getKind() == Obj.Con) {
			report_error("Semanticka greska 40. Pokusaj promjene konstante", node);
			return;
		}
		if (node.getDesignatorList().obj.getType() != Tab.intType) {
			report_error("Semanticka greska 31. Inkrementiranje radi samo sa cjelobrojnim tipovima", node);
			return;
		}
		report_info("Uspjesno inkrementiranje " + node.getDesignatorList().obj.getName(), node);
	}

	public void visit(DecrementDesignatorStatement node) {
		if (node.getDesignatorList().obj.getKind() == Obj.Meth) {
			report_error("Semanticka greska 32. Rezultat referenciranja je metod, a ne promjenljiva", node);
			return;
		} else if (node.getDesignatorList().obj.getKind() == Obj.Con) {
			report_error("Semanticka greska 41. Pokusaj promjene konstante", node);
			return;
		}
		if (node.getDesignatorList().obj.getType() != Tab.intType) {
			report_error("Semanticka greska 33. Dekrementiranje radi samo sa cjelobrojnim tipovima", node);
			return;
		}
		report_info("Uspjesno dekrementiranje " + node.getDesignatorList().obj.getName(), node);
	}
	
	
	

	public void visit(ExprAssignmentDesignatorStatement node) {
		// U nekoj gramatici bi se moglo dozvoliti da bude metod, ako taj metod poslije
		// vraca refType, ali u postavci pise
		// da to ne treba dozvoliti
		if (node.getDesignatorList().obj.getKind() == Obj.Con || node.getDesignatorList().obj.getKind() == Obj.Meth) {
			report_error("Semanticka greska 46. Izraz sa lijeve strane ne moze biti konstanta ni funkcija", node);
			return;
		}
		
		// Sa lijeve strane treba da bude izraz koji se dodjeljuje, a argument je
		// designator kome se dodjeljuje
		// Zato sto se za this provjerava da moze da bude null.
		if (this.errorDetector.assignableTo(node.getExpr().obj.getType(), node.getDesignatorList().obj.getType(), node)) {
			report_info("Uspjesno prepoznat iskaz dodjele", node);
		}
	}

	// TERM

	public void visit(NextFactorExpression node) {
		node.obj = Tab.noObj;

		if (node.getTermNext().obj == null) {
			// u pitanju je epsilon smjena, nema narednog
			// dovoljno je za ovaj provjeriti da li je int, jer ce se za sve ostale
			// provjeravati jednakost tipova.
			if (node.getFactor().obj.getType() != Tab.intType)
				report_error("Semanticka greska 35. Mnozenje tipa koji nije cjelobrojni", node);
		} else if (node.getFactor().obj.getType() != node.getTermNext().obj.getType())
			report_error("Semanticka greska 36. Mnozenje podataka razlicitog tipa", node);

		node.obj = node.getFactor().obj;
	}

	public void visit(Term node) {
		node.obj = Tab.noObj;
		if (node.getTermNext().obj != null && node.getFactor().obj.getType() != node.getTermNext().obj.getType()) {
			// ako next term nije null, ne mora da se radi mnozenje pa ne mora da bude
			// cijeli broj
			report_error("Semanticka greska 37. Mnozenje podataka razlicitog tipa", node);
			return;
		}
		node.obj = node.getFactor().obj;
		report_info("Zavrsen obilazak jednog Term cvora", node);
	}

	// EXPRESSION

	public void visit(ExpressionExists node) {
		node.obj = node.getExpr().obj;
	}

	public void visit(NoExpression node) {
		node.obj = Tab.noObj;
	}

	public void visit(Expression node) {
		if (node.getExprNext().obj != null && node.getTerm().obj.getType() != node.getExprNext().obj.getType())
			report_error("Semanticka greska 42. Sabiranje podataka razlicitog tipa", node);
		node.obj = node.getTerm().obj;
		report_info("Zavrsen obilazak jednog Expression cvora", node);
	}

	public void visit(NegativeExpression node) {
		if (node.getExprNext().obj != null && node.getTerm().obj.getType() != node.getExprNext().obj.getType())
			report_error("Semanticka greska 42. Sabiranje podataka razlicitog tipa", node);
		node.obj = node.getTerm().obj;
		report_info("Zavrsen obilazak jednog NegativeExpression cvora", node);
	}

	public void visit(NextTermExpression node) {
		if (node.getExprNext().obj == null) {
			if (node.getTerm().obj.getType() != Tab.intType)
				report_error("Semanticka greska 43. Sabiranje tipa koji nije cijelobrojni", node);
		} else if (node.getTerm().obj.getType() != node.getExprNext().obj.getType())
			report_error("Semanticka greska 44. Sabiranje podataka razlicitog tipa", node);
		node.obj = node.getTerm().obj;

	}

	// PREDEFINISANI METODI

	public void visit(ReadStatement node) {
		Obj my_designator = node.getDesignatorList().obj;
		if (my_designator.getKind() != Obj.Var && my_designator.getKind() != Obj.Type
				&& my_designator.getKind() != Obj.Fld && my_designator.getKind() != Obj.Elem) {
			report_error("Semanticka greska 629. Read ne sadrzi promjenlijvu, element niza ili polje", node);
		}
		if (my_designator.getType() != Tab.charType && my_designator.getType() != Tab.intType
				&& my_designator.getType() != boolType) {
			report_error("Semanticka greska 632. Read ne sadrzi designator tipa bool, char, int", node);
		}
	}

	public void visit(PrintStatementNoNumber node) {
		Struct my_expr = node.getExpr().obj.getType();
		if (my_expr.getKind() != Struct.Bool && my_expr.getKind() != Struct.Char && my_expr.getKind() != Struct.Int
				&& my_expr.getKind() != 8) {
			report_error("Semanticka greska 639. Print ne sadrzi izraz tipa bool, char, int, set", node);
		}
		report_info("Prepoznat print", node);
	}

	public void visit(PrintStatementYesNumber node) {
		Struct my_expr = node.getExpr().obj.getType();
		int number = node.getNumber();
		if (number < 0) {
			report_error("Broj mora biti pozitivan", node);
		}
		if (my_expr.getKind() != Struct.Bool && my_expr.getKind() != Struct.Char && my_expr.getKind() != Struct.Int
				&& my_expr.getKind() != 8) {
			report_error("Semanticka greska 639. Print ne sadrzi izraz tipa bool, char, int, set", node);
		}
		report_info("Prepoznat print", node);
	}

	// SET

	public void visit(SetAssignmentDesignatorStatement node) {
		Obj dest = node.getDesignatorList().obj;
		if (dest.getKind() != Obj.Var && dest.getKind() != Obj.Elem && dest.getKind() != Obj.Type
				&& dest.getKind() != Obj.Fld) {
			report_error("Sa lijeve strane mora biti promjenlijva", node);
		}
		if (dest.getType() != setType) {
			report_error("Destinaciona promjenlijva mora biti skup", node);
		}
		Obj left = node.getDesignatorList1().obj;
		if (left.getType() != setType) {
			report_error("Lijevi operand skupovne operacije nije skup", node);
		}
		Obj right = node.getDesignatorList2().obj;
		if (right.getType() != setType) {
			report_error("Desni operand skupovne operacije nije skup", node);
		}
	}

	// METHOD CALL

	public void visit(ActParam node) {
		node.obj = node.getExpr().obj;
	}

	private void checkActualAndFormalParameters(SyntaxNode node, Obj method_called, ActParsList parameterList) {
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
					report_error("Semanticka greska. Previse argumenata proslijedjeno", node);
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
				
				if (form_par.getName() == "this") {
					if (parameter_types[0] != null) {
						report_error("Proslijedjen argument na mjesto podrazumijevanog", node);
					}
					continue;
				} else {
					if (parameter_types[form_par.getAdr()] == null) {
						report_error("Parametar " + form_par.getName() + " nedostaje", node);
						continue;
					}
				}
				
				
				if (this.errorDetector.assignableTo(parameter_types[form_par.getAdr()], form_par.getType(), node)) {
					report_info("Parametar " + form_par.getName() + " je odgovarajuceg tipa", node);
				} else {
					report_error("Parametar " + form_par.getName() + " nije odgovarajuceg tipa", node);
				}
			}
		}
		report_info("Zavrsena provjera poklapanja parametara i argumenata za funkciju " + method_called.getName(),
				node);

	}

	public void visit(MethodCallDesignatorStatement node) {
		Obj method_called = node.getDesignatorList().obj;
		if (method_called.getKind() != Obj.Meth) {
			report_error("Semanticka greska. Poziv identifikatora koji nije metod kao metoda", node);
			return;
		}
		ActParsList parameterList = node.getActParsList();
		this.checkActualAndFormalParameters(node, method_called, parameterList);

	}

	// Identicno kao u MethodCallDesignatorStatement treba i za DesignatorFactor

	// Conditional Operations

	public void visit(ConditionFactor node) {
		Obj leftOperand = node.getExpr().obj;
		if (node.getRelExprOrNone() instanceof NoComparisonExpression) {
			if (leftOperand.getType() != SemanticAnalyzer.boolType) {
				report_error("Semanticka greska. Uslovni izraz mora da bude tipa boolean!", node);
			}
		} else {
			Obj rightOperand = ((ComparisonExpression) node.getRelExprOrNone()).getExpr().obj;
			if (!this.errorDetector.compatibleWith(leftOperand.getType(), rightOperand.getType(), node)) {
				report_error("Semanticka greska. Nekompatibilni tipovi u relacionom operatoru", node);
			}
			if (rightOperand.getType().getKind() == Struct.Class || rightOperand.getType().getKind() == Struct.Array) {
				Relop relop = ((ComparisonExpression) node.getRelExprOrNone()).getRelop();
				if (!(relop instanceof IsEqual) && !(relop instanceof IsNotEqual)) {
					report_error("Semanticka greska. Klasni i nizovni tipovi se porede sa znakom razlicitim od == i !=",
							node);
				}
			}
		}
		// u fazi generisanja koda -> kada se naidje na Condition factor, radi se
		// izracunavanje.
	}

	public void visit(BreakStatement node) {
		SyntaxNode current = node;
		while (current != null && !(current instanceof DoWhileStatement)) {
			current = current.getParent();
		}
		if (current == null) {
			report_error("Semanticka greska. Break izraz van Do While petlje", node);
		}
	}
	

	public void visit(BreakNumStatement node) {
		int i = 0;
		int num = node.getNumber();
		SyntaxNode current = node;
		if (num <= 0) {
			report_error("Semanticka greska. Break izraz sa brojem izlaza koji je nepozitivan broj", node);
		}
		while (current != null && i != num) {
			if (current instanceof DoWhileStatement) {
				i++;
			}
			current = current.getParent();
		}
		if (current == null) {
			report_error("Semanticka greska. Break izraz sa brojem izlaza nedovoljjno ugnjezdjen", node);
		}
	}

	public void visit(ContinueStatement node) {
		SyntaxNode current = node;
		while (current != null && !(current instanceof DoWhileStatement)) {
			current = current.getParent();
		}
		if (current == null) {
			report_error("Semanticka greska. Continue izraz van Do While petlje", node);
		}
	}

	public void visit(MapExpression node) {
		DesignatorList function = node.getDesignatorList();
		DesignatorList array = node.getDesignatorList1();
		if (function.obj.getKind() != Obj.Meth) {
			report_error("Semanticka greska. Sa lijeve strane map se ne nalazi funkcija", node);
		}
		if (function.obj.getType() != Tab.intType) {
			report_error("Semanticka greska. Funkcija sa lijeve strane ne vraca cijelobrojnu vrijednost", node);
		}
		if (function.obj.getLevel() != 1) {
			report_error("Semanticka greska. Funkcija sa lijeve strane ne prima tacno jedan argument", node);
		}
		for (Obj parameter : function.obj.getLocalSymbols()) {
			if (parameter.getAdr() == 0) {
				if (parameter.getType() != Tab.intType) {
					report_error("Semanticka greska. Funkcija sa lijeve strane mora imati cijelobrojni parametar",
							node);
				}
				break;
			}
		}

		if (array.obj.getType().getKind() != Struct.Array) {
			report_error("Semanticka greska. Sa desne strane nije niz", node);
		}
		if (array.obj.getType().getElemType() != Tab.intType) {
			report_error("Semanticka greska. Niz sa desne strane nije cijelobrojni", node);
		}

		node.obj = new Obj(Obj.Con, null, Tab.intType);
	}
	
	public void visit(MaxArrayElementDesignatorFactor node) {
		node.obj = Tab.noObj;
		// Potrebno provjeriti da se radi o nizu cijelih brojeva
		Obj argument = node.getDesignatorList().obj;
		if (argument.getType().getKind() != Struct.Array && argument.getKind() != Obj.Elem) {
			report_error("Semanticka greska. Neispravno koriscenje operatora #", node);
			return;
		}
		if (argument.getType().getKind() == Struct.Array && argument.getType().getElemType() != Tab.intType) {
			report_error("Semanticka greska. Operator maksimuma koriscen na nizu koji nije od cijelih brojeva", node);
		}
		node.obj = new Obj(Obj.Con, null, Tab.intType);
	}
	
}
