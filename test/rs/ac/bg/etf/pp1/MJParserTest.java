package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.factory.SymbolTableFactory;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;

public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(MJParserTest.class);
		
		Reader br = null;
		try {
			File sourceCode = new File("test/"+args[0]);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser parser = new MJParser(lexer);
	        Symbol s = parser.parse();

			if (parser.errorDetected) {
				System.out.println("Syntax errors found during parsing! Cannot proceed with semantic analysis.");
				return;
			}
	        
	        Program prog = (Program)(s.value); 
	        Tab.init();
			log.info(prog.toString(""));
			log.info("===================================");
			MJParserTest.addToUniverse();
			SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
			prog.traverseBottomUp(semanticAnalyzer);
			log.info("===================================");
			//Tab.dump(new BooleanDumpSymbolTableVisitor());

			if (semanticAnalyzer.getErrorDetected()) {
				System.out.println("Semantic errors found during analysis! Cannot proceed with code generation");
				return;
			}

			File objFile = new File("test/"+args[0].substring(0, args[0].length() - 3) + ".obj");
			if (objFile.exists()) objFile.delete();
			CodeGenerator codeGenerator = new CodeGenerator(semanticAnalyzer.n_vars);
			prog.traverseBottomUp(codeGenerator);
			Code.dataSize = codeGenerator.n_vars;
			Code.mainPc = codeGenerator.getMainPc();
			Code.write(new FileOutputStream(objFile));
			
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	private static void addToUniverse() {
		
		Tab.insert(Obj.Type, "bool", SemanticAnalyzer.boolType);
		Tab.insert(Obj.Type, "set", SemanticAnalyzer.setType);

		Obj add = Tab.insert(Obj.Meth, "add", Tab.noType);
		add.setLevel(2);
		Obj set = new Obj(Obj.Var, "dest", SemanticAnalyzer.setType);
		set.setLevel(1);
		set.setAdr(0);
		Obj new_number = new Obj(Obj.Var, "number", Tab.intType);
		new_number.setLevel(1);
		new_number.setAdr(1);
		SymbolDataStructure add_parameters = SymbolTableFactory.instance().createSymbolTableDataStructure();
		add_parameters.insertKey(set);
		add_parameters.insertKey(new_number);
		add.setLocals(add_parameters);
		
		Obj remove = Tab.insert(Obj.Meth, "remove", Tab.noType);
		remove.setLevel(2);
		set = new Obj(Obj.Var, "dest", SemanticAnalyzer.setType);
		set.setLevel(1);
		set.setAdr(0);
		new_number = new Obj(Obj.Var, "number", Tab.intType);
		new_number.setLevel(1);
		new_number.setAdr(1);
		SymbolDataStructure remove_parameters = SymbolTableFactory.instance().createSymbolTableDataStructure();
		remove_parameters.insertKey(set);
		remove_parameters.insertKey(new_number);
		remove.setLocals(remove_parameters);

		Obj print_set = Tab.insert(Obj.Meth, "print", Tab.noType);
		print_set.setLevel(1);
		set = new Obj(Obj.Var, "dest", SemanticAnalyzer.setType);
		set.setAdr(0);
		set.setLevel(1);
		SymbolDataStructure print_parameters = SymbolTableFactory.instance().createSymbolTableDataStructure();
		print_parameters.insertKey(set);
		print_set.setLocals(print_parameters);

		Obj addAll = Tab.insert(Obj.Meth, "addAll", Tab.noType);
		addAll.setLevel(2);
		set = new Obj(Obj.Var, "dest", SemanticAnalyzer.setType);
		set.setAdr(0);
		set.setLevel(1);
		Obj add_array = new Obj(Obj.Var, "new_array",  new Struct(Struct.Array, Tab.intType));
		add_array.setAdr(1);
		add_array.setLevel(1);
		SymbolDataStructure add_all_parameters = SymbolTableFactory.instance().createSymbolTableDataStructure();
		add_all_parameters.insertKey(set);
		add_all_parameters.insertKey(add_array);
		addAll.setLocals(add_all_parameters);
		
		Obj union = Tab.insert(Obj.Meth, "union", Tab.noType);
		union.setLevel(3);
		Obj dest = new Obj(Obj.Var, "dest", SemanticAnalyzer.setType);
		dest.setAdr(0);
		dest.setLevel(1);
		Obj left = new Obj(Obj.Var, "left", SemanticAnalyzer.setType);
		left.setAdr(1);
		left.setLevel(1);
		Obj right = new Obj(Obj.Var, "right", SemanticAnalyzer.setType);
		right.setAdr(2);
		right.setLevel(1);
		SymbolDataStructure union_parameters = SymbolTableFactory.instance().createSymbolTableDataStructure();
		union_parameters.insertKey(dest);
		union_parameters.insertKey(left);
		union_parameters.insertKey(right);
		union.setLocals(union_parameters);
		
		Obj intersect = Tab.insert(Obj.Meth, "intersect", Tab.noType);
		intersect.setLevel(3);
		dest = new Obj(Obj.Var, "dest", SemanticAnalyzer.setType);
		dest.setAdr(0);
		dest.setLevel(1);
		left = new Obj(Obj.Var, "left", SemanticAnalyzer.setType);
		left.setAdr(1);
		left.setLevel(1);
		right = new Obj(Obj.Var, "right", SemanticAnalyzer.setType);
		right.setAdr(2);
		right.setLevel(1);
		SymbolDataStructure intersect_parameters = SymbolTableFactory.instance().createSymbolTableDataStructure();
		intersect_parameters.insertKey(dest);
		intersect_parameters.insertKey(left);
		intersect_parameters.insertKey(right);
		intersect.setLocals(intersect_parameters);
		
	}
	
	
}
