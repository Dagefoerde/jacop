/* Generated By:JJTree: Do not edit this line. ASTConstElem.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.jacop.fz;

public class ASTConstElem extends SimpleNode {
    public ASTConstElem(int id) {
        super(id);
    }

    public ASTConstElem(Parser p, int id) {
        super(p, id);
    }

    String name;

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return super.toString() + ": " + name;
    }
}
/* JavaCC - OriginalChecksum=ab83b2ea3ef58820862e9980c082b599 (do not edit this line) */
