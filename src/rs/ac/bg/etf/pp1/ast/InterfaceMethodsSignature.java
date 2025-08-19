// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class InterfaceMethodsSignature extends InterfaceMethods {

    private InterfaceMethodSignature InterfaceMethodSignature;
    private InterfaceMethods InterfaceMethods;

    public InterfaceMethodsSignature (InterfaceMethodSignature InterfaceMethodSignature, InterfaceMethods InterfaceMethods) {
        this.InterfaceMethodSignature=InterfaceMethodSignature;
        if(InterfaceMethodSignature!=null) InterfaceMethodSignature.setParent(this);
        this.InterfaceMethods=InterfaceMethods;
        if(InterfaceMethods!=null) InterfaceMethods.setParent(this);
    }

    public InterfaceMethodSignature getInterfaceMethodSignature() {
        return InterfaceMethodSignature;
    }

    public void setInterfaceMethodSignature(InterfaceMethodSignature InterfaceMethodSignature) {
        this.InterfaceMethodSignature=InterfaceMethodSignature;
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
        if(InterfaceMethodSignature!=null) InterfaceMethodSignature.accept(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(InterfaceMethodSignature!=null) InterfaceMethodSignature.traverseTopDown(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(InterfaceMethodSignature!=null) InterfaceMethodSignature.traverseBottomUp(visitor);
        if(InterfaceMethods!=null) InterfaceMethods.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("InterfaceMethodsSignature(\n");

        if(InterfaceMethodSignature!=null)
            buffer.append(InterfaceMethodSignature.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(InterfaceMethods!=null)
            buffer.append(InterfaceMethods.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [InterfaceMethodsSignature]");
        return buffer.toString();
    }
}
