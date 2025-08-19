// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class DesignatorList implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Obj obj = null;

    private DesignatorIdentifier DesignatorIdentifier;
    private DesignatorNext DesignatorNext;

    public DesignatorList (DesignatorIdentifier DesignatorIdentifier, DesignatorNext DesignatorNext) {
        this.DesignatorIdentifier=DesignatorIdentifier;
        if(DesignatorIdentifier!=null) DesignatorIdentifier.setParent(this);
        this.DesignatorNext=DesignatorNext;
        if(DesignatorNext!=null) DesignatorNext.setParent(this);
    }

    public DesignatorIdentifier getDesignatorIdentifier() {
        return DesignatorIdentifier;
    }

    public void setDesignatorIdentifier(DesignatorIdentifier DesignatorIdentifier) {
        this.DesignatorIdentifier=DesignatorIdentifier;
    }

    public DesignatorNext getDesignatorNext() {
        return DesignatorNext;
    }

    public void setDesignatorNext(DesignatorNext DesignatorNext) {
        this.DesignatorNext=DesignatorNext;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(DesignatorIdentifier!=null) DesignatorIdentifier.accept(visitor);
        if(DesignatorNext!=null) DesignatorNext.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(DesignatorIdentifier!=null) DesignatorIdentifier.traverseTopDown(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(DesignatorIdentifier!=null) DesignatorIdentifier.traverseBottomUp(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorList(\n");

        if(DesignatorIdentifier!=null)
            buffer.append(DesignatorIdentifier.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(DesignatorNext!=null)
            buffer.append(DesignatorNext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorList]");
        return buffer.toString();
    }
}
