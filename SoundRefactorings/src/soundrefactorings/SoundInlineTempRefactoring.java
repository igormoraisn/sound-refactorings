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
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.rename.TempDeclarationFinder;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;


@SuppressWarnings("restriction")
public class SoundInlineTempRefactoring extends InlineTempRefactoring{
	private int fSelectionStart;
	private int fSelectionLength;
	private ICompilationUnit fCu;
	private CompilationUnit fASTRoot;
	private VariableDeclaration fVariableDeclaration;
	List<QualifiedName> fields = new ArrayList<QualifiedName>();
	ASTRewrite rewrite;
	ListRewrite listRewrite;
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
		Change change = super.createChange(pm);
		String source = fCu.getSource();
        Document document = new Document(source);
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
		return super.createChange(pm);
	}
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		CompilationUnit parse = parse(fCu);
		ASTNode nodeSelected =  NodeFinder.perform(parse, this.fSelectionStart, this.fSelectionLength);
		if(checkReadOnly(parse))
			return RefactoringStatus.createFatalErrorStatus("One or most variables changed after expression");
		else {
			int totalAsserts = doAssignmentAssertions(nodeSelected) + doMethodAssertions();
			if(totalAsserts > 0) {
				RefactoringStatus status = super.checkInitialConditions(pm);
				status.addInfo(totalAsserts + " asserts will be generated");
				return status;
			}
			return super.checkInitialConditions(pm);
		}
	}
	public int doAssignmentAssertions(ASTNode node) throws CoreException{
		int asserts = 0;
        Expression right = fVariableDeclaration.getInitializer();
        QualifiedNameVisitor fa = new QualifiedNameVisitor();
        right.accept(fa);
        List<QualifiedName> lfa = fa.getMethods();
        if(lfa.size() > 0){
            ASTParser parser = ASTParser.newParser(AST.JLS3);
            parser.setSource(fCu);
            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            AST ast = cu.getAST();
    		rewrite = ASTRewrite.create( ast );
    		cu.recordModifications();
            MethodDeclaration method = getMethodDeclaration(cu);
        	Block b = method.getBody();
			listRewrite = rewrite.getListRewrite(b,
	                   Block.STATEMENTS_PROPERTY);
        	for(QualifiedName atual : fields){
        			SimpleName name = atual.getName();
        			Name exp = atual.getQualifier();
        			for(QualifiedName fac : lfa){
        				SimpleName rn = fac.getName();
        				if(convertToString(name).compareTo(convertToString(rn)) == 0){
        					Name re = fac.getQualifier();
        					SimpleName left = ast.newSimpleName(convertToString(re));
        					SimpleName rightExp = ast.newSimpleName(convertToString(exp));
        					InfixExpression infixExpression = ast .newInfixExpression();
        					infixExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
        					infixExpression.setLeftOperand(left);
        					infixExpression.setRightOperand(rightExp);
        					AssertStatement as = ast.newAssertStatement();
        					as.setExpression(infixExpression);
        					listRewrite.insertLast(as, null);
        					asserts++;
        				}
        			}
        	}
        }
        return asserts;
	}
	public int doMethodAssertions() throws JavaModelException{
		int asserts = 0;
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(fCu);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
		cu.recordModifications();
        Expression right = fVariableDeclaration.getInitializer();
        MethodDeclaration method = getMethodDeclaration(cu);
        QualifiedNameVisitor fa = new QualifiedNameVisitor();
        MethodInvocationVisitor miv = new MethodInvocationVisitor();
        right.accept(fa);
        method.accept(miv);
        List<QualifiedName> lfa = fa.getMethods();
        List<MethodInvocation> lmi = miv.getMethods();
        if(lfa.size() > 0 && lmi.size() > 0){
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
				listRewrite.insertLast(vds, null);
				i++;
        	}
        	for(MethodInvocation m : lmi){
            	i=1;
            	for(QualifiedName qn : lfa){
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
					asserts++;
					i++;
            	}
        	}
        }
        return asserts;
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
	public boolean checkReadOnly(CompilationUnit un) {
		boolean error = false;
		getVariableDeclaration();
		AssignmentVisitor assignmentVisitor = new AssignmentVisitor();
		Expression right = fVariableDeclaration.getInitializer();
		SimpleNameVisitor simpleNameVisitor = new SimpleNameVisitor();
		QualifiedNameVisitor qualifiedNameVisitor = new QualifiedNameVisitor();
		right.accept(simpleNameVisitor);
		int expressionPosition = right.getStartPosition();
		List<SimpleName> vard = simpleNameVisitor.getMethods(); 
		right.accept(qualifiedNameVisitor);
		List<QualifiedName> fieldList = qualifiedNameVisitor.getMethods(); 
		MethodDeclaration methodVerified= getMethodDeclaration(un);
		methodVerified.accept(assignmentVisitor);
		List<String> variablesList = new ArrayList<String>();
		for(Assignment assignment : assignmentVisitor.getMethods()){
			Expression aux = assignment.getLeftHandSide();
			int assignPosition = aux.getStartPosition();
			if(assignPosition > expressionPosition){
				if(aux.getNodeType() == 40){
					this.fields.add((QualifiedName)aux);
				}
				variablesList.add(convertToString(aux));
			}
		}
		for(SimpleName sn : vard){
			if(variablesList.contains(convertToString(sn))){
				error = true;
			}
        }
		for(QualifiedName qn : fieldList){
			if(variablesList.contains(convertToString(qn))){
				error = true;
			}
        }
		return error;
	}
	public VariableDeclaration getVariableDeclaration() {
		if (fVariableDeclaration == null) {
			fVariableDeclaration= TempDeclarationFinder.findTempDeclaration(getASTRoot(), fSelectionStart, fSelectionLength);
		}
		return fVariableDeclaration;
	}
	public MethodDeclaration getMethodDeclaration(CompilationUnit un) {
        Expression right = fVariableDeclaration.getInitializer();
        int pos = right.getStartPosition();
		MethodDeclarationVisitor mdv = new MethodDeclarationVisitor();
	    un.accept(mdv);
		MethodDeclaration method = null;
        for(MethodDeclaration md : mdv.getMethods()){
        	if(md.getStartPosition() < pos){
        		method = md;
        	}
        	else{
        		break;
        	}
        }
        return method;
	}
}
