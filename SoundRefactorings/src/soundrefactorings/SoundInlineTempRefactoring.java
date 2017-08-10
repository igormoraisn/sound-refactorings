package soundrefactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.TempDeclarationFinder;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


public class SoundInlineTempRefactoring extends InlineTempRefactoring{
	private int fSelectionStart;
	private int fSelectionLength;
	private ICompilationUnit fCu;
	private CompilationUnit fASTRoot;
	private VariableDeclaration fVariableDeclaration;
	public SoundInlineTempRefactoring(ICompilationUnit unit, int selectionStart, int selectionLength){
		super(unit, null, selectionStart, selectionLength);
		this.fCu = unit;
		this.fSelectionLength = selectionLength;
		this.fSelectionStart = selectionStart;
		this.fVariableDeclaration = null;
		this.fASTRoot = null;
	}
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		return super.createChange(pm);
	}
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		boolean error = false;
		CompilationUnit parse = parse(fCu);
		getVariableDeclaration();
		ASTNode nodeSelected =  NodeFinder.perform(parse, this.fSelectionStart, this.fSelectionLength);
		Expression right;
		AssignmentVisitor assignmentVisitor = new AssignmentVisitor();
		right = fVariableDeclaration.getInitializer();
		SimpleNameVisitor vis = new SimpleNameVisitor();
		right.accept(vis);
		int pos = right.getStartPosition();
		List<SimpleName> vard = vis.getMethods(); 
		nodeSelected = nodeSelected.getParent();
		nodeSelected = nodeSelected.getParent();
		nodeSelected = nodeSelected.getParent();
		nodeSelected.accept(assignmentVisitor);
		List<String> simplename = new ArrayList<String>();
		for(Assignment as : assignmentVisitor.getMethods()){
			SimpleName aux = (SimpleName) as.getLeftHandSide();
			int positionAssig = aux.getStartPosition();
			if(positionAssig > pos){
				simplename.add(convertToString(aux));
			}
		}
		for(SimpleName sn : vard){
			if(simplename.contains(convertToString(sn))){
				error = true;
			}
        }
		if(error)
			return RefactoringStatus.createErrorStatus("One or most variables changed after expression");
		else
			return super.checkInitialConditions(pm);
	}
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
		return super.checkFinalConditions(pm);
	}
	private String convertToString(Object a){
		return a + "";
	}
    private static CompilationUnit parse(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null); // parse
    }
	private CompilationUnit getASTRoot() {
		if (fASTRoot == null) {
			fASTRoot= RefactoringASTParser.parseWithASTProvider(fCu, true, null);
		}
		return fASTRoot;
	}

	public VariableDeclaration getVariableDeclaration() {
		if (fVariableDeclaration == null) {
			fVariableDeclaration= TempDeclarationFinder.findTempDeclaration(getASTRoot(), fSelectionStart, fSelectionLength);
		}
		return fVariableDeclaration;
	}
}
