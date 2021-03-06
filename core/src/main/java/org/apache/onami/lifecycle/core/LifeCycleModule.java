package org.apache.onami.lifecycle.core;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.google.inject.AbstractModule;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.*;

import static com.google.inject.matcher.Matchers.any;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Guice module to register methods to be invoked after injection is complete.
 */
public abstract class LifeCycleModule
    extends AbstractModule
{

    protected interface InvocationResultHandler
    {
        public void afterInvocation(Object  obj)
            throws Throwable;
    }

    private final Logger  logger = Logger.getLogger(getClass().getName());

    /**
     * Binds lifecycle listener.
     */
    protected final void bindLifeCycle( final Class<?> clz, final String mtd )
    {
        bindLifeCycle( clz, mtd , null );
    }

    protected final void bindLifeCycle( final Class<?> clz, final String mtd, final InvocationResultHandler  handler )
    {
        logger.info("Lifecycle - bind to " + clz.getName() + " with " + mtd);

        bindListener( new AbstractMatcher<TypeLiteral>()
        {
            public boolean matches( TypeLiteral  tl )
            {
                return clz.isAssignableFrom( tl.getRawType() );
            }
        }, new TypeListener()
        {
            public final <I> void hear( TypeLiteral<I> type, TypeEncounter<I> encounter )
            {
                try
                {
                    final Method method = type.getRawType().getMethod( mtd, (Class[]) null );

                    encounter.register( new InjectionListener<I>()
                    {
                        @Override
                        public void afterInjection(I injectee)
                        {
                            logger.info("Lifecycle - before invoke " + mtd + " for " + injectee);

                            try
                            {
                                method.invoke( injectee );

                                logger.info("Lifecycle - after invoke " + mtd + " for " + injectee);

                                if (handler != null)
                                {
                                    handler.afterInvocation(injectee);
                                }
                            }
                            catch ( IllegalArgumentException e )
                            {
                                // should not happen, anyway...
                                throw new ProvisionException( format( "Method %s requires arguments", method ), e );
                            }
                            catch ( IllegalAccessException e )
                            {
                                throw new ProvisionException( format( "Impossible to access to %s on %s", method, injectee ), e );
                            }
                            catch ( InvocationTargetException e )
                            {
                                throw new ProvisionException( format( "An error occurred while invoking %s on %s", method, injectee ), e.getCause() );
                            }
                            catch ( Throwable th )
                            {
                                throw new ProvisionException( format( "An error occurred while invoking %s on %s", method, injectee ), th );
                            }
                        }
                    } );
                }
                catch (Exception  ex)
                {
                    encounter.addError(ex);
                }
            }
        } );
    }

    /**
     * Binds lifecycle listener.
     *
     * @param annotation the lifecycle annotation to be searched.
     */
    protected final void bindLifeCycle( Class<? extends Annotation> annotation )
    {
        bindLifeCycle( asList( annotation ), any() , null );
    }

    protected final void bindLifeCycle( Class<? extends Annotation> annotation, final InvocationResultHandler  handler )
    {
        bindLifeCycle( asList( annotation ), any() , handler );
    }

    /**
     * Binds lifecycle listener.
     *
     * @param annotation  the lifecycle annotation to be searched.
     * @param typeMatcher the filter for injectee types.
     */
    protected final void bindLifeCycle( Class<? extends Annotation> annotation, Matcher<? super TypeLiteral<?>> typeMatcher )
    {
        bindLifeCycle( asList( annotation ), typeMatcher, null );
    }

    /**
     * Binds lifecycle listener.
     *
     * @param annotations  the lifecycle annotations to be searched in the order to be searched.
     * @param typeMatcher the filter for injectee types.
     */
    protected final void bindLifeCycle( List<? extends Class<? extends Annotation>> annotations, Matcher<? super TypeLiteral<?>> typeMatcher )
    {
        bindLifeCycle( annotations, typeMatcher, null );
    }

    protected final void bindLifeCycle( List<? extends Class<? extends Annotation>> annotations, Matcher<? super TypeLiteral<?>> typeMatcher, final InvocationResultHandler  handler )
    {
        logger.info("Lifecycle - bind to " + annotations + " with matcher " + typeMatcher);

        bindListener( typeMatcher, new AbstractMethodTypeListener( annotations )
        {

            @Override
            protected <I> void hear( final Method method, TypeLiteral<I> parentType, TypeEncounter<I> encounter,
                                     final Class<? extends Annotation> annotationType )
            {
                encounter.register( new InjectionListener<I>()
                {

                    @Override
                    public void afterInjection( I injectee )
                    {
                        logger.info("Lifecycle - before invoke " + method.getName() + " for " + injectee);

                        try
                        {
                            method.invoke( injectee );

                            logger.info("Lifecycle - after invoke " + method.getName() + " for " + injectee);

                            if (handler != null)
                            {
                                handler.afterInvocation(injectee);
                            }
                        }
                        catch ( IllegalArgumentException e )
                        {
                            // should not happen, anyway...
                            throw new ProvisionException(
                                format( "Method @%s %s requires arguments", annotationType.getName(), method ), e );
                        }
                        catch ( IllegalAccessException e )
                        {
                            throw new ProvisionException(
                                format( "Impossible to access to @%s %s on %s", annotationType.getName(), method,
                                        injectee ), e );
                        }
                        catch ( InvocationTargetException e )
                        {
                            throw new ProvisionException(
                                format( "An error occurred while invoking @%s %s on %s", annotationType.getName(),
                                        method, injectee ), e.getCause() );
                        }
                        catch ( Throwable th )
                        {
                            throw new ProvisionException(
                                format( "An error occurred while invoking @%s %s on %s", annotationType.getName(),
                                        method, injectee ), th );
                        }
                    }

                } );
            }

        } );
    }

}
