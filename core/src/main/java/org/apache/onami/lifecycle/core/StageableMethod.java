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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A {@link StageableMethod} is a reference to a stageable injectee
 * and related method to release resources.
 */
final class StageableMethod
    extends AbstractBasicStageable<Object>
{

    /**
     * The method to be invoked to stage resources.
     */
    private final Method stageMethod;

    /**
     * Creates a new {@link StageableMethod} reference.
     *
     * @param stageMethod the method to be invoked to stage resources.
     * @param injectee    the target injectee has to stage the resources.
     */
    StageableMethod( Method stageMethod, Object injectee )
    {
        super( injectee );
        this.stageMethod = stageMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void stage( StageHandler stageHandler )
    {
        try
        {
            AccessController.doPrivileged( new PrivilegedAction<Void>()
            {

                @Override
                public Void run()
                {
                    stageMethod.setAccessible( true );
                    return null;
                }

            } );
            stageMethod.invoke( object );
        }
        catch ( InvocationTargetException e )
        {
            stageHandler.onError( object, e.getCause() );
            return;
        }
        catch ( Throwable e )
        {
            stageHandler.onError( object, e );
            return;
        }
        stageHandler.onSuccess( object );
    }

}
