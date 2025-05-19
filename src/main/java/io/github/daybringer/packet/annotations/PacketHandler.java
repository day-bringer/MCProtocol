package io.github.daybringer.packet.annotations;

import io.github.daybringer.packet.utils.PacketHandlerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketHandler {
    PacketHandlerType handlerType() default PacketHandlerType.NORMAL;
}
