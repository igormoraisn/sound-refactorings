package soundrefactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

public class SimpleNameVisitor extends ASTVisitor{
	    List<SimpleName> methods = new ArrayList<SimpleName>();
	    @Override
	    public boolean visit(SimpleName node) {
	               methods.add(node);
	                return super.visit(node);
	        }

	        public List<SimpleName> getMethods() {
	                return methods;
	        }

}
