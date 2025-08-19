// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class ProcedureSignature extends MethodSignature {

    private ProcedureTypeName ProcedureTypeName;
    private FormParsList FormParsList;

    public ProcedureSignature (ProcedureTypeName ProcedureTypeName, FormParsList FormParsList) {
        this.ProcedureTypeName=ProcedureTypeName;
        if(ProcedureTypeName!=null) ProcedureTypeName.setParent(this);
        this.FormParsList=FormParsList;
        if(FormParsList!=null) FormParsList.setParent(this);
    }

    public ProcedureTypeName getProcedureTypeName() {
        return ProcedureTypeName;
    }

    public void setProcedureTypeName(ProcedureTypeName ProcedureTypeName) {
        this.ProcedureTypeName=ProcedureTypeName;
    }

    public FormParsList getFormParsList() {
        return FormParsList;
    }

    public void setFormParsList(FormParsList FormParsList) {
        this.FormParsList=FormParsList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ProcedureTypeName!=null) ProcedureTypeName.accept(visitor);
        if(FormParsList!=null) FormParsList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ProcedureTypeName!=null) ProcedureTypeName.traverseTopDown(visitor);
        if(FormParsList!=null) FormParsList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ProcedureTypeName!=null) ProcedureTypeName.traverseBottomUp(visitor);
        if(FormParsList!=null) FormParsList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ProcedureSignature(\n");

        if(ProcedureTypeName!=null)
            buffer.append(ProcedureTypeName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(FormParsList!=null)
            buffer.append(FormParsList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ProcedureSignature]");
        return buffer.toString();
    }
}
