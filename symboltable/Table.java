package symboltable;import syntaxtree.IntegerType;import syntaxtree.BooleanType;import syntaxtree.IntArrayType;import syntaxtree.IdentifierType;import syntaxtree.Type;import java.util.Hashtable;import java.util.Enumeration;public class Table {    private Hashtable<String, RamClass> hashtable;    public ErrorMsg error = new ErrorMsg();    public boolean anyErrors() { return error.anyErrors; }    public Table() {        hashtable = new Hashtable<String, RamClass>();    }    public boolean addClass(String id) {        if (containsClass(id)) {            return false;        } else {            hashtable.put(id, new RamClass(id));        }        return true;    }    public RamClass getClass(String id) {        if (containsClass(id)) {            return hashtable.get(id);        } else {            return null;        }    }        public int numClasses() {        return hashtable.size();    }    public boolean containsClass(String id) {        return hashtable.containsKey(id);    }        public Type getVarType(RamMethod m, RamClass c, String id) {        if (m != null) {            if (m.getVar(id) != null) {                return m.getVar(id).type();            }            if (m.getParam(id) != null) {                return m.getParam(id).type();            }        }                 if (c.getVar(id) != null) {            return c.getVar(id).type();        }         error.complain("Variable " + id                + " not defined in current scope");        return null;    }    public RamMethod getMethod(String id, String classScope) {        if (getClass(classScope) == null) {            error.complain("Class " + classScope                    + " not defined");            return null;        }        RamClass c = getClass(classScope);                if (c.getMethod(id) != null) {            return c.getMethod(id);        }                 error.complain("Method " + id + " not defined in class " + classScope);        return null;    }    public Type getMethodType(String id, String classScope) {        if (getClass(classScope) == null) {            error.complain("Class " + classScope                    + " not defined");            return null;        }        RamClass c = getClass(classScope);                if (c.getMethod(id) != null) {            return c.getMethod(id).type();        }                 error.complain("Method " + id + " not defined in class " + classScope);        return null;    }    public boolean compareTypes(Type t1, Type t2) {        if (t1 == null || t2 == null) {            return false;        }        if (t1 instanceof IntegerType && t2 instanceof IntegerType) {            return true;        }        if (t1 instanceof BooleanType && t2 instanceof BooleanType) {            return true;        }        if (t1 instanceof IntArrayType && t2 instanceof IntArrayType) {            return true;        }        if (t1 instanceof IdentifierType && t2 instanceof IdentifierType) {            IdentifierType i1 = (IdentifierType) t1;            IdentifierType i2 = (IdentifierType) t2;            RamClass c = getClass(i2.s);                        if (c != null && i1.s.equals(c.getId())) {                return true;            }         }        return false;    }            public String toString()     {        StringBuffer sb = new StringBuffer("Classes:\n");        for (Enumeration<String> e = hashtable.keys(); e.hasMoreElements(); )            sb.append(hashtable.get(e.nextElement()).toString());        return sb.toString();    }}//SymbolTable