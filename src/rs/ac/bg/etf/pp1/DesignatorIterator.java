package rs.ac.bg.etf.pp1;



import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.structure.*;

public class DesignatorIterator {

	public static Obj getPreviousDesignator(ArrayIdentifier node) {
		 SyntaxNode parent = node.getParent().getParent();
		 if (parent instanceof DesignatorList) {
			 return ((DesignatorList)parent).getDesignatorIdentifier().obj;
		 }
		 if (parent instanceof ClassFieldAccess) {
			 return ((ClassFieldAccess)parent).getMemberIdentifier().obj;
		 }
		 return null;
	 }
	
	public static Obj getPreviousDesignator(MemberIdentifier node) {
		 SyntaxNode parent = node.getParent().getParent();
		 if (parent instanceof DesignatorList) {
			 return ((DesignatorList)parent).getDesignatorIdentifier().obj;
		 }
		 if (parent instanceof ClassFieldAccess) {
			 return ((ClassFieldAccess)parent).getMemberIdentifier().obj;
		 }
		 if (parent instanceof ArrayElementAccess) {
			 return((ArrayElementAccess)parent).getArrayIdentifier().obj;
		 }
		 System.out.println("NESTO LOSE 35");
		 return null;
	 }
	
	public static Obj getPreviousDesignator(DesignatorListEnd node) {
		SyntaxNode parent = node.getParent();
		 if (parent instanceof DesignatorList) {
			 return ((DesignatorList)parent).getDesignatorIdentifier().obj;
		 }
		 if (parent instanceof ClassFieldAccess) {
			 return ((ClassFieldAccess)parent).getMemberIdentifier().obj;
		 }
		 if (parent instanceof ArrayElementAccess) {
			 return((ArrayElementAccess)parent).getArrayIdentifier().obj;
		 }
		 return null;
	}
	
	
	public static Obj traverseDesignator(DesignatorList node) {
		DesignatorNext designator_next = node.getDesignatorNext();
		while (!(designator_next instanceof DesignatorListEnd)) {
			if (designator_next instanceof ArrayElementAccess) {
				designator_next = ((ArrayElementAccess)designator_next).getDesignatorNext();
			} else if (designator_next instanceof ClassFieldAccess) {
				designator_next = ((ClassFieldAccess)designator_next).getDesignatorNext();
			} else {
				System.out.println("DesignatorNext nedefinisanog tipa " + node.getLine());
				return null;
			}
			
		}
		return designator_next.obj;
	}
	
	public static Struct searchType(Obj node) {
		// vraca tip podatka na osnovu objektnog cvora kome se pristupa. Za niz
		return node.getType().getElemType();
		
	}
	
	public static Struct searchType(Obj node, String field) {
		return ((HashTableDataStructure)node.getLocalSymbols()).searchKey(field).getType();
	}
}
