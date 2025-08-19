// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class ClassFieldAccess extends DesignatorNext {

    private MemberIdentifier MemberIdentifier;
    private DesignatorNext DesignatorNext;

    public ClassFieldAccess (MemberIdentifier MemberIdentifier, DesignatorNext DesignatorNext) {
        this.MemberIdentifier=MemberIdentifier;
        if(MemberIdentifier!=null) MemberIdentifier.setParent(this);
        this.DesignatorNext=DesignatorNext;
        if(DesignatorNext!=null) DesignatorNext.setParent(this);
    }

    public MemberIdentifier getMemberIdentifier() {
        return MemberIdentifier;
    }

    public void setMemberIdentifier(MemberIdentifier MemberIdentifier) {
        this.MemberIdentifier=MemberIdentifier;
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
        if(MemberIdentifier!=null) MemberIdentifier.accept(visitor);
        if(DesignatorNext!=null) DesignatorNext.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MemberIdentifier!=null) MemberIdentifier.traverseTopDown(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MemberIdentifier!=null) MemberIdentifier.traverseBottomUp(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassFieldAccess(\n");

        if(MemberIdentifier!=null)
            buffer.append(MemberIdentifier.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(DesignatorNext!=null)
            buffer.append(DesignatorNext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassFieldAccess]");
        return buffer.toString();
    }
}
