/*
 * Copyright 2010 Martin Grotzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.javakaffee.web.msm.serializer.kryo;

import java.nio.ByteBuffer;

import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.SingletonTargetSource;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

import de.javakaffee.kryoserializers.cglib.CGLibProxySerializer.CGLibProxyMarker;

/**
 * A kryo serializer for cglib proxies. It needs to be registered for {@link CGLibProxyMarker} class.
 * When the serializer for a certain class is requested (via {@link Kryo#newSerializer(Class)})
 * {@link #canSerialize(Class)} has to be checked with the provided class to see if 
 * a {@link BeanFactoryBasedTargetSourceSerializer} should be returned.
 * 
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public class BeanFactoryBasedTargetSourceSerializer extends Serializer {

    private final Kryo _kryo;
    
    /**
     * Constructor.
     * 
     * @param kryo the kryo instance.
     */
    public BeanFactoryBasedTargetSourceSerializer( final Kryo kryo ) {
        _kryo = kryo;
    }

    @Override
    public <T> T readObjectData( final ByteBuffer buffer, final Class<T> type ) {
        return _kryo.readObjectData( buffer, type );
    }

    @Override
    public void writeObjectData( final ByteBuffer buffer, final Object object ) {
        try {
            _kryo.writeObjectData( buffer, new SingletonTargetSource(((AbstractBeanFactoryBasedTargetSource)object).getTarget()) );
        } catch ( final RuntimeException e ) {
            // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
            // handles SerializationException specifically (resizing the buffer)...
            throw e;
        } catch ( final Exception e ) {
            throw new RuntimeException( "Could not get target from targetSource " + object, e );
        }
    }

}
