// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class ReturnStatement extends Statement {

    private ExprOrNone ExprOrNone;

    public ReturnStatement (ExprOrNone ExprOrNone) {
        this.ExprOrNone=ExprOrNone;
        if(ExprOrNone!=null) ExprOrNone.setParent(this);
    }

    public ExprOrNone getExprOrNone() {
        return ExprOrNone;
    }

    public void setExprOrNone(ExprOrNone ExprOrNone) {
        this.ExprOrNone=ExprOrNone;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ExprOrNone!=null) ExprOrNone.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ExprOrNone!=null) ExprOrNone.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ExprOrNone!=null) ExprOrNone.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ReturnStatement(\n");

        if(ExprOrNone!=null)
            buffer.append(ExprOrNone.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ReturnStatement]");
        return buffer.toString();
    }
}
