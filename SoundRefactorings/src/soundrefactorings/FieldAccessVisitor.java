package soundrefactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;

public class FieldAccessVisitor extends ASTVisitor{
	    List<QualifiedName> methods = new ArrayList<QualifiedName>();
	    @Override
	    public boolean visit(QualifiedName node) {
	               methods.add(node);
	                return super.visit(node);
	        }

	        public List<QualifiedName> getMethods() {
	                return methods;
	        }
	   public QualifiedName getFirst(){
		   return methods.get(0);
	   }
	}
