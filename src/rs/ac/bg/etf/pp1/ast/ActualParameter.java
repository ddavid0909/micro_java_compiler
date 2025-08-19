// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class ActualParameter extends ActPars {

    private ActParsNext ActParsNext;
    private ActParam ActParam;

    public ActualParameter (ActParsNext ActParsNext, ActParam ActParam) {
        this.ActParsNext=ActParsNext;
        if(ActParsNext!=null) ActParsNext.setParent(this);
        this.ActParam=ActParam;
        if(ActParam!=null) ActParam.setParent(this);
    }

    public ActParsNext getActParsNext() {
        return ActParsNext;
    }

    public void setActParsNext(ActParsNext ActParsNext) {
        this.ActParsNext=ActParsNext;
    }

    public ActParam getActParam() {
        return ActParam;
    }

    public void setActParam(ActParam ActParam) {
        this.ActParam=ActParam;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ActParsNext!=null) ActParsNext.accept(visitor);
        if(ActParam!=null) ActParam.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ActParsNext!=null) ActParsNext.traverseTopDown(visitor);
        if(ActParam!=null) ActParam.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ActParsNext!=null) ActParsNext.traverseBottomUp(visitor);
        if(ActParam!=null) ActParam.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ActualParameter(\n");

        if(ActParsNext!=null)
            buffer.append(ActParsNext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ActParam!=null)
            buffer.append(ActParam.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ActualParameter]");
        return buffer.toString();
    }
}
