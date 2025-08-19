// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class ConditionFactor extends CondFact {

    private Expr Expr;
    private RelExprOrNone RelExprOrNone;

    public ConditionFactor (Expr Expr, RelExprOrNone RelExprOrNone) {
        this.Expr=Expr;
        if(Expr!=null) Expr.setParent(this);
        this.RelExprOrNone=RelExprOrNone;
        if(RelExprOrNone!=null) RelExprOrNone.setParent(this);
    }

    public Expr getExpr() {
        return Expr;
    }

    public void setExpr(Expr Expr) {
        this.Expr=Expr;
    }

    public RelExprOrNone getRelExprOrNone() {
        return RelExprOrNone;
    }

    public void setRelExprOrNone(RelExprOrNone RelExprOrNone) {
        this.RelExprOrNone=RelExprOrNone;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Expr!=null) Expr.accept(visitor);
        if(RelExprOrNone!=null) RelExprOrNone.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Expr!=null) Expr.traverseTopDown(visitor);
        if(RelExprOrNone!=null) RelExprOrNone.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Expr!=null) Expr.traverseBottomUp(visitor);
        if(RelExprOrNone!=null) RelExprOrNone.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConditionFactor(\n");

        if(Expr!=null)
            buffer.append(Expr.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(RelExprOrNone!=null)
            buffer.append(RelExprOrNone.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConditionFactor]");
        return buffer.toString();
    }
}
