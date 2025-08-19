// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class DesignatorIdentifier implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Obj obj = null;

    private String objectName;

    public DesignatorIdentifier (String objectName) {
        this.objectName=objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName=objectName;
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
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorIdentifier(\n");

        buffer.append(" "+tab+objectName);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorIdentifier]");
        return buffer.toString();
    }
}
