// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class ErrorInVariableDeclaration extends VarDecl {

    private NextVarDecl NextVarDecl;

    public ErrorInVariableDeclaration (NextVarDecl NextVarDecl) {
        this.NextVarDecl=NextVarDecl;
        if(NextVarDecl!=null) NextVarDecl.setParent(this);
    }

    public NextVarDecl getNextVarDecl() {
        return NextVarDecl;
    }

    public void setNextVarDecl(NextVarDecl NextVarDecl) {
        this.NextVarDecl=NextVarDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(NextVarDecl!=null) NextVarDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(NextVarDecl!=null) NextVarDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(NextVarDecl!=null) NextVarDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ErrorInVariableDeclaration(\n");

        if(NextVarDecl!=null)
            buffer.append(NextVarDecl.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ErrorInVariableDeclaration]");
        return buffer.toString();
    }
}
