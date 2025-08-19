// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class InterfaceDecl implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Obj obj = null;

    private InterfaceDeclStart InterfaceDeclStart;
    private InterfaceMethods InterfaceMethods;

    public InterfaceDecl (InterfaceDeclStart InterfaceDeclStart, InterfaceMethods InterfaceMethods) {
        this.InterfaceDeclStart=InterfaceDeclStart;
        if(InterfaceDeclStart!=null) InterfaceDeclStart.setParent(this);
        this.InterfaceMethods=InterfaceMethods;
        if(InterfaceMethods!=null) InterfaceMethods.setParent(this);
    }

    public InterfaceDeclStart getInterfaceDeclStart() {
        return InterfaceDeclStart;
    }

    public void setInterfaceDeclStart(InterfaceDeclStart InterfaceDeclStart) {
        this.InterfaceDeclStart=InterfaceDeclStart;
    }

    public InterfaceMethods getInterfaceMethods() {
        return InterfaceMethods;
    }

    public void setInterfaceMethods(InterfaceMethods InterfaceMethods) {
        this.InterfaceMethods=InterfaceMethods;
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
        if(InterfaceDeclStart!=null) InterfaceDeclStart.accept(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(InterfaceDeclStart!=null) InterfaceDeclStart.traverseTopDown(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(InterfaceDeclStart!=null) InterfaceDeclStart.traverseBottomUp(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("InterfaceDecl(\n");

        if(InterfaceDeclStart!=null)
            buffer.append(InterfaceDeclStart.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(InterfaceMethods!=null)
            buffer.append(InterfaceMethods.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [InterfaceDecl]");
        return buffer.toString();
    }
}
