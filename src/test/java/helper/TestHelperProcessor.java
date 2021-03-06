package helper;

import com.google.common.io.Files;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import processors.HelperAnnotationProcessor;
import processors.PojoAnnotationProcessor;

/**
 * Created by nikita on 21.11.16.
 */
public class TestHelperProcessor {


    @Test
    public void test() throws IOException {
        Compilation compilation = Compiler.javac().withProcessors(new HelperAnnotationProcessor()).compile(JavaFileObjects.forSourceString("BadHelperWithConstructor", Files.toString(new File("BadHelper"), StandardCharsets.UTF_8)));
        Assert.assertFalse(compilation.errors().isEmpty());
        Assert.assertEquals(1, compilation.errors().size());
        final String message = compilation.errors().get(0).getMessage(Locale.getDefault());
        Assert.assertTrue(message.contains("constructor"));
    }
}
