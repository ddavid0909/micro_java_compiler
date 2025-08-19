// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class NextConstDeclaration extends NextConstDecl {

    private NextConstDecl NextConstDecl;
    private String constName;
    private OneConst OneConst;

    public NextConstDeclaration (NextConstDecl NextConstDecl, String constName, OneConst OneConst) {
        this.NextConstDecl=NextConstDecl;
        if(NextConstDecl!=null) NextConstDecl.setParent(this);
        this.constName=constName;
        this.OneConst=OneConst;
        if(OneConst!=null) OneConst.setParent(this);
    }

    public NextConstDecl getNextConstDecl() {
        return NextConstDecl;
    }

    public void setNextConstDecl(NextConstDecl NextConstDecl) {
        this.NextConstDecl=NextConstDecl;
    }

    public String getConstName() {
        return constName;
    }

    public void setConstName(String constName) {
        this.constName=constName;
    }

    public OneConst getOneConst() {
        return OneConst;
    }

    public void setOneConst(OneConst OneConst) {
        this.OneConst=OneConst;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(NextConstDecl!=null) NextConstDecl.accept(visitor);
        if(OneConst!=null) OneConst.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(NextConstDecl!=null) NextConstDecl.traverseTopDown(visitor);
        if(OneConst!=null) OneConst.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(NextConstDecl!=null) NextConstDecl.traverseBottomUp(visitor);
        if(OneConst!=null) OneConst.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("NextConstDeclaration(\n");

        if(NextConstDecl!=null)
            buffer.append(NextConstDecl.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(" "+tab+constName);
        buffer.append("\n");

        if(OneConst!=null)
            buffer.append(OneConst.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [NextConstDeclaration]");
        return buffer.toString();
    }
}
