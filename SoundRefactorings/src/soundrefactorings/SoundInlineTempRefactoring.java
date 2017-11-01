package soundrefactorings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.TempDeclarationFinder;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;


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
		List<QualifiedName> fields = new ArrayList<QualifiedName>();
		for(Assignment as : assignmentVisitor.getMethods()){
			Expression aux = as.getLeftHandSide();
			int positionAssig = aux.getStartPosition();
			if(positionAssig > pos){
				if(aux.getNodeType() == 40){
					fields.add((QualifiedName)aux);
				}
				simplename.add(convertToString(aux));
			}
		}
		for(SimpleName sn : vard){
			if(simplename.contains(convertToString(sn))){
				error = true;
			}
        }
		//doAssignmentAssertions(nodeSelected, fields);
		doMethodAssertions();
		/*if(error)
			return RefactoringStatus.createErrorStatus("One or most variables changed after expression");
		else
			return super.checkInitialConditions(pm);*/
		return new RefactoringStatus();
	}
	public void doAssignmentAssertions(ASTNode node, List<QualifiedName> fields) throws JavaModelException{
		String source = fCu.getSource();
        Document document = new Document(source);

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(fCu);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
		ASTRewrite rewrite = ASTRewrite.create( ast );
		cu.recordModifications();
        Expression right = fVariableDeclaration.getInitializer();
        int pos = right.getStartPosition();
        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
        cu.accept(mdv);
        MethodDeclaration method = null;
        for(MethodDeclaration md : mdv.getMethods()){
        	if(md.getStartPosition() < pos){
        		method = md;
        	}
        	else{
        		break;
        	}
        }
        // Primeira checagem
        FieldAccessVisitor fa = new FieldAccessVisitor();
        right.accept(fa);
        List<QualifiedName> lfa = fa.getMethods();
        if(lfa.size() > 0){
        	for(QualifiedName atual : fields){
        			SimpleName name = atual.getName();
        			System.out.println(name);
        			Name exp = atual.getQualifier();
        			for(QualifiedName fac : lfa){
        				SimpleName rn = fac.getName();
        				if(convertToString(name).compareTo(convertToString(rn)) == 0){
        					Name re = fac.getQualifier();
        					Block b = method.getBody();
        					SimpleName left = ast.newSimpleName(convertToString(re));
        					SimpleName rightExp = ast.newSimpleName(convertToString(exp));
        					InfixExpression infixExpression = ast .newInfixExpression();
        					infixExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
        					infixExpression.setLeftOperand(left);
        					infixExpression.setRightOperand(rightExp);
        					AssertStatement as = ast.newAssertStatement();
        					as.setExpression(infixExpression);
        					ListRewrite listRewrite = rewrite.getListRewrite(b,
        			                   Block.STATEMENTS_PROPERTY);
        					List<ASTNode> lista = listRewrite.getOriginalList();
        					listRewrite.insertAt(as, 3, null);
        					TextEdit edits = rewrite.rewriteAST(document, null);
        					UndoEdit undo;
        					try {
        						undo = edits.apply(document);
        			        } catch (MalformedTreeException es) {
        			            // TODO Auto-generated catch block
        			            es.printStackTrace();
        			        } catch (BadLocationException es) {
        			            // TODO Auto-generated catch block
        			            es.printStackTrace();
        			        }
        					String newSource = document.get();
        					// update of the compilation unit
        					fCu.getBuffer().setContents(newSource);
        				}
        		}
        	}
        	
        }
	}
	public void doMethodAssertions() throws JavaModelException{
		String source = fCu.getSource();
        Document document = new Document(source);

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(fCu);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
		ASTRewrite rewrite = ASTRewrite.create( ast );
		cu.recordModifications();
        Expression right = fVariableDeclaration.getInitializer();
        int pos = right.getStartPosition();
        MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
        cu.accept(mdv);
        MethodDeclaration method = null;
        for(MethodDeclaration md : mdv.getMethods()){
        	if(md.getStartPosition() < pos){
        		method = md;
        	}
        	else{
        		break;
        	}
        }
        FieldAccessVisitor fa = new FieldAccessVisitor();
        MethodInvocationVisitor miv = new MethodInvocationVisitor();
        right.accept(fa);
        method.accept(miv);
        List<QualifiedName> lfa = fa.getMethods();
        List<MethodInvocation> lmi = miv.getMethods();
        if(lfa.size() > 0 && lmi.size() > 0){
        	Block b = method.getBody();
        	ListRewrite listRewrite = rewrite.getListRewrite(b,
                   Block.STATEMENTS_PROPERTY);
        	int i=1;
        	for(QualifiedName qn : lfa){
        		VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
        		Name name = ast.newName(convertToString(qn.getQualifier()));
        		SimpleName sn = ast.newSimpleName(convertToString(qn.getName()));
        		QualifiedName e = ast.newQualifiedName(name, sn);
        		SimpleName varName = ast.newSimpleName("t"+ i);
        		vd.setName(varName);
        		vd.setInitializer(e);
        		VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vd);
				listRewrite.insertFirst(vds, null);
				i++;
        	}
        	for(MethodInvocation m : lmi){
            	i=1;
            	for(QualifiedName qn : lfa){
					//SimpleName left = ast.newSimpleName(convertToString(qn));
            		Name name = ast.newName(convertToString(qn.getQualifier()));
            		SimpleName sn = ast.newSimpleName(convertToString(qn.getName()));
            		QualifiedName left = ast.newQualifiedName(name, sn);
					SimpleName rightExp = ast.newSimpleName("t" + i);
					InfixExpression infixExpression = ast .newInfixExpression();
					infixExpression.setOperator(InfixExpression.Operator.EQUALS);
					infixExpression.setLeftOperand(left);
					infixExpression.setRightOperand(rightExp);
					AssertStatement as = ast.newAssertStatement();
					as.setExpression(infixExpression);
					listRewrite.insertLast(as, null);
					i++;
            	}
        	}
			TextEdit edits = rewrite.rewriteAST(document, null);
			UndoEdit undo;
			try {
				undo = edits.apply(document);
			} catch (MalformedTreeException es) {
				// TODO Auto-generated catch block
				es.printStackTrace();
			} catch (BadLocationException es) {
				// TODO Auto-generated catch block
				es.printStackTrace();
			}
			String newSource = document.get();
			// update of the compilation unit
			fCu.getBuffer().setContents(newSource);
        }
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
