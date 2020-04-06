package visitor;
import syntaxtree.*;
import symboltable.*;
import java.util.Enumeration;


public class CodeGenerator extends DepthFirstVisitor {

    private java.io.PrintStream out;
    private static int nextLabelNum = 0;
    private Table symTable;

    //Counters for print statements
    private int printlnCounter = 0;

    //Counters for loops/decision structures
    private int ifCounter = 0;
    private int ifDoneCounter = 0;
    private int WhileCounter = 0;
    private int callCounter = 0;
    private RamMethod currentMethod;
    private RamClass currentClass;

    //Counters for boolean operators
    private int andCounter = 0;
    private int orCounter = 0;
    private int lessThanCounter = 0;
    
    private StringBuilder dataString = new StringBuilder("");
    
    public CodeGenerator(java.io.PrintStream o, Table st) {
        out = o; 
        symTable = st;
    }

    private void emit(String s) {
        out.println("\t" + s);
    }

    private void emitLabel(String l) {
        out.println(l + ":");
    }
    
    private void emitComment(String s) {
        out.println("\t" + "#" + s);
    }
    
    // MainClass m;
    // ClassDeclList cl;
    public void visit(Program n) {
        
        emit(".text");
        emit(".globl main");
        
        n.m.accept(this);
        for ( int i = 0; i < n.cl.size(); i++ ) {
            n.cl.elementAt(i).accept(this);
        }
        
        emit("");
        emit(".data");
        out.println(dataString.toString());
    }
    
    // Identifier i1, i2;
    // Statement s;
    public void visit(MainClass n) {
        symTable.addClass(n.i1.toString());
        TypeCheckVisitor.currClass = symTable.getClass(n.i1.toString());
        symTable.getClass(n.i1.s).addMethod("main", new IdentifierType("void"));
        TypeCheckVisitor.currMethod = symTable.getClass(n.i1.toString()).getMethod("main");
        symTable.getMethod("main", 
                TypeCheckVisitor.currClass.getId()).addParam(n.i2.toString(), new IdentifierType("String[]"));

        emitLabel("main");
        
        emitComment("begin prologue -- main");
        emit("subu $sp, $sp, 24    # stack frame is at least 24 bytes");
        emit("sw $fp, 4($sp)       # save caller's frame pointer");
        emit("sw $ra, 0($sp)       # save return address");
        
        emit("addi $fp, $sp, 20    # set up main's frame pointer");       
        emitComment("end prologue -- main");
        
        n.s.accept(this);
        
        emitComment("begin epilogue -- main");
        emit("lw $ra, 0($sp)       # restore return address");
        emit("lw $fp, 4($sp)       # restore caller's frame pointer");
        emit("addi $sp, $sp, 24    # pop the stack"); 
        emitComment("end epilogue -- main");
        
        /*
        emit("jr $ra");   // SPIM: how to end programs
        emit("\n");       // SPIM: how to end programs 
        */
        
        emit("li $v0, 10");   // MARS: how to end programs
        emit("syscall");      // MARS: how to end programs
        
        TypeCheckVisitor.currMethod = null;
        
    }
    
    // TODO
    // Exp e;


    public void visit(Print n) {
        n.e.accept(this);

        emit("move $a0, $v0");
        emit("li $v0, 1");
        emit("syscall");
    }
    public void visit(Println n){
//        emit(".data");
//        emit("Str:  "+"asciiz " + "\"" + "\\n" + "\"");
//        emit(".text");
        n.e.accept(this);
        emit("move $a0, $v0");
        emit("li $v0, 1");
        emit("syscall");
//        emit("li $v0, 4");
//        emit("li $v0, Str");
//        emit("syscall");

    }


    // int i;
    public void visit(IntegerLiteral n) {
        emit("li $v0, "+n.i+"        # load literal "+n.i+" into $v0");
    }


    public void visit(True n){
        emit("li $v0, "+1+"        # load literal "+1+" into $v0");

    }
    public void visit(False n){
        emit("li $v0, "+0+"        # load literal "+0+" into $v0");

    }
    public void visit(Plus n){
        n.e1.accept(this);
        emit("sub $sp, $sp, 4");
        emit("sw $v0, ($sp)");

        n.e2.accept(this);
        emit("lw $v1 ($sp)");
        emit("addiu $sp, $sp, 4");
        emit("add $v0, $v0, $v1");

    }
    public void visit(Minus n){
        n.e1.accept(this);
        emit("sub $sp, $sp, 4");
        emit("sw $v0, ($sp)");

        n.e2.accept(this);
        emit("lw $v1, ($sp)");
        emit("addiu $sp, $sp, 4");
        emit("sub $v0, $v1, $v0");
    }
    public void visit(Times n){
        n.e1.accept(this);
        emit("sub $sp, $sp, 4");
        emit("sw $v0, ($sp)");

        n.e2.accept(this);
        emit("lw $v1, ($sp)");
        emit("addiu $sp, $sp, 4");
        emit("mul $v0, $v0, $v1");
    }
    public void visit (If n){
        ifCounter++;
        n.e.accept(this);
        emit("beqz $v0, IsFalse" + ifCounter);

        n.s1.accept(this);
        emit("jal IsDone" + ifDoneCounter);

        emitLabel("IsFalse" + ifCounter);
        n.s2.accept(this);
        emit("jal IsDone" + ifDoneCounter);

        emitLabel("IsDone" + ifDoneCounter);
        ifDoneCounter++;
        
    }
    public void visit(And n){
        n.e1.accept(this);
        emit("beq $v0, $zero, IsFalse" + ifCounter);
        n.e2.accept(this);
        emit("beq $v0, $zero, IsFalse" + ifCounter);
        emit("li $v0,1");
        emit("j IsDone" +ifDoneCounter);
        emit("IsFalse" + ifCounter +": ");
        emit("li $v0, 0");
        emit("IsDone" + ifDoneCounter + ": ");
        ifCounter++;
        ifDoneCounter++;
    }
    public void visit(Or n){
        n.e1.accept(this);
        emit("beq $v0, $zero, IsTrue" + ifCounter);
        n.e2.accept(this);
        emit("beq $v0, $zero, IsTrue" + ifCounter);
        emit("li $v0, 0");
        emit("j IsDone" +ifDoneCounter);
        emit("IsTrue" + ifCounter +": ");
        emit("li $v0, 1");
        emit("IsDone" + ifDoneCounter + ": ");
        ifCounter++;
        ifDoneCounter++;
    }
    public void visit(LessThan n){
        n.e1.accept(this);
        emit("sub $sp, $sp, 4");
        emit("sw $v0, ($sp)");

        n.e2.accept(this);
        emit("lw $v1, ($sp)");
        emit("addiu $sp, $sp, 4");

        emit("slt $t1, $v1, $v0");
        emit("beq $v0, $v1, IsFalse" + ifCounter);
        emit("beq $t1, $zero, IsFalse"  +ifCounter);
        emit("li $v0,1");
        emit("j IsDone" +ifDoneCounter);
        emit("IsFalse" + ifCounter +": ");
        emit("li $v0, 0");
        emit("IsDone" + ifDoneCounter + ": ");
        ifCounter++;
        ifDoneCounter++;

    }
    public void visit(Equals n){
        n.e1.accept(this);
        emit("sub $sp, $sp, 4");
        emit("sw $v0, ($sp)");

        n.e2.accept(this);
        emit("lw $v1, ($sp)");
        emit("addiu $sp, $sp, 4");

        emit("seq $t0, $v0, $v1");
        emit("beq $t0, 0, IsFalse" + ifCounter);
        emit("li $v0,1");
        emit("j IsDone" +ifDoneCounter);
        emit("IsFalse" + ifCounter +": ");
        emit("li $v0, 0");
        emit("IsDone" + ifDoneCounter + ": ");
        ifCounter++;
        ifDoneCounter++;
    }
    public void visit(Not n){
        n.e.accept(this);
        emit("beq $v0, 0, Opposite" + ifCounter);
        emit("li $v0, 0");
        emit("j IsDone" +ifDoneCounter);
        emit("Opposite" + ifCounter +": ");
        emit("li $v0, 1");
        emit("IsDone" + ifDoneCounter + ": ");
        ifCounter++;
        ifDoneCounter++;
    }
//Causes issues Might have to comment out
    public void visit(Call n){
        callCounter++;
        emitComment("CALL "+n.i.s);
    emitComment("Incoming...");
    for(int i = 0; i < n.el.size(); i++){
        emit("subu $sp, $sp, 4" + "#create room on stack for element in expList");
        n.el.elementAt(i).accept(this);
        emit("sw $v0, 0($sp)");

    }
    emit("move $fp, $sp");
    emit("jal " + n.i + callCounter);
    emitLabel("return" + n.i+callCounter);
        //        emit("#Preparing");
//
        emitComment("CALL "+n.i.s);
    }

    public void visit(MethodDecl n) {
        emitComment("METHOD DECL " + n.i.s);
        int size = n.fl.size()*4;
        emitComment("begin prologue " + n.toString());
        emit("subu $sp, $sp, " + (size)+"#stack frame is at least  " + (size)+ " bytes");
        emit("sw $fp, 4($sp)");
        emit("sw $ra, 0($sp)");
        emit("addi $fp, $sp, " + ((size+24)-4)+"#set up "+ n.i.s+"'s frame pointer");
        emitComment("end prologue..."+n.i.s);

        for(int i = 0; i < n.sl.size(); i++){
            emitComment("Running " + i+ "the statement is: ");
            n.sl.elementAt(i).accept(this);
        }
        n.e.accept(this);
        emitComment("METHOD DECL " + n.i.s);
    }
    public void visit(ClassDeclSimple n){
        emitComment("CLASS DECL " + n.i.s);
//        TypeCheckVisitor.currClass = symTable.getClass(n.i.toString());


        TypeCheckVisitor.currClass = symTable.getClass(n.i.s);


        for(int i = 0; i < n.ml.size(); i++) {
            TypeCheckVisitor.currMethod = symTable.getMethod(n.ml.elementAt(i).i.s,TypeCheckVisitor.currClass.getId());
            emitLabel(n.ml.elementAt(i).i.s +callCounter);
//            for (int j = 0; i < n.ml.elementAt(i).sl.size(); j++) {
//                n.ml.elementAt(i).sl.elementAt(j).accept(this);
//            }
            int offset = symTable.getMethod(n.ml.elementAt(i).i.s,TypeCheckVisitor.currClass.getId()).numParams();
            for(int j = 0;j< symTable.getMethod(n.ml.elementAt(i).i.s,TypeCheckVisitor.currClass.getId()).numParams();j++){
                symTable.getMethod(n.ml.elementAt(i).i.s,TypeCheckVisitor.currClass.getId()).getVar((n.ml.elementAt(i)).vl.elementAt(j).i.s).setOffset(offset);
                offset-=4;
            }
            offset = n.ml.elementAt(i).vl.size() *4;
            for(int k = 0; k<n.ml.elementAt(i).vl.size();k++){
                symTable.getMethod(n.ml.elementAt(i).i.s,TypeCheckVisitor.currClass.getId()).getVar((n.ml.elementAt(i)).vl.elementAt(k).i.s).setOffset(offset);
                offset-=4;
            }
            n.ml.elementAt(i).accept(this);
            emit("j return"+n.ml.elementAt(i).i.s +callCounter);
        }
        emitComment("CLASS DECL " + n.i.s);
    }
    public void visit(Identifier n){
        //TypeCheckVisitor.currMethod = currentMethod;
        int offset = TypeCheckVisitor.currMethod.getParam(n.s).getOffset();
        emit("add $t0, $fp "+offset);
        emit("move $v0, $t0");
    }
    public void visit(IdentifierExp n){
        int offset = TypeCheckVisitor.currMethod.getParam(n.s).getOffset();
        emit("add $t0, $fp" + offset);
        emit("$t1, $t0");
        emit("move $v0, $t1");
//        emit("lw $a1, ($sp) + $t0");

        // emit("move $v0, $a1");
    }
    public void visit(Assign n){
        n.e.accept(this);
        emit("move $t0, $v0");
        n.i.accept(this);
        emit("sw $t0, $v0");
    }

//    public void visit(While n){
//        emit("While: " + WhileCounter);
//        n.e.accept(this);
//        emit("beqz $v0, Done");
//        n.s.accept(this);
//        emit("j While" + WhileCounter + ": ");
//        emit("IsDone" + WhileCounter + ": ");
//        WhileCounter++;
//
//    }


}