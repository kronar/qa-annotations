package processors;

import annotations.Helper;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 */
public class HelperAnnotationProcessor extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Helper.class.getName());
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Helper.class);
        ArrayListMultimap<TypeElement, String> errors = ArrayListMultimap.create();
        for (Element element : elements) {


            if (element.getKind() == ElementKind.CLASS) {
                TypeElement te = (TypeElement) element;
                List<? extends Element> enclosedElements = te.getEnclosedElements();
                List<ExecutableElement> constructors = ElementFilter.constructorsIn(enclosedElements);
                boolean allConstructorsArePrivate = Iterables.all(constructors, new Predicate<ExecutableElement>() {
                    public boolean apply(ExecutableElement input) {
                        return input.getModifiers().equals(Sets.newHashSet(Modifier.PRIVATE));
                    }
                });


                List<VariableElement> fields = ElementFilter.fieldsIn(enclosedElements);
                boolean allFieldsAreStaticAndFinal = Iterables.all(fields, new Predicate<VariableElement>() {
                    public boolean apply(VariableElement input) {
                        return input.getModifiers().contains(Modifier.FINAL) && input.getModifiers().contains(Modifier.STATIC);
                    }
                });

                List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElements);
                boolean allMethodsAreStatic = Iterables.all(methods, new Predicate<ExecutableElement>() {
                    public boolean apply(ExecutableElement input) {
                        return input.getModifiers().contains(Modifier.STATIC);
                    }
                });

                if (!allConstructorsArePrivate) {

                    String msg = "Class " + te.getQualifiedName().toString() + " is not trully Helper (only private constructors are allowed)";
                    errors.put(te, msg);
                }

                if (!allMethodsAreStatic) {
                    String msg = "Class " + te.getQualifiedName().toString() + " is not trully Helper (only static methods are allowed)";
                    errors.put(te, msg);
                }

                if (!allFieldsAreStaticAndFinal) {
                    String msg = "Class " + te.getQualifiedName().toString() + " is not trully Helper (only static and final fields are allowed)";
                    errors.put(te, msg);
                }


            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Wrong usage of @Helper annotation on "+element.getSimpleName().toString());
            }


        }


        Set<TypeElement> typeElements = errors.asMap().keySet();
        for (TypeElement typeElement : typeElements) {
            List<String> strings = errors.get(typeElement);
            for (String errMsg : strings) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errMsg);
            }
        }
        return true;
    }
}
