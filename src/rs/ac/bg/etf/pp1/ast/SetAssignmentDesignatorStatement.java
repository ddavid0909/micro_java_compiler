// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class SetAssignmentDesignatorStatement extends DesignatorStatement {

    private DesignatorList DesignatorList;
    private Assignop Assignop;
    private DesignatorList DesignatorList1;
    private Setop Setop;
    private DesignatorList DesignatorList2;

    public SetAssignmentDesignatorStatement (DesignatorList DesignatorList, Assignop Assignop, DesignatorList DesignatorList1, Setop Setop, DesignatorList DesignatorList2) {
        this.DesignatorList=DesignatorList;
        if(DesignatorList!=null) DesignatorList.setParent(this);
        this.Assignop=Assignop;
        if(Assignop!=null) Assignop.setParent(this);
        this.DesignatorList1=DesignatorList1;
        if(DesignatorList1!=null) DesignatorList1.setParent(this);
        this.Setop=Setop;
        if(Setop!=null) Setop.setParent(this);
        this.DesignatorList2=DesignatorList2;
        if(DesignatorList2!=null) DesignatorList2.setParent(this);
    }

    public DesignatorList getDesignatorList() {
        return DesignatorList;
    }

    public void setDesignatorList(DesignatorList DesignatorList) {
        this.DesignatorList=DesignatorList;
    }

    public Assignop getAssignop() {
        return Assignop;
    }

    public void setAssignop(Assignop Assignop) {
        this.Assignop=Assignop;
    }

    public DesignatorList getDesignatorList1() {
        return DesignatorList1;
    }

    public void setDesignatorList1(DesignatorList DesignatorList1) {
        this.DesignatorList1=DesignatorList1;
    }

    public Setop getSetop() {
        return Setop;
    }

    public void setSetop(Setop Setop) {
        this.Setop=Setop;
    }

    public DesignatorList getDesignatorList2() {
        return DesignatorList2;
    }

    public void setDesignatorList2(DesignatorList DesignatorList2) {
        this.DesignatorList2=DesignatorList2;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(DesignatorList!=null) DesignatorList.accept(visitor);
        if(Assignop!=null) Assignop.accept(visitor);
        if(DesignatorList1!=null) DesignatorList1.accept(visitor);
        if(Setop!=null) Setop.accept(visitor);
        if(DesignatorList2!=null) DesignatorList2.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(DesignatorList!=null) DesignatorList.traverseTopDown(visitor);
        if(Assignop!=null) Assignop.traverseTopDown(visitor);
        if(DesignatorList1!=null) DesignatorList1.traverseTopDown(visitor);
        if(Setop!=null) Setop.traverseTopDown(visitor);
        if(DesignatorList2!=null) DesignatorList2.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(DesignatorList!=null) DesignatorList.traverseBottomUp(visitor);
        if(Assignop!=null) Assignop.traverseBottomUp(visitor);
        if(DesignatorList1!=null) DesignatorList1.traverseBottomUp(visitor);
        if(Setop!=null) Setop.traverseBottomUp(visitor);
        if(DesignatorList2!=null) DesignatorList2.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("SetAssignmentDesignatorStatement(\n");

        if(DesignatorList!=null)
            buffer.append(DesignatorList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Assignop!=null)
            buffer.append(Assignop.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(DesignatorList1!=null)
            buffer.append(DesignatorList1.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Setop!=null)
            buffer.append(Setop.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(DesignatorList2!=null)
            buffer.append(DesignatorList2.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [SetAssignmentDesignatorStatement]");
        return buffer.toString();
    }
}
