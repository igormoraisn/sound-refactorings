package soundrefactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class VariableDeclarationVisitor extends ASTVisitor {
    List<VariableDeclarationFragment> methods = new ArrayList<VariableDeclarationFragment>();
    @Override
    public boolean visit(VariableDeclarationFragment node) {
               methods.add(node);
                return super.visit(node);
        }

        public List<VariableDeclarationFragment> getMethods() {
                return methods;
        }

}
