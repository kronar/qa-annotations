package processors;

import annotations.Pojo;
import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 */
public class PojoAnnotationProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Pojo.class.getName());
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Pojo.class);
        Set<TypeElement> errorTypes = Sets.newHashSet();

        for (Element element : elements) {
            TypeElement te = (TypeElement) element;
            final List<? extends Element> inner = element.getEnclosedElements();
            List<ExecutableElement> methods = ElementFilter.methodsIn(inner);
            ImmutableList<MethodSpec> methdoSpecs = FluentIterable.from(methods).transform(new Function<ExecutableElement, MethodSpec>() {
                public MethodSpec apply(ExecutableElement input) {
                    return new MethodSpec(input.getSimpleName().toString(), input.getModifiers(), input.getParameters(), input.getReturnType());
                }
            }).toList();


            Optional<MethodSpec> equalsMethodPresents = Iterables.tryFind(methdoSpecs, new EqualsMethodSpecPredicate());
            Optional<MethodSpec> hashCodeMethodPresents = Iterables.tryFind(methdoSpecs, new HashcodeMethodSpecPredicate());
            Optional<MethodSpec> toStringMethodPresents = Iterables.tryFind(methdoSpecs, new ToStringMethodSpecPredicate());


            if (!equalsMethodPresents.isPresent() || !hashCodeMethodPresents.isPresent() || !toStringMethodPresents.isPresent()) {
                errorTypes.add(te);
            }
        }


        if (!errorTypes.isEmpty()) {
            for (TypeElement errorType : errorTypes) {
                processingEnv.getMessager().printMessage(ERROR, "Class annotated with @Pojo doesnt satisfies to POJO rules (equals/hashCode/toString methods are definded): " + errorType.getQualifiedName().toString());
            }
        }
        return true;
    }


    private static final class MethodSpec {
        private final String methodName;
        private final Set<Modifier> modifiers;
        private final List<? extends VariableElement> parameters;
        private final TypeMirror returnType;

        private MethodSpec(String methodName, Set<Modifier> modifiers, List<? extends VariableElement> parameters, TypeMirror returnType) {
            this.methodName = methodName;
            this.modifiers = modifiers;
            this.parameters = parameters;
            this.returnType = returnType;
        }

        public String getMethodName() {
            return methodName;
        }

        public Set<Modifier> getModifiers() {
            return modifiers;
        }

        public List<? extends VariableElement> getParameters() {
            return parameters;
        }

        public TypeMirror getReturnType() {
            return returnType;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodSpec that = (MethodSpec) o;
            return Objects.equal(methodName, that.methodName) &&
                    Objects.equal(modifiers, that.modifiers) &&
                    Objects.equal(parameters, that.parameters) &&
                    Objects.equal(returnType, that.returnType);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(methodName, modifiers, parameters, returnType);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("methodName", methodName)
                    .add("modifiers", modifiers)
                    .add("parameters", parameters)
                    .add("returnType", returnType)
                    .toString();
        }
    }

    private static class EqualsMethodSpecPredicate implements Predicate<MethodSpec> {
        private static final String EQUALS = "equals";
        private static final String BOOLEAN = "boolean";
        private static final int EQUALS_METHOD_PARAMS_COUNT = 1;

        public boolean apply(MethodSpec input) {

            return input.getMethodName().equals(EQUALS) &&
                    input.getReturnType().getKind() == TypeKind.BOOLEAN &&
                    input.getModifiers().equals(Sets.newHashSet(Modifier.PUBLIC)) &&
                    input.getParameters().size() == EQUALS_METHOD_PARAMS_COUNT;
        }
    }

    private static class HashcodeMethodSpecPredicate implements Predicate<MethodSpec> {
        private static final String HASH_CODE = "hashCode";
        private static final String INT = "int";

        public boolean apply(MethodSpec input) {
            return input.getMethodName().equals(HASH_CODE)
                    && input.getModifiers().equals(Sets.newHashSet(Modifier.PUBLIC)) &&
                    input.getParameters().isEmpty() &&
                    input.getReturnType().getKind() == TypeKind.INT;
        }
    }

    private static class ToStringMethodSpecPredicate implements Predicate<MethodSpec> {


        private static final String TO_STRING = "toString";
        private static final String STRING_TYPE_FQN = "java.lang.String";

        public boolean apply(MethodSpec input) {

            return input.getMethodName().equals(TO_STRING)
                    && input.getModifiers().equals(Sets.newHashSet(Modifier.PUBLIC))
                    && input.getParameters().isEmpty()
                    && input.getReturnType().toString().equals(STRING_TYPE_FQN);
        }
    }
}
