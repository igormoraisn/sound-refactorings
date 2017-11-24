package soundrefactorings;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.refactoring.InlineTempWizard;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class RefactoringHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		doInlineTemp();
		return null;
	}
	public void doInlineTemp(){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ITextEditor editor = (ITextEditor) page.getActiveEditor();
		IJavaElement elem = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
		ICompilationUnit un = (ICompilationUnit) elem;
		ISelectionService ss = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ITextSelection its = (ITextSelection) ss.getSelection();
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			SoundInlineTempRefactoring iir = new SoundInlineTempRefactoring(un, its.getOffset(), its.getLength());
			RefactoringWizard rw = new InlineTempWizard(iir);
			RefactoringStarter rs = new RefactoringStarter();
			rs.activate(rw, shell, "Sound Inline Temp", 1);
			/*status = iir.checkInitialConditions(monitor);
		    if(status.hasError()){
		    	MessageDialog.openError(shell, "Error", status.toString());
		    }
		    else{
		    	iir.checkFinalConditions(monitor);
			    Change change = iir.createChange(monitor);
			    change.perform(monitor);
		    }*/
			
		}catch(AssertionFailedException a) {
			a.printStackTrace();
		}catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
}
