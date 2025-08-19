// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class ConditionTerm extends CondTerm {

    private CondTermStart CondTermStart;
    private CondFact CondFact;
    private AndConditionList AndConditionList;

    public ConditionTerm (CondTermStart CondTermStart, CondFact CondFact, AndConditionList AndConditionList) {
        this.CondTermStart=CondTermStart;
        if(CondTermStart!=null) CondTermStart.setParent(this);
        this.CondFact=CondFact;
        if(CondFact!=null) CondFact.setParent(this);
        this.AndConditionList=AndConditionList;
        if(AndConditionList!=null) AndConditionList.setParent(this);
    }

    public CondTermStart getCondTermStart() {
        return CondTermStart;
    }

    public void setCondTermStart(CondTermStart CondTermStart) {
        this.CondTermStart=CondTermStart;
    }

    public CondFact getCondFact() {
        return CondFact;
    }

    public void setCondFact(CondFact CondFact) {
        this.CondFact=CondFact;
    }

    public AndConditionList getAndConditionList() {
        return AndConditionList;
    }

    public void setAndConditionList(AndConditionList AndConditionList) {
        this.AndConditionList=AndConditionList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(CondTermStart!=null) CondTermStart.accept(visitor);
        if(CondFact!=null) CondFact.accept(visitor);
        if(AndConditionList!=null) AndConditionList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(CondTermStart!=null) CondTermStart.traverseTopDown(visitor);
        if(CondFact!=null) CondFact.traverseTopDown(visitor);
        if(AndConditionList!=null) AndConditionList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(CondTermStart!=null) CondTermStart.traverseBottomUp(visitor);
        if(CondFact!=null) CondFact.traverseBottomUp(visitor);
        if(AndConditionList!=null) AndConditionList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConditionTerm(\n");

        if(CondTermStart!=null)
            buffer.append(CondTermStart.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(CondFact!=null)
            buffer.append(CondFact.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(AndConditionList!=null)
            buffer.append(AndConditionList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConditionTerm]");
        return buffer.toString();
    }
}
