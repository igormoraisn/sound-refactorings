/*package soundrefactorings;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;


public class WalaTest {
	public static void main(String args[]) throws IOException, ClassHierarchyException, CoreException {

			//File exFile=new FileProvider().getFile("/home/igor/workspace/com.ibm.wala.ide.jdt.test/data/Java60RegressionExclusions.txt");
			//System.out.println(exFile.getAbsolutePath());
			//JDTJavaSourceAnalysisEngine test = new JDTJavaSourceAnalysisEngine("de.refactoring.call");
			//AnalysisScope js = test.makeAnalysisScope();
			
			/*AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("/home/igor/workspace/SD/amqp-client-3.6.5.jar",exFile);
			IClassHierarchy cha = ClassHierarchy.make(scope);
			for (IClass c : cha) {
				String cname = c.getName().toString();
				System.out.println("Class:" + cname);
				for (IMethod m : c.getAllMethods()) {
					String mname = m.getName().toString();
					System.out.println("  method:" + mname);
				}
				System.out.println();
			}


	}
}*/