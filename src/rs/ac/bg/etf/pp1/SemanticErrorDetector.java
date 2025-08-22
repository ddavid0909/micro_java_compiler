package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

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
        if (dest.isRefType() && src == Tab.nullType) return true;
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
        report_error("Semantic error. Erroneous assignment.", node);
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
        if (s_2.isRefType() && s_1 == Tab.nullType || s_1.isRefType() && s_2 == Tab.nullType) return true;
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
        report_error("Semantic error. Erroneous comparison.", node);
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

    public boolean constTypesMatch(Struct type_1, Struct type_2, SyntaxNode node) {
        if (type_1 != type_2) {
            report_error("Semantic error. Constant types mismatch", node);
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

}
