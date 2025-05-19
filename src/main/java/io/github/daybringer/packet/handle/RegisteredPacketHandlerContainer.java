package io.github.daybringer.packet.handle;

import java.lang.reflect.Method;

public record RegisteredPacketHandlerContainer(Object instance, Method handle)
{

}
