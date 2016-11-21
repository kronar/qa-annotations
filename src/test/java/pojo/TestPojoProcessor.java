package pojo;


import com.google.common.io.Files;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.junit.Assert;
import org.junit.Test;
import processors.PojoAnnotationProcessor;

/**
 * Created by nikita on 21.11.16.
 */
public class TestPojoProcessor {

@Test
public void testPojoFailed() throws IOException {


    Compilation compilation = Compiler.javac().withProcessors(new PojoAnnotationProcessor()).compile(JavaFileObjects.forSourceString("FailedPojo", Files.toString(new File("FailedPojo"), StandardCharsets.UTF_8)));
    Assert.assertFalse(compilation.errors().isEmpty());
    Assert.assertEquals(1, compilation.errors().size());
}


}
