/*
 * Copyright 2010 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.proofpoint.node.testing;

import com.google.common.base.Optional;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.proofpoint.node.NodeConfig;
import com.proofpoint.node.NodeInfo;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.weakref.jmx.guice.ExportBinder.newExporter;

public class TestingNodeModule
        implements Module
{
    // avoid having an accidental dependency on the environment name
    private static final AtomicLong nextId = new AtomicLong(ThreadLocalRandom.current().nextInt(1000000));

    private final String environment;
    private final Optional<String> pool;

    public TestingNodeModule()
    {
        this(Optional.<String>absent());
    }

    public TestingNodeModule(Optional<String> environment)
    {
        this(environment.or("test" + nextId.getAndIncrement()));
    }

    public TestingNodeModule(String environment)
    {
        this(environment, Optional.<String>absent());
    }

    public TestingNodeModule(String environment, Optional<String> pool)
    {
        checkArgument(!isNullOrEmpty(environment), "environment is null or empty");
        this.environment = environment;
        this.pool = checkNotNull(pool, "pool is null");
    }

    public TestingNodeModule(String environment, String pool)
    {
        this(environment, Optional.of(checkNotNull(pool, "pool is null")));
    }

    @Override
    public void configure(Binder binder)
    {
        binder.bind(NodeInfo.class).in(Scopes.SINGLETON);
        binder.bind(NodeConfig.class).toInstance(new NodeConfig()
                .setEnvironment(environment));
        newExporter(binder).export(NodeInfo.class).withGeneratedName();
    }
}
