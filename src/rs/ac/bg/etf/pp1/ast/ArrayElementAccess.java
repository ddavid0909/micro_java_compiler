// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class ArrayElementAccess extends DesignatorNext {

    private ArrayIdentifier ArrayIdentifier;
    private DesignatorNext DesignatorNext;

    public ArrayElementAccess (ArrayIdentifier ArrayIdentifier, DesignatorNext DesignatorNext) {
        this.ArrayIdentifier=ArrayIdentifier;
        if(ArrayIdentifier!=null) ArrayIdentifier.setParent(this);
        this.DesignatorNext=DesignatorNext;
        if(DesignatorNext!=null) DesignatorNext.setParent(this);
    }

    public ArrayIdentifier getArrayIdentifier() {
        return ArrayIdentifier;
    }

    public void setArrayIdentifier(ArrayIdentifier ArrayIdentifier) {
        this.ArrayIdentifier=ArrayIdentifier;
    }

    public DesignatorNext getDesignatorNext() {
        return DesignatorNext;
    }

    public void setDesignatorNext(DesignatorNext DesignatorNext) {
        this.DesignatorNext=DesignatorNext;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ArrayIdentifier!=null) ArrayIdentifier.accept(visitor);
        if(DesignatorNext!=null) DesignatorNext.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ArrayIdentifier!=null) ArrayIdentifier.traverseTopDown(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ArrayIdentifier!=null) ArrayIdentifier.traverseBottomUp(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ArrayElementAccess(\n");

        if(ArrayIdentifier!=null)
            buffer.append(ArrayIdentifier.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(DesignatorNext!=null)
            buffer.append(DesignatorNext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ArrayElementAccess]");
        return buffer.toString();
    }
}
