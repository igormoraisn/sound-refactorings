package soundrefactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class SoundInlineTempRefactoring extends InlineTempRefactoring{
	public SoundInlineTempRefactoring(ICompilationUnit unit, int selectionStart, int selectionLength){
		super(unit, null, selectionStart, selectionLength);
	}
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		return super.createChange(pm);
	}
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		return super.checkInitialConditions(pm);
	}
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
		return super.checkFinalConditions(pm);
	}
}
