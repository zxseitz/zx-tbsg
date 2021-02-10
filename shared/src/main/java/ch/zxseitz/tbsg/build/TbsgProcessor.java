package ch.zxseitz.tbsg.build;

import ch.zxseitz.tbsg.games.TbsgGame;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes({"ch.zxseitz.tbsg.games.TbsgGame"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class TbsgProcessor extends AbstractProcessor {
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        messager.printMessage(Diagnostic.Kind.WARNING, "start annotation processor");
        var annotatedElements = roundEnv.getElementsAnnotatedWith(TbsgGame.class);
        var types = ElementFilter.typesIn(annotatedElements);
        for (var type : types) {
            var packageElement = (PackageElement) type.getEnclosingElement();
            var packageName = packageElement.getQualifiedName().toString();
            var annotationValue = type.getAnnotation(TbsgGame.class).value();

            messager.printMessage(Diagnostic.Kind.NOTE, "Found class " + type.getSimpleName().toString() + " with Annotation @Tbsg(" +  annotationValue + ")");
        }

        return true;
    }
}
