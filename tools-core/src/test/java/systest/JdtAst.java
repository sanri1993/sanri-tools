package systest;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class JdtAst {

	private ASTParser astParser = ASTParser.newParser(AST.JLS3); // 非常慢

	/**
     * 获得java源文件的结构CompilationUnit
     */
    public CompilationUnit getCompilationUnit(String filePath)
            throws Exception {

        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        byte[] input = new byte[bufferedInputStream.available()];
        bufferedInputStream.read(input);
        bufferedInputStream.close();
        this.astParser.setSource(new String(input).toCharArray());
        CompilationUnit result = (CompilationUnit) (this.astParser.createAST(null)); // 很慢
//            result.getImports();//通过result去获取java文件的属性，如getImports是获取java文件中import的文件的。
        return result;

    }


    @Test
    public void test() throws Exception {
        final CompilationUnit compilationUnit = getCompilationUnit("D:\\companyproject\\fssc_eam\\eam-service\\fssc-eims\\fssc-eims-core\\taxmpv2-eam\\utax-eam\\src\\main\\java\\com\\wenjing\\eam\\controller/EamHscEquipUserController.java");
        final PackageDeclaration aPackage = compilationUnit.getPackage();

    }
}