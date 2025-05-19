package io.github.daybringer;

import io.github.daybringer.packet.annotations.PacketHandler;
import io.github.daybringer.packet.utils.PacketHandlerType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("io.github.daybringer.packet.annotations.PacketHandler")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class PacketHandlerProcessor extends AbstractProcessor
{
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(PacketHandler.class)) {
            if (element.getKind() != ElementKind.METHOD) continue;

            ExecutableElement method = (ExecutableElement) element;
            PacketHandler annotation = method.getAnnotation(PacketHandler.class);

            if (annotation.handlerType() == PacketHandlerType.CANCELABLE) {
                if (!method.getReturnType().toString().equals("boolean") &&
                        !method.getReturnType().toString().equals("java.lang.Boolean")) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "PacketHandler with handlerType = CANCELABLE must return boolean",
                            method
                    );
                }
            }
        }
        return true;
    }
}
