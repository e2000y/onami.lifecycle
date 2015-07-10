package org.apache.onami.lifecycle.guava;

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

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import org.apache.onami.lifecycle.core.DefaultStager;
import org.apache.onami.lifecycle.core.DisposingStager;
import org.apache.onami.lifecycle.core.LifeCycleStageModule;

import com.google.common.util.concurrent.Service;

/**
 * Guice module to register methods to be invoked when {@link org.apache.onami.lifecycle.core.Stager#stage()} is invoked.
 * Module instance have state so it must not be used to construct more than one {@link com.google.inject.Injector}.
 *
 * @author Eric Yung
 */
public class GuavaServiceStopModule
    extends LifeCycleStageModule
{

    private final DisposingStager<Service> stager = new DefaultStager<Service>(
        Service.class, DefaultStager.Order.FIRST_IN_LAST_OUT );

    @Override
    protected void configureBindings()
    {
        bindStager( stager, new AbstractMatcher<TypeLiteral>()
        {
            public boolean matches( TypeLiteral  tl )
            {
                return stager.getStage().isAssignableFrom( tl.getRawType() );
            }
        }, null, "stopAsync" );

        bind( new TypeLiteral<DisposingStager<Service>>() {} ).toInstance( stager );
    }

    public DisposingStager<Service> getStager()
    {
        return stager;
    }

}
