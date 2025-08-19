// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class InterfaceMethodsDeclaration extends InterfaceMethods {

    private InterfaceMethodDeclaration InterfaceMethodDeclaration;
    private InterfaceMethods InterfaceMethods;

    public InterfaceMethodsDeclaration (InterfaceMethodDeclaration InterfaceMethodDeclaration, InterfaceMethods InterfaceMethods) {
        this.InterfaceMethodDeclaration=InterfaceMethodDeclaration;
        if(InterfaceMethodDeclaration!=null) InterfaceMethodDeclaration.setParent(this);
        this.InterfaceMethods=InterfaceMethods;
        if(InterfaceMethods!=null) InterfaceMethods.setParent(this);
    }

    public InterfaceMethodDeclaration getInterfaceMethodDeclaration() {
        return InterfaceMethodDeclaration;
    }

    public void setInterfaceMethodDeclaration(InterfaceMethodDeclaration InterfaceMethodDeclaration) {
        this.InterfaceMethodDeclaration=InterfaceMethodDeclaration;
    }

    public InterfaceMethods getInterfaceMethods() {
        return InterfaceMethods;
    }

    public void setInterfaceMethods(InterfaceMethods InterfaceMethods) {
        this.InterfaceMethods=InterfaceMethods;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(InterfaceMethodDeclaration!=null) InterfaceMethodDeclaration.accept(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(InterfaceMethodDeclaration!=null) InterfaceMethodDeclaration.traverseTopDown(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(InterfaceMethodDeclaration!=null) InterfaceMethodDeclaration.traverseBottomUp(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("InterfaceMethodsDeclaration(\n");

        if(InterfaceMethodDeclaration!=null)
            buffer.append(InterfaceMethodDeclaration.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(InterfaceMethods!=null)
            buffer.append(InterfaceMethods.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [InterfaceMethodsDeclaration]");
        return buffer.toString();
    }
}
