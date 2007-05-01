/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Service;
import org.apache.camel.util.ObjectHelper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * A default endpoint useful for implementation inheritance
 *
 * @version $Revision$
 */
public abstract class DefaultEndpoint<E extends Exchange> implements Endpoint<E> {
    private String endpointUri;
    private final Component component;
    private CamelContext context;
    private ScheduledExecutorService executorService;

    protected DefaultEndpoint(String endpointUri, Component component) {
        this.endpointUri = endpointUri;
        this.component = component;
        this.context = component.getCamelContext();
    }

    public int hashCode() {
        return endpointUri.hashCode() * 37 + 1;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DefaultEndpoint) {
            DefaultEndpoint that = (DefaultEndpoint) object;
            return ObjectHelper.equals(this.endpointUri, that.endpointUri);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Endpoint[" + endpointUri + "]";
    }

    public String getEndpointUri() {
        return endpointUri;
    }

    public CamelContext getContext() {
        return context;
    }

    public Component getComponent() {
        return component;
    }

    /**
     * @return the executor
     */
    public synchronized ScheduledExecutorService getExecutorService() {
        if (executorService == null) {
            Component c = getComponent();
            if (c != null && c instanceof DefaultComponent) {
                DefaultComponent dc = (DefaultComponent) c;
                executorService = dc.getExecutorService();
            }
            if (executorService == null) {
                executorService = createExecutorService();
            }
        }
        return executorService;
    }

    /**
     * @param executorService the executor to set
     */
    public synchronized void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Converts the given exchange to the specified exchange type
     */
    public E convertTo(Class<E> type, Exchange exchange) {
        // TODO we could infer type parameter
        if (type.isInstance(exchange)) {
            return type.cast(exchange);
        }
        return getContext().getExchangeConverter().convertTo(type, exchange);
    }

    public E createExchange(Exchange exchange) {
        E answer = createExchange();
        answer.copyFrom(exchange);
        return answer;
    }

    public E toExchangeType(Exchange exchange) {
        // TODO avoid cloning exchanges if E == Exchange!
        return createExchange(exchange);
    }

    /**
     * A helper method to reduce the clutter of implementors of {@link #createProducer()} and {@link #createConsumer(Processor)}
     */
    protected <T extends Service> T startService(T service) throws Exception {
        service.start();
        return service;
    }

    protected ScheduledThreadPoolExecutor createExecutorService() {
        return new ScheduledThreadPoolExecutor(10);
    }
}
