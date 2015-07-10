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

import com.google.inject.*;
import org.junit.Test;
import org.junit.Assert;

import com.google.common.util.concurrent.Service;

import static com.google.inject.Guice.createInjector;

public final class GuavaServiceObjectTestCase
{
    public static class TestModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(GuavaServiceObject.class);
        }
    }

    @Test
    public void testGuavaServiceStartup()
    {
        GuavaServiceObject svc = createInjector( new GuavaServiceStartModule(), new TestModule() ).getInstance( GuavaServiceObject.class );
        //  startAsync call doStart
        Assert.assertEquals(svc.sb.toString(), "s");
    }

    @Test
    public void testGuavaServiceStop()
    {
        GuavaServiceStopModule mod = new GuavaServiceStopModule();
        Injector injector = createInjector( mod, new TestModule() );
        GuavaServiceObject  svc = injector.getInstance( GuavaServiceObject.class);

        mod.getStager().stage();

        //  stopAsync will NOT call doStop since it is NOT started yet
        Assert.assertEquals("", svc.sb.toString());
    }

    @Test
    public void testGuavaService()
    {
        GuavaServiceStopModule mod = new GuavaServiceStopModule();
        Injector injector = createInjector( new GuavaServiceStartModule(), mod, new TestModule() );
        GuavaServiceObject  svc = injector.getInstance( GuavaServiceObject.class);

        mod.getStager().stage();

        //  complete lifcycle for Guava service
        Assert.assertEquals(svc.sb.toString(), "sS");
    }


}
