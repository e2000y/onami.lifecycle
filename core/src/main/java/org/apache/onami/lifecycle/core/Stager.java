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

/**
 * A Stager is a mini-container that stages resources
 * invoking {@link Stageable#stage(StageHandler)}.
 * Order of disposal is specified by the concrete implementation of this
 * interface via {@link org.apache.onami.lifecycle.core.DefaultStager.Order}.
 * Implementations must be thread-safe because registration can be done from
 * any thread.
 */
public interface Stager<A>
{

    /**
     * Register a {@link Stageable} to stage resources.
     *
     * @param stageable object to be invoked to stage resources.
     */
    void register( Stageable stageable );

    /**
     * Stages resources invoking {@link Stageable#stage(StageHandler)}.
     */
    void stage();

    /**
     * Stages resources invoking {@link Stageable#stage(StageHandler)}.
     *
     * @param stageHandler the {@link StageHandler} instance that tracks progresses.
     * @since 0.2.0
     */
    void stage( StageHandler stageHandler );

    /**
     * Returns the annotation that represents this stage.
     *
     * @return stage annotation.
     */
    Class<A> getStage();

}
