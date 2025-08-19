// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class FunctionSignature extends MethodSignature {

    private FunctionTypeName FunctionTypeName;
    private FormParsList FormParsList;

    public FunctionSignature (FunctionTypeName FunctionTypeName, FormParsList FormParsList) {
        this.FunctionTypeName=FunctionTypeName;
        if(FunctionTypeName!=null) FunctionTypeName.setParent(this);
        this.FormParsList=FormParsList;
        if(FormParsList!=null) FormParsList.setParent(this);
    }

    public FunctionTypeName getFunctionTypeName() {
        return FunctionTypeName;
    }

    public void setFunctionTypeName(FunctionTypeName FunctionTypeName) {
        this.FunctionTypeName=FunctionTypeName;
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
        if(FunctionTypeName!=null) FunctionTypeName.accept(visitor);
        if(FormParsList!=null) FormParsList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(FunctionTypeName!=null) FunctionTypeName.traverseTopDown(visitor);
        if(FormParsList!=null) FormParsList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(FunctionTypeName!=null) FunctionTypeName.traverseBottomUp(visitor);
        if(FormParsList!=null) FormParsList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("FunctionSignature(\n");

        if(FunctionTypeName!=null)
            buffer.append(FunctionTypeName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(FormParsList!=null)
            buffer.append(FormParsList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FunctionSignature]");
        return buffer.toString();
    }
}
