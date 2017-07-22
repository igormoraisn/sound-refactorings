package soundrefactorings;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
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
		RefactoringStatus status = new RefactoringStatus();
		try {
			ITypeRoot it = (ITypeRoot) un;
			SoundInlineTempRefactoring iir = new SoundInlineTempRefactoring(un, its.getOffset(), its.getLength());
			IProgressMonitor monitor = new NullProgressMonitor();
		    iir.checkInitialConditions(monitor);
		    iir.checkFinalConditions(monitor);
		    Change change = iir.createChange(monitor);
		    change.perform(monitor);

		} catch (CoreException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
}