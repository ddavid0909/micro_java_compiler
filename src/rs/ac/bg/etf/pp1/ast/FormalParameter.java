// generated with ast extension for cup
// version 0.8
// 19/7/2025 20:4:51


package rs.ac.bg.etf.pp1.ast;

public class FormalParameter extends FormPars {

    private FormPars FormPars;
    private FormParsType FormParsType;
    private String formParName;
    private ArrayVar ArrayVar;

    public FormalParameter (FormPars FormPars, FormParsType FormParsType, String formParName, ArrayVar ArrayVar) {
        this.FormPars=FormPars;
        if(FormPars!=null) FormPars.setParent(this);
        this.FormParsType=FormParsType;
        if(FormParsType!=null) FormParsType.setParent(this);
        this.formParName=formParName;
        this.ArrayVar=ArrayVar;
        if(ArrayVar!=null) ArrayVar.setParent(this);
    }

    public FormPars getFormPars() {
        return FormPars;
    }

    public void setFormPars(FormPars FormPars) {
        this.FormPars=FormPars;
    }

    public FormParsType getFormParsType() {
        return FormParsType;
    }

    public void setFormParsType(FormParsType FormParsType) {
        this.FormParsType=FormParsType;
    }

    public String getFormParName() {
        return formParName;
    }

    public void setFormParName(String formParName) {
        this.formParName=formParName;
    }

    public ArrayVar getArrayVar() {
        return ArrayVar;
    }

    public void setArrayVar(ArrayVar ArrayVar) {
        this.ArrayVar=ArrayVar;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(FormPars!=null) FormPars.accept(visitor);
        if(FormParsType!=null) FormParsType.accept(visitor);
        if(ArrayVar!=null) ArrayVar.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(FormPars!=null) FormPars.traverseTopDown(visitor);
        if(FormParsType!=null) FormParsType.traverseTopDown(visitor);
        if(ArrayVar!=null) ArrayVar.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(FormPars!=null) FormPars.traverseBottomUp(visitor);
        if(FormParsType!=null) FormParsType.traverseBottomUp(visitor);
        if(ArrayVar!=null) ArrayVar.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("FormalParameter(\n");

        if(FormPars!=null)
            buffer.append(FormPars.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(FormParsType!=null)
            buffer.append(FormParsType.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(" "+tab+formParName);
        buffer.append("\n");

        if(ArrayVar!=null)
            buffer.append(ArrayVar.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FormalParameter]");
        return buffer.toString();
    }
}
