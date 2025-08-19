// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class NextVariableDeclaration extends NextVarDecl {

    private NextVarDecl NextVarDecl;
    private String varName;
    private ArrayVar ArrayVar;

    public NextVariableDeclaration (NextVarDecl NextVarDecl, String varName, ArrayVar ArrayVar) {
        this.NextVarDecl=NextVarDecl;
        if(NextVarDecl!=null) NextVarDecl.setParent(this);
        this.varName=varName;
        this.ArrayVar=ArrayVar;
        if(ArrayVar!=null) ArrayVar.setParent(this);
    }

    public NextVarDecl getNextVarDecl() {
        return NextVarDecl;
    }

    public void setNextVarDecl(NextVarDecl NextVarDecl) {
        this.NextVarDecl=NextVarDecl;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName=varName;
    }

    public ArrayVar getArrayVar() {
        return ArrayVar;
    }

    public void setArrayVar(ArrayVar ArrayVar) {
        this.ArrayVar=ArrayVar;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(NextVarDecl!=null) NextVarDecl.accept(visitor);
        if(ArrayVar!=null) ArrayVar.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(NextVarDecl!=null) NextVarDecl.traverseTopDown(visitor);
        if(ArrayVar!=null) ArrayVar.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(NextVarDecl!=null) NextVarDecl.traverseBottomUp(visitor);
        if(ArrayVar!=null) ArrayVar.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("NextVariableDeclaration(\n");

        if(NextVarDecl!=null)
            buffer.append(NextVarDecl.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(" "+tab+varName);
        buffer.append("\n");

        if(ArrayVar!=null)
            buffer.append(ArrayVar.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [NextVariableDeclaration]");
        return buffer.toString();
    }
}
