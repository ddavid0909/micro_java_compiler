package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.mj.runtime.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.ArrayList;
import java.util.HashMap;

import rs.ac.bg.etf.pp1.ast.*;

public class CodeGenerator extends VisitorAdaptor {
	private int mainPc;
	private HashMap<SyntaxNode, ArrayList<Integer>> addresses = new HashMap<>();
	private HashMap<SyntaxNode, Integer> backward_jumps = new HashMap<>();
	private HashMap<Struct, ArrayList<Integer>> needed_vtps = new HashMap<>();
	private HashMap<Struct, Integer> class_vtp = new HashMap<>();
	private Obj program_node;
	int nvars;
	
	public CodeGenerator(int nvars) {
		this.nvars = nvars;
	}
	
	public int getMainPc() {
		return this.mainPc;
	}
	
	public void visit(PrintStatementNoNumber node) {
		Struct node_type = node.getExpr().obj.getType();
		if (node_type == Tab.intType || node_type == SemanticPass_A.boolType) {
			Code.loadConst(5);
			Code.put(Code.print);
		} else if (node_type == Tab.charType) {
			Code.loadConst(5);
			Code.put(Code.bprint);
		} else if (node_type == SemanticPass_A.setType) {
			Obj print_node = Tab.currentScope.findSymbol("print");
			int offset = print_node.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
			
		}
	}
	
	public void visit(PrintStatementYesNumber node) {
		int width = node.getNumber();
		Code.loadConst(width);
		Struct node_type = node.getExpr().obj.getType();
		if (node_type == Tab.intType || node_type == SemanticPass_A.boolType) {
			Code.put(Code.print);
		} else if (node_type == Tab.charType) {
			Code.put(Code.bprint);
		} else if (node_type == SemanticPass_A.setType) {
			// ne obradjuje se ovaj load.
			Code.put(Code.pop);
			Obj print_node = Tab.currentScope.findSymbol("print");
			int offset = print_node.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
		}
	}
	
	
	private void methodStart(Obj methNode) {
		
		methNode.setAdr(Code.pc);
		if (methNode.getName().equalsIgnoreCase("main")) {
			this.mainPc = Code.pc;
		}
		
		int parameters = methNode.getLevel();
		int altogether = methNode.getLocalSymbols().size();
		Code.put(Code.enter);
		Code.put(parameters);
		Code.put(altogether);
		if (methNode.getName().equalsIgnoreCase("main")) {
			this.initializeVTPs();
			}
		//Code.put(Code.call);
	}
	
	private void methodStart(Obj methNode, int additional) {
		
		methNode.setAdr(Code.pc);
		if (methNode.getName().equalsIgnoreCase("main")) {
			this.mainPc = Code.pc;
		}
		
		int parameters = methNode.getLevel();
		int altogether = parameters + additional;
		Code.put(Code.enter);
		Code.put(parameters);
		Code.put(altogether);
		//Code.put(Code.call);
	}
	
	public void visit(ProcedureTypeName node ) {
		Obj methNode = node.obj;
		this.methodStart(methNode);
		
	}
	
	private void addOrd() {
		Obj ordNode = Tab.currentScope.findSymbol("ord");
		this.methodStart(ordNode);
		Code.put(Code.load);
		Code.put(0);
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	private void addChr() {
		Obj chrNode = Tab.currentScope.findSymbol("chr");
		this.methodStart(chrNode);
		Code.put(Code.load);
		Code.put(0);
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	private void addPrint() {
		Obj printNode = Tab.currentScope.findSymbol("print");
		this.methodStart(printNode, 2);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_n+0);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.aload);
		Code.put(Code.store_1);
		
		Code.loadConst(0);
		Code.put(Code.store_2);
		int iteration_start = Code.pc;
		
		Code.put(Code.load_1);
		Code.put(Code.load_2);
		Code.put(Code.jcc + Code.eq);
		int insert_finish = Code.pc;
		
		Code.put2(0);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_2);
		Code.put(Code.aload);
		
		Code.loadConst(1);
		Code.put(Code.print);
		
		Code.loadConst(32);
		Code.loadConst(1);
		Code.put(Code.bprint);
		
		Code.put(Code.load_2);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store_2);
		Code.putJump(iteration_start);
		Code.fixup(insert_finish);
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	private void addAddAll() {
		Obj addAllNode = Tab.currentScope.findSymbol("addAll");
		Obj addNode = Tab.currentScope.findSymbol("add");
		this.methodStart(addAllNode, 2);
		
		Code.put(Code.load_1);
		Code.put(Code.arraylength);
		this.arr_access_space_removal();
		Code.put(Code.store_2);
		
		Code.loadConst(0);
		Code.put(Code.store_3);
		
		int iteration_start = Code.pc;
		Code.put(Code.load_2);
		Code.put(Code.load_3);
		Code.put(Code.jcc + Code.eq);
		int insert_finish_address = Code.pc;
		Code.put2(0);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_1);
		Code.put(Code.load_3);
		Code.put(Code.aload);
		
		int offset = addNode.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
		Code.put(Code.load_3);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store_3);
		Code.putJump(iteration_start);
		Code.fixup(insert_finish_address);
		Code.put(Code.exit);
		Code.put(Code.return_);
		
	}
	
	private void addRemove() {
		Obj removeNode = Tab.currentScope.findSymbol("remove");
		this.methodStart(removeNode, 3);
		Code.put(Code.load_n+0);
		Code.put(Code.load_n+0);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.aload);
		Code.put(Code.store_2); // u promjenlijvu dva se smjesta velicina skupa
		
		Code.loadConst(-1);
		Code.put(Code.store_3); // brojac
		
		int iteration_start = Code.pc;
		Code.put(Code.load_3);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store_3);
		
		Code.put(Code.load_3); // brojac
		Code.put(Code.load_2); // velicina skupa
		Code.put(Code.jcc + Code.ge); // Ako je jednak ili veci brojac, onda je potrebno izaci
		int put_exit_address = Code.pc;
		Code.put2(0);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_3);
		Code.put(Code.aload);
		Code.put(Code.load_1);
		Code.putFalseJump(Code.eq, iteration_start);
		
		// ako jesu jednaki, onda je potrebno swap sa poslednjim elementom i velicinu dekrementirati
		Code.put(Code.load_n+0);
		Code.put(Code.load_2);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.aload);
		Code.put(Code.store); // u cetiri je krajji element
		Code.put(4);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_3);
		Code.put(Code.load);
		Code.put(4);
		
		Code.put(Code.astore); // Cuvanje krajnjeg elementa na poziciju koja se brise
		
		Code.put(Code.load_2);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.store_2);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_n+0);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.load_2);
		Code.put(Code.astore);
		
		Code.fixup(put_exit_address);
		
		Code.put(Code.exit);
		Code.put(Code.return_);
		
	}

	
	private void addAdd() {
		Obj addNode = Tab.currentScope.findSymbol("add");
		this.methodStart(addNode, 3);
		
		// load variable 0 -> reference to set
		Code.put(Code.load_n+0);
		// find the length of the set
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.store);
		Code.put(2); // variable number two stores the length of the set (occupied spots)
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_2);
		Code.put(Code.aload);
		
		Code.put(Code.store);
		Code.put(3);
		
		Code.put(Code.load_2);
		Code.put(Code.load_3);
		Code.put(Code.jcc + Code.eq);
		int insert_finish_label_address = Code.pc;
		Code.put2(0);
		Code.loadConst(0);
		Code.put(Code.store);
		Code.put(4);
		
		int new_iter_address = Code.pc;
		
		Code.put(Code.load);
		Code.put(4);
		Code.put(Code.load_3);
		
		Code.put(Code.jcc + Code.eq);
		int insert_set_addition_address = Code.pc;
		Code.put2(0);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load);
		Code.put(4);
		Code.put(Code.aload);
		Code.put(Code.load_1);
		Code.put(Code.jcc + Code.eq);
		int insert_finish_label = Code.pc;
		Code.put2(0);
		
		Code.put(Code.load);
		Code.put(4);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store);
		Code.put(4);
		Code.putJump(new_iter_address);

		Code.fixup(insert_set_addition_address);
		Code.put(Code.load_n + 0);
		Code.put(Code.load_3);
		Code.put(Code.load_1);
		Code.put(Code.astore);
		
		Code.put(Code.load_n + 0);
		Code.put(Code.load_2);
		Code.put(Code.load_3);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.astore);
		
		

		Code.fixup(insert_finish_label);
		Code.fixup(insert_finish_label_address);
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	private void addIntersect() {
		Obj intersectNode = Tab.currentScope.findSymbol("intersect");
		Obj addNode = Tab.currentScope.findSymbol("add");
		this.methodStart(intersectNode, 4);
		
		// Velicina destinacionog skupa se nulira
		Code.put(Code.load_n+0);
		Code.put(Code.load_n+0);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.loadConst(0);
		Code.put(Code.astore);
		
		// Ovo je pozicija u finalnom skupu.
		Code.loadConst(-1);
		Code.put(Code.store_3);
		
		// Brojac za lijevi
		Code.loadConst(-1);
		Code.put(Code.store);
		Code.put(4);
		
		// Provjere za kraj iterisanja
		int iteration_start = Code.pc;
		Code.put(Code.load_3);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store_3);
		
		Code.put(Code.load);
		Code.put(4);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store);
		Code.put(4);
		
		Code.put(Code.load_n+0);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.load_3);
		Code.put(Code.jcc+Code.le);
		int put_exit_address = Code.pc;
		Code.put2(0);
		
		Code.put(Code.load_1);
		Code.put(Code.load_1);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.aload);
		Code.put(Code.load);
		Code.put(4);
		Code.put(Code.jcc+Code.le);
		int put_exit_address_1 = Code.pc;
		Code.put2(0);
		
		Code.put(Code.load_1);
		Code.put(Code.load);
		Code.put(4);
		Code.put(Code.aload);
		Code.put(Code.store);
		Code.put(5); // peta promjenljiva - trenutni element
		
		// Brojac za desni
		Code.loadConst(-1);
		Code.put(Code.store);
		Code.put(6);
		int subiteration_start = Code.pc;
		
		Code.put(Code.load);
		Code.put(6);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store);
		Code.put(6);
		
		
		Code.put(Code.load_2);
		Code.put(Code.load_2);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.aload);
		Code.put(Code.load);
		Code.put(6);
		Code.putFalseJump(Code.gt, iteration_start);
		
		// ako su brojevi jednaki dodaj
		
		Code.put(Code.load_2);
		Code.put(Code.load);
		Code.put(6);
		Code.put(Code.aload);
		Code.put(Code.load);
		Code.put(5);
		Code.putFalseJump(Code.eq, subiteration_start);
		Code.put(Code.load_n+0);
		Code.put(Code.load);
		Code.put(5);
		int offset = addNode.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		Code.putJump(iteration_start);
		
		Code.fixup(put_exit_address);
		Code.fixup(put_exit_address_1);
		
		Code.put(Code.exit);
		Code.put(Code.return_);
		
		
	}
	
	private void addUnion() {
		Obj unionNode = Tab.currentScope.findSymbol("union");
		Obj addNode = Tab.currentScope.findSymbol("add");
		this.methodStart(unionNode, 2);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_n+0);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.loadConst(0);
		Code.put(Code.astore);
		
		Code.put(Code.load_1);
		Code.put(Code.load_1);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.aload);
		Code.put(Code.store_3);
		Code.loadConst(0);
		Code.put(Code.store);
		Code.put(4);
		
		int left_iteration = Code.pc;
		Code.put(Code.load_3);
		Code.put(Code.load);
		Code.put(4);
		Code.put(Code.jcc+Code.eq);
		int fill_right_iteration = Code.pc;
		Code.put2(0);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_1);
		Code.put(Code.load);
		Code.put(4);
		Code.put(Code.aload);
		
		int offset = addNode.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
		Code.put(Code.load);
		Code.put(4);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store);
		Code.put(4);
		
		Code.putJump(left_iteration);
		
		Code.fixup(fill_right_iteration);
		
		Code.put(Code.load_2);
		Code.put(Code.load_2);
		Code.put(Code.arraylength);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.put(Code.aload);
		Code.put(Code.store_3);
		Code.loadConst(0);
		Code.put(Code.store);
		Code.put(4);
		
		int right_iteration = Code.pc;
		Code.put(Code.load_3);
		Code.put(Code.load);
		Code.put(4);
		Code.put(Code.jcc+Code.eq);
		int fill_end = Code.pc;
		Code.put2(0);
		
		Code.put(Code.load_n+0);
		Code.put(Code.load_2);
		Code.put(Code.load);
		Code.put(4);
		Code.put(Code.aload);
		
		offset = addNode.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
		Code.put(Code.load);
		Code.put(4);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.store);
		Code.put(4);
		
		Code.putJump(right_iteration);
		
		Code.fixup(fill_end);
		Code.put(Code.exit);
		Code.put(Code.return_);
		
	}
	
	
	public void visit(Program node) {
		System.out.println(this.addresses.size());
		// Backward jumps nije automatski uklonjen jer kad cvor A postavi adresu, nikad se ne zna ko sve treba da je vidi
		// dok je za addresses lakse, jer tad cvor postavi svima adresu i zavrsi.
		this.backward_jumps.clear();
		System.out.println(this.backward_jumps.size());
		
	}
	
	public void visit(ProgName node) {
		this.addOrd();
		this.addChr();
		this.addAdd();
		this.addPrint();
		this.addAddAll();
		this.addUnion();
		this.addIntersect();
		this.addRemove();
		this.program_node = node.obj;
		
	}
	
	public void visit(FunctionTypeName node) {
		Obj methNode = node.obj;
		this.methodStart(methNode);
	}
	
	public void visit(MethodDeclaration node) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ExprAssignmentDesignatorStatement node) {
		Obj dest = node.getDesignatorList().obj;
		Code.store(dest);
	}
	
	private void callFunction(Obj function_node) {
		if (function_node.getFpPos() == 1) {
			// invoke virtual
			Code.put(Code.getfield);
			Code.put2(0);
			Code.put(Code.invokevirtual);
			for (int i = 0 ; i < function_node.getName().length(); i++) {
				Code.put4(function_node.getName().charAt(i));
			}
			Code.put4(-1);
		}
		else {
			if (function_node.getName() == "len") {
				Code.put(Code.arraylength);
				this.arr_access_space_removal();
				return;
			}
			int offset = function_node.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
		}
	}
	
	public void visit(DesignatorFactor node) {
		if (node.getBracedActParsList() instanceof BracedActualParametersList) {
			// Act Params moraju biti stavljeni na stek vec
			Obj functionObj = node.obj;
			if (functionObj.getFpPos() == 1) {
				// invoke virtual
				Code.put(Code.getfield);
				Code.put2(0);
				Code.put(Code.invokevirtual);
				for (int i = 0 ; i < functionObj.getName().length(); i++) {
					Code.put4(functionObj.getName().charAt(i));
				}
				Code.put4(-1);
				return;
			}
			if (functionObj.getName() == "len") {
				Code.put(Code.arraylength);
				this.arr_access_space_removal();
				return;
			}
			int offset = functionObj.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
		} else {
			// ovo je slucaj promjenljivih.
			Code.load(node.obj);
			this.checkOperation(node);
		}
		
	}
	private void checkOperation(Factor node) {
		SyntaxNode parent = node.getParent();
		if (parent instanceof NextFactorExpression) {
			Mulop op = ((NextFactorExpression)parent).getMulop();
			if (op instanceof Multiplication) {
				Code.put(Code.mul);
			} else if (op instanceof Division) {
				Code.put(Code.div);
			} else if (op instanceof Remainder) {
				Code.put(Code.rem);
			}
		}
	}
	
	public void visit(NumberFactor node) {
		Code.load(node.obj);
		this.checkOperation(node);
	}
	
	public void visit(BooleanFactor node) {
		Code.load(node.obj);
	}
	
	public void visit(CharacterFactor node) {
		Code.load(node.obj);
	}
	
	public void visit(Term node) {
		SyntaxNode parent = node.getParent();
		if (parent instanceof NextTermExpression) {
			Addop op = ((NextTermExpression)parent).getAddop();
			if (op instanceof Addition) {
				Code.put(Code.add);
			}
			else if (op instanceof Subtraction) {
				Code.put(Code.sub);
			}
		} else  if (parent instanceof NegativeExpression) {
			Code.loadConst(-1);
			Code.put(Code.mul);
		}
	}
	
	
	public void visit(MethodCallDesignatorStatement node) {
		Obj method = node.getDesignatorList().obj;

		if (method.getFpPos() == 1) {
			// invoke virtual
			Code.put(Code.getfield);
			Code.put2(0);
			Code.put(Code.invokevirtual);
			for (int i = 0 ; i < method.getName().length(); i++) {
				Code.put4(method.getName().charAt(i));
			}
			Code.put4(-1);
			return;
		}
		
		if (method.getName() == "len") {
			Code.put(Code.arraylength);
			this.arr_access_space_removal();
			return;
		}
		int offset = method.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		if (method.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
	}
	
	public void visit(AllocateVariableFactor node) {
		Code.put(Code.new_);
		Code.put2(node.obj.getType().getNumberOfFields()*4);
		
		Code.put(Code.dup);
		if (this.class_vtp.containsKey(node.obj.getType())) {
			Code.loadConst(this.class_vtp.get(node.obj.getType()));
		} else {
			Code.put(Code.const_); 
			if (!this.needed_vtps.containsKey(node.obj.getType())) {
				this.needed_vtps.put(node.obj.getType(), new ArrayList<>());
			}
			this.needed_vtps.get(node.obj.getType()).add(Code.pc);
			Code.put4(0);
		}
		// a adresa se sacuva u hes tabelu
		Code.put(Code.putfield);
		Code.put2(0);
		
	}
	
	
	public void visit(AllocateArrayFactor node) {
		int b = 1;
		if (node.obj.getType().getKind() == Struct.Array && (node.obj.getType().getElemType() != Tab.intType && node.obj.getType().getElemType().getKind() != Struct.Class)) {
			b = 0;
		}
		
		if (node.obj.getType().getKind() == Struct.Array) {
			Code.loadConst(2);
			Code.put(Code.mul);
		}
		
		if (node.obj.getType() == SemanticPass_A.setType) {
			Code.loadConst(1);
			Code.put(Code.add);
		}
		Code.put(Code.newarray);
		Code.put(b);
		
	}
	
	public void visit(DecrementDesignatorStatement node) {
		if (node.getDesignatorList().obj.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		if (node.getDesignatorList().obj.getKind() == Obj.Fld) {
			Code.put(Code.dup);
		}
		Code.load(node.getDesignatorList().obj);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.store(node.getDesignatorList().obj);
	}
	
	public void visit(IncrementDesignatorStatement node) {
		if (node.getDesignatorList().obj.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		if (node.getDesignatorList().obj.getKind() == Obj.Fld) {
			Code.put(Code.dup);
		}
		Code.load(node.getDesignatorList().obj);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(node.getDesignatorList().obj);
	}
	
	public void visit(DualIncrementDesignatorStatement node) {
		if (node.getDesignatorList().obj.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		if (node.getDesignatorList().obj.getKind() == Obj.Fld) {
			Code.put(Code.dup);
		}
		Code.load(node.getDesignatorList().obj);
		Code.loadConst(2);
		Code.put(Code.add);
		Code.store(node.getDesignatorList().obj);
		
	}
	
	public void visit(ReadStatement node) {
		if (node.getDesignatorList().obj.getType() == Tab.charType) {
			Code.put(Code.bread);
		} else {
			Code.put(Code.read);
		}
		Code.store(node.getDesignatorList().obj);
	}
	
	public void visit(BracketedExpressionFactor node) {
		this.checkOperation(node);
	}
	
	private int selectRelOp(Relop op) {
		if (op instanceof IsEqual) {
			return Code.eq;
		} else if (op instanceof IsNotEqual) {
			return Code.ne;
		} else if (op instanceof IsGreaterThan) {
			return Code.gt;
		} else if (op instanceof IsLessThan) {
			return Code.lt;
		} else if (op instanceof IsGreaterThanOrEqual) {
			return Code.ge;
		} else if (op instanceof IsLessThanOrEqual) {
			return Code.le;
		}
		return 0;
	}
	
	private void add_to_addresses(SyntaxNode node, int pc) {
		if (!this.addresses.containsKey(node)) {
			this.addresses.put(node, new ArrayList<>());
		}
		this.addresses.get(node).add(pc);
	}
	
	private void update_addresses(SyntaxNode node) {
		if (this.addresses.containsKey(node)) {
			ArrayList<Integer> current_addresses = this.addresses.remove(node);
			for (Integer address: current_addresses) {
				Code.fixup(address);
			}
		}
	}
	
	public OrConditionList get_or(SyntaxNode node) {
		while (!(node instanceof ConditionTermsList) && !(node instanceof OrConditions)) {
			node = node.getParent();
		}
		if (node instanceof ConditionTermsList) {
			return ((ConditionTermsList)node).getOrConditionList();
		} else if (node instanceof OrConditions) {
			return ((OrConditions)node).getOrConditionList();
		} 
		return null;
	}
	
	public AndConditionList get_and(SyntaxNode node) {
		while (!(node instanceof ConditionTerm) && !(node instanceof AndConditions)) {
			node = node.getParent();
		}
		if (node instanceof ConditionTerm) {
			return ((ConditionTerm)node).getAndConditionList();
		} else if (node instanceof AndConditions) {
			return ((AndConditions)node).getAndConditionList();
		} 
		return null;
	}
	
	public Statement get_statement(SyntaxNode node) {
		while (!(node instanceof Statement)) {
			node = node.getParent();
		}
		return (Statement)node;
	}
	
	public void visit(ConditionFactor node) {
		// Trebalo bi da su izrazi vec na steku pri obilasku
		//Obj leftOperand = node.getExpr().obj;
		Obj rightOperand = node.getRelExprOrNone() instanceof NoComparisonExpression ? null : ((ComparisonExpression)node.getRelExprOrNone()).getExpr().obj;
		int relop;
		if (rightOperand != null) {
			relop = this.selectRelOp(((ComparisonExpression)node.getRelExprOrNone()).getRelop());
		} else {
			Code.loadConst(1);
			relop = Code.eq;
		}
		
		OrConditionList or_condition = this.get_or(node);
		AndConditionList and_condition = this.get_and(node);
		SyntaxNode jump = null;
		Statement statement = this.get_statement(node);
		if (statement instanceof IfStatement || statement instanceof IfElseStatement) {
		// if is last term or is not last condition
		if (or_condition instanceof NoOrConditions || !(and_condition instanceof NoAndConditions)) {
			relop = Code.inverse[relop];
			if (or_condition instanceof OrConditions ) {
				// jump to next cond term
				jump = ((OrConditions)or_condition).getCondTerm();
			}
			if (statement instanceof IfStatement) {
				jump = statement;
			} else {
				jump = ((IfElseStatement)statement).getElseStart();
			}
		} else {
			if (statement instanceof IfStatement) {
				jump = ((IfStatement)statement).getIfStart();
			} else {
				jump = ((IfElseStatement)statement).getIfStart();
			}
		}
		//System.out.println(Code.pc + " last term:" + (or_condition instanceof NoOrConditions) + " last factor:" + (and_condition instanceof NoAndConditions));
		}
		else if (statement instanceof DoWhileStatement){
			SyntaxNode success = this.findDoWhileSuccessJump(node);
			SyntaxNode failure = this.findDoWhileFailJump(node);
			if (or_condition instanceof NoOrConditions) {
				relop = Code.inverse[relop];
				jump = this.findDoWhileFailJump(node);
		}
			else {
				if (and_condition instanceof NoAndConditions) {
					jump = this.findDoWhileSuccessJump(node);
				} else {
					jump = ((OrConditions)or_condition).getCondTerm();
				}
			}
		}
		Code.put(Code.jcc + relop);
		this.add_to_addresses(jump, Code.pc);
		Code.put2(0);
	}
	
	private SyntaxNode findDoStart(SyntaxNode node) {
		while (!(node instanceof DoWhileStatement)) {
			node = node.getParent();
		}
		return ((DoWhileStatement)node).getDoStart();
	}
	
	private SyntaxNode findDoWhileSuccessJump(SyntaxNode node) {
		while (!(node instanceof DoWhileStatement)) {
			node = node.getParent();
		}
		node = ((DoWhileStatement)node).getDoWhileConditionList();
		if (node instanceof MoreDoWhileConditions) {
			return ((MoreDoWhileConditions)node).getDoWhileSuccess();
		}
		return node;
	}
	
	private SyntaxNode findDoWhileFailJump(SyntaxNode node) {
		while (!(node instanceof DoWhileStatement)) {
			node = node.getParent();
		}
		return node;
	}
	
	private SyntaxNode findDoWhileFailJump(SyntaxNode node, int num) {
		while (num > 0) {
			node = node.getParent();
			while (!(node instanceof DoWhileStatement)) {
				node = node.getParent();
			}
			num--;
		}
		return node;
	}
	
	public void visit(ContinueStatement node) {
		Code.put(Code.jmp);
		SyntaxNode answer = this.findDoWhileSuccessJump(node);
		this.add_to_addresses(answer, Code.pc);
		Code.put2(0);
	}
	
	public void visit(BreakStatement node) {
		Code.put(Code.jmp);
		SyntaxNode answer = this.findDoWhileFailJump(node);
		this.add_to_addresses(answer, Code.pc);
		Code.put2(0);
	}
	
	public void visit(BreakNumStatement node) {
		Code.put(Code.jmp);
		SyntaxNode answer = this.findDoWhileFailJump(node, node.getNumber());
		this.add_to_addresses(answer, Code.pc);
		Code.put2(0);
	}
	
	public void visit(DoStart node) {
		this.backward_jumps.put(node, Code.pc);
	}
	
	public void visit(IfStart node) {
		this.update_addresses(node);
	}
	
	public void visit(CondTermStart node) {
		this.update_addresses(node);
	}
	
	public void visit(ElseStart node) {
		Code.put(Code.jmp);
		this.add_to_addresses(node.getParent(), Code.pc);
		Code.put2(0);
		this.update_addresses(node);
	}
	
	public void visit(IfStatement node) {
		this.update_addresses(node);
	}
	
	public void visit(IfElseStatement node) {
		this.update_addresses(node);
	}
	
	public void visit(DoWhileStatement node) {
		this.update_addresses(node);
	}
	
	public void visit(OneDoWhileCondition node) {
		this.update_addresses(node);
		SyntaxNode jump = this.findDoStart(node);
		Code.putJump(this.backward_jumps.get(jump));
	}
	
	public void visit(MoreDoWhileConditions node) {
		SyntaxNode jump = this.findDoStart(node);
		Code.putJump(this.backward_jumps.get(jump));
	}
	
	public void visit(NoDoWhileCondition node) {
		this.update_addresses(node);
		SyntaxNode jump = this.findDoStart(node);
		Code.putJump(this.backward_jumps.get(jump));
	}
	
	public void visit(DoWhileSuccess node) {
		this.update_addresses(node);
	}
	
	
	public void visit(SetAssignmentDesignatorStatement node) {
		Obj dest = node.getDesignatorList().obj;
		Obj left = node.getDesignatorList1().obj;
		Obj right = node.getDesignatorList2().obj;
		
		Code.load(dest);
		Code.load(left);
		Code.load(right);
		// TO-DO treba da prepozna koja je operacija u pitanju.
		Obj setop_node = Tab.noObj;
		if (node.getSetop() instanceof Union) {
			setop_node = Tab.currentScope.findSymbol("union");
		} else if (node.getSetop() instanceof Intersect) {
			setop_node = Tab.currentScope.findSymbol("intersect");
		}
		int offset = setop_node.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
	}
	
	private void arr_access_space_removal() {
		Code.loadConst(2);
		Code.put(Code.div);
	}
	
	private void arr_index_check() {
		Code.put(Code.dup2);
		Code.put(Code.dup2);
		Code.put(Code.pop);
		Code.put(Code.arraylength);
		this.arr_access_space_removal();
		
		Code.put(Code.jcc+Code.lt);
		int addr = Code.pc;
		Code.put2(0);
		Code.put(Code.trap);
		Code.put(1);
		Code.fixup(addr);
		Code.put(Code.pop);
		
	}
	
	public void visit(MapExpression node) {
		Obj arr = node.getDesignatorList1().obj;
		Obj meth = node.getDesignatorList().obj;
		
		Code.loadConst(0);
		Code.loadConst(0);
		int return_pc = Code.pc;
		Code.put(Code.dup);
		Code.put(Code.dup);
		Code.load(arr);
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		Code.load(arr);
		Code.put(Code.arraylength);
		this.arr_access_space_removal();
		Code.put(Code.jcc + Code.eq);
		int fill_with_end = Code.pc;
		Code.put2(0);
		Code.put(Code.aload);
		
		int offset = meth.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		Code.loadConst(1);
		Code.put(Code.add);
		
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		Code.put(Code.add);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		
		Code.putJump(return_pc);
		Code.fixup(fill_with_end);
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.put(Code.pop);
		
	}
	
	private void detect_arr_access() {
		 Code.put(Code.dup2);
		 Code.put(Code.dup2);
		 Code.put(Code.pop);
		 Code.put(Code.arraylength);
		 this.arr_access_space_removal();
		 Code.put(Code.add);
		 Code.put(Code.dup2);
		 Code.put(Code.aload);
		 Code.loadConst(1);
		 Code.put(Code.add);
		 Code.put(Code.astore);
	}
	
	private boolean isAccess(SyntaxNode node) {
		while (node != null && ! (node instanceof Factor)) {
			node = node.getParent();
		}
		if (node == null) return true;
		if (node instanceof MaxArrayElementDesignatorFactor) return false;
		return true;
	}
	
	public void visit(ArrayIdentifier node) {
		this.arr_index_check();
		if (this.isAccess(node)) this.detect_arr_access();
		ArrayElementAccess parent = (ArrayElementAccess)(node.getParent());
		if (parent.getDesignatorNext() instanceof DesignatorListEnd) return;
		Code.load(node.obj);
		
		
	}
	 
	public void visit(MemberIdentifier node) {
		ClassFieldAccess parent = (ClassFieldAccess)(node.getParent());
		if (parent.getDesignatorNext() instanceof DesignatorListEnd) return;
		Code.load(node.obj);
	}
	
	public void visit(DesignatorIdentifier node) {
		// ako se pristupa polju tipa niza direktno potrebno je oboje - prvi if dodaje adresu this-a ako se koristi bez navodjenja this, a drugi zapocinje pristup elementu na heapu.
		if (node.obj.getKind() == Obj.Fld || (node.obj.getKind() == Obj.Meth && node.obj.getFpPos() == 1)) {
			Code.put(Code.load_n+0);
		}
		if (node.obj.getType().getKind() == Struct.Class || node.obj.getType().getKind() == Struct.Array || node.obj.getType().getKind() == Struct.Interface) {
			if (((DesignatorList)node.getParent()).getDesignatorNext() instanceof DesignatorListEnd) return;
			Code.load(node.obj);
		}
	}
	
	public void visit(ClassDeclaration node) {
		Struct class_node = node.obj.getType();
		Struct base_class_node = class_node.getElemType();
		if (base_class_node == null || base_class_node == Tab.noType) {
			return;
		}
		var class_members = class_node.getMembersTable();
		var base_class_members = base_class_node.getMembers();
		for (Obj base_class_member : base_class_members) {
			if (base_class_member.getKind() == Obj.Meth) {
				Obj class_meth = class_members.searchKey(base_class_member.getName());
				if (class_meth == null) {
					System.out.println("Fatalna greska 900 : nepostojeci element iz osnovne klase");
					return;
				}
				if (class_meth.getAdr() == 0) {
					class_meth.setAdr(base_class_member.getAdr());
				}
				
			}
		}
	}
	
	private Obj findCallInitiator(SyntaxNode start) {
		SyntaxNode node = start;
		while (node != null && !(node instanceof DesignatorFactor) && !(node instanceof MethodCallDesignatorStatement)) {
			node = node.getParent();
		}
		if (node == null) {
			System.out.println("Fatalna greska 900");
			return null;
		}
		
		if (node instanceof DesignatorFactor) {
			return ((DesignatorFactor)node).obj;
		}
		if (node instanceof MethodCallDesignatorStatement) {
			return ((MethodCallDesignatorStatement)node).getDesignatorList().obj;
		}
		return null;
	}
	
	public void visit(ActParam node) {
		Obj method = this.findCallInitiator(node);
		if (method == null) return;
		if (method.getFpPos() == 1) {
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
		}
	}
	
	public void visit(DesignatorList node) {
		if (node.obj.getKind() == Obj.Meth && node.obj.getFpPos() == 1) {
			Code.put(Code.dup);
		}
	}
	
	private void initializeVTPs() {
		for (Obj symbol : this.program_node.getLocalSymbols()) {
			if (symbol.getType().getKind() == Struct.Class && symbol.getName() != "null") {
				Obj vtp_node = symbol.getType().getMembersTable().searchKey("VTP");
				
				ArrayList<Integer> vtp_addresses = this.needed_vtps.get(symbol.getType());
				if (vtp_addresses != null) {
					for (Integer vtp_address : vtp_addresses) {
						Code.put2(vtp_address, this.nvars >> 16);
						Code.put2(vtp_address+2, this.nvars);
					}
				}
				this.class_vtp.put(symbol.getType(), this.nvars);
				
				for (Obj element: symbol.getType().getMembers()) {
					if (element.getKind() == Obj.Meth) {
						for (int i = 0 ; i < element.getName().length(); i++) {
							Code.loadConst(element.getName().charAt(i));
							Code.put(Code.putstatic);
							Code.put2(this.nvars++);
						}
						Code.loadConst(-1);
						Code.put(Code.putstatic);
						Code.put2(this.nvars++);
						Code.loadConst(element.getAdr());
						Code.put(Code.putstatic);
						Code.put2(this.nvars++);
					}
				}
				Code.loadConst(-2);
				Code.put(Code.putstatic);
				Code.put2(this.nvars++);
				
			}
			
		}
	}
	
	public void visit(InterfaceMethodSignature node) {
		node.obj.setFpPos(1);
	}
	
	private void arrMaxOperator(MaxArrayElementDesignatorFactor node) {
		Code.load(node.getDesignatorList().obj);
		Code.put(Code.dup);
		Code.put(Code.arraylength);
		this.arr_access_space_removal();
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.put(Code.aload);
		// Ucitan maksimum
		
		Code.load(node.getDesignatorList().obj);
		Code.put(Code.arraylength);
		this.arr_access_space_removal();
		Code.loadConst(1);
		Code.put(Code.sub);
		// Ucitan brojac
		
		int iteration_address = Code.pc;
		Code.loadConst(1);
		Code.put(Code.sub);
		
		Code.put(Code.dup);
		Code.loadConst(-1);
		Code.put(Code.jcc + Code.eq);
		int put_end_address = Code.pc;
		Code.put2(0);
		
		Code.put(Code.dup_x1);
		
		Code.load(node.getDesignatorList().obj);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.put(Code.aload);
		
		Code.put(Code.dup2);
		
		Code.put(Code.jcc+Code.gt);
		int put_old_maximum_address = Code.pc;
		Code.put2(0);
		
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.fixup(put_old_maximum_address);
		Code.put(Code.pop);
		
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		
		Code.putJump(iteration_address);
		Code.fixup(put_end_address);
		
		Code.put(Code.pop);
	}
	
	private void accessCount() {
		Code.put(Code.dup2);
		Code.put(Code.pop);
		Code.put(Code.arraylength);
		this.arr_access_space_removal();
		Code.put(Code.add);
		Code.put(Code.aload);
	}
	
	public void visit(MaxArrayElementDesignatorFactor node) {
		if (node.getDesignatorList().obj.getType().getKind() == Struct.Array) {
			this.arrMaxOperator(node);
			return;
		}
		if (node.getDesignatorList().obj.getKind() == Obj.Elem) {
			this.accessCount();
			return;
		}
		
		
		
	}
	
}
