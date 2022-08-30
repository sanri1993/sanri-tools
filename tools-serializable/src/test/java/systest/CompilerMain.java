package systest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompilerMain {
    @Test
    public void test1(){
        List a = new ArrayList();
        a.add(11);
        a.add("sss");
        System.out.println(a);
    }
    @Test
    public void test3() throws IOException {
        final InputStream resourceAsStream = CompilerMain.class.getResourceAsStream("/classpath.txt");
        final String classpath = IOUtils.toString(resourceAsStream);
        final List<File> classpathFiles = Arrays.stream(classpath.split(";")).map(File::new).collect(Collectors.toList());
        final List<File> lombok = classpathFiles.stream().filter(classpathFile -> classpathFile.getName().contains("lombok")).collect(Collectors.toList());
        List<File> sources = Arrays.asList(new File("D:\\tmp\\sanritools\\data\\gitrepositorys\\yuanian\\fssc_eam\\eam-service\\fssc-eims\\fssc-eims-core\\taxmpv2-eam\\utax-eam-base\\src\\main\\java\\"));

        classpathFiles.add(new File("D:\\tmp\\sanritools\\data\\gitrepositorys\\yuanian\\fssc_eam\\eam-service\\fssc-eims\\fssc-eims-core\\taxmpv2-eam\\utax-eam-base/target/classes"));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        final StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        standardFileManager.setLocation(StandardLocation.CLASS_PATH,classpathFiles);
        standardFileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH,lombok);
        standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT,Arrays.asList(new File("d:/test")));
        standardFileManager.setLocation(StandardLocation.SOURCE_PATH,sources);
//        standardFileManager.setLocation(StandardLocation.SOURCE_OUTPUT,Arrays.asList(new File("d:/test/generatejava")));
//        final Iterable<? extends JavaFileObject> javaFileObjects = standardFileManager.getJavaFileObjects(new File("D:\\tmp\\sanritools\\data\\gitrepositorys\\yuanian\\fssc_eam\\eam-service\\fssc-eims\\fssc-eims-core\\taxmpv2-eam\\utax-eam-base\\src\\main\\java\\com\\wenjing\\eambase\\service\\pushbiz\\OnlyCollectImageFinishUpload.java"));
        final JavaFileObject javaFileObject = standardFileManager.getJavaFileForInput(StandardLocation.SOURCE_PATH, "com.wenjing.eambase.service.pushbiz.OnlyCollectImageFinishUpload", JavaFileObject.Kind.SOURCE);

        List<String> options = Arrays.asList("-encoding", "utf-8","-source","1.8");

        JavaCompiler.CompilationTask task = compiler.getTask(null, standardFileManager, null, options, null, Arrays.asList(javaFileObject));

        final Boolean call = task.call();

        standardFileManager.close();

        assert call ;


    }

    @Test
    public void test4() throws IOException {
        final InputStream resourceAsStream = CompilerMain.class.getResourceAsStream("/classpath.txt");
        final String classpath = IOUtils.toString(resourceAsStream);
        final List<File> classpathFiles = Arrays.stream(classpath.split(";")).map(File::new).collect(Collectors.toList());
        final List<File> lombok = classpathFiles.stream().filter(classpathFile -> classpathFile.getName().contains("lombok")).collect(Collectors.toList());
        List<File> sources = Arrays.asList(new File("D:\\tmp\\sanritools\\data\\gitrepositorys\\yuanian\\fssc_eam\\eam-service\\fssc-eims\\fssc-eims-core\\taxmpv2-eam\\utax-eam-base\\src\\main\\java\\"));

        classpathFiles.add(new File("D:\\tmp\\sanritools\\data\\gitrepositorys\\yuanian\\fssc_eam\\eam-service\\fssc-eims\\fssc-eims-core\\taxmpv2-eam\\utax-eam-base/target/classes"));

        final DiagnosticCollector<JavaFileObject> diagnosticCollector = null;// new DiagnosticCollector<>();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        final StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
        standardFileManager.setLocation(StandardLocation.CLASS_PATH,classpathFiles);
        standardFileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH,lombok);
        standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT,Arrays.asList(new File("d:/test")));
        standardFileManager.setLocation(StandardLocation.SOURCE_PATH,sources);
//        final JavaFileObject javaFileObject = standardFileManager.getJavaFileForInput(StandardLocation.SOURCE_PATH, "com.wenjing.eambase.service.pushbiz.OnlyCollectImageFinishUpload", JavaFileObject.Kind.SOURCE);

        final File file = new File(sources.get(0), "com/wenjing/eambase/service/pushbiz/OnlyCollectImageFinishUpload.java");
        final Iterable<? extends JavaFileObject> javaFileObjects = standardFileManager.getJavaFileObjects(file);

        List<String> options = Arrays.asList("-encoding", "utf-8","-source","1.8");

        JavaCompiler.CompilationTask task = compiler.getTask(null, standardFileManager, diagnosticCollector, options, null, javaFileObjects);

        final Boolean call = task.call();

        final JavaFileObject javaFileForInput = standardFileManager.getJavaFileForInput(StandardLocation.CLASS_OUTPUT, "com.wenjing.eambase.service.pushbiz.OnlyCollectImageFinishUpload", JavaFileObject.Kind.CLASS);

        System.out.println(javaFileForInput);

        standardFileManager.close();

        System.out.println(diagnosticCollector);

        assert call ;
    }
}
