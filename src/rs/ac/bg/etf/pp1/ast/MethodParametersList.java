// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class MethodParametersList extends MethodParamsList {

    private MethodParamsList MethodParamsList;
    private VarDecl VarDecl;

    public MethodParametersList (MethodParamsList MethodParamsList, VarDecl VarDecl) {
        this.MethodParamsList=MethodParamsList;
        if(MethodParamsList!=null) MethodParamsList.setParent(this);
        this.VarDecl=VarDecl;
        if(VarDecl!=null) VarDecl.setParent(this);
    }

    public MethodParamsList getMethodParamsList() {
        return MethodParamsList;
    }

    public void setMethodParamsList(MethodParamsList MethodParamsList) {
        this.MethodParamsList=MethodParamsList;
    }

    public VarDecl getVarDecl() {
        return VarDecl;
    }

    public void setVarDecl(VarDecl VarDecl) {
        this.VarDecl=VarDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MethodParamsList!=null) MethodParamsList.accept(visitor);
        if(VarDecl!=null) VarDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MethodParamsList!=null) MethodParamsList.traverseTopDown(visitor);
        if(VarDecl!=null) VarDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MethodParamsList!=null) MethodParamsList.traverseBottomUp(visitor);
        if(VarDecl!=null) VarDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MethodParametersList(\n");

        if(MethodParamsList!=null)
            buffer.append(MethodParamsList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarDecl!=null)
            buffer.append(VarDecl.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MethodParametersList]");
        return buffer.toString();
    }
}
