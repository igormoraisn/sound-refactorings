package soundrefactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;

public class AssignmentVisitor extends ASTVisitor {
    List<Assignment> methods = new ArrayList<Assignment>();
    @Override
    public boolean visit(Assignment node) {
               methods.add(node);
                return super.visit(node);
        }

        public List<Assignment> getMethods() {
                return methods;
        }
   public Assignment getFirst(){
	   return methods.get(0);
   }
}
