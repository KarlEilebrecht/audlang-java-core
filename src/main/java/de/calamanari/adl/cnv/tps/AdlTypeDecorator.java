//@formatter:off
/*
 * AdlTypeDecorator
 * Copyright 2024 Karl Eilebrecht
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"):
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//@formatter:on

package de.calamanari.adl.cnv.tps;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Often an {@link AdlType}s behavior will be common resp. applicable in many scenarios but we want to change the formatter or add a type caster. To avoid
 * creating boiler-plate code the {@link AdlTypeDecorator} provides an easy solution by composition. A given type gets wrapped together with a new formatter or
 * type caster.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class AdlTypeDecorator implements AdlType {

    private static final long serialVersionUID = 2654441110457026318L;

    /**
     * Static counter to ensure we get unique names for the decorators
     */
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

    /**
     * the decorated type
     */
    private final AdlType delegate;

    /**
     * decorated formatter or null (use the one of the delegate)
     */
    private final ArgValueFormatter formatter;

    /**
     * decorated type caster or null (use the one of the delegate)
     */
    private final NativeTypeCaster nativeTypeCaster;

    /**
     * We append a number to the original name, this keeps the identifiers short and still informative regarding the base type
     */
    private final String decoratorName;

    @Override
    public AdlType getBaseType() {
        return delegate.getBaseType();
    }

    /**
     * @param name if null, the wrapper gets a unique id assigned as its name
     * @param delegate NOT NULL
     * @param formatter
     * @param nativeTypeCaster
     */
    AdlTypeDecorator(String name, AdlType delegate, ArgValueFormatter formatter, NativeTypeCaster nativeTypeCaster) {
        this.delegate = delegate;
        this.formatter = formatter;
        this.nativeTypeCaster = nativeTypeCaster;
        if (name != null) {
            this.decoratorName = name;
        }
        else if (delegate instanceof AdlTypeDecorator dec) {
            this.decoratorName = dec.getBaseName() + "-" + INSTANCE_COUNTER.incrementAndGet();
        }
        else {
            this.decoratorName = delegate.name() + "-" + INSTANCE_COUNTER.incrementAndGet();
        }
    }

    /**
     * @return the name of the inner type (center of the "type onion")
     */
    private String getBaseName() {
        if (delegate instanceof AdlTypeDecorator dec) {
            return dec.getBaseName();
        }
        return delegate.name();
    }

    @Override
    public String name() {
        return this.decoratorName;
    }

    @Override
    public ArgValueFormatter getFormatter() {
        return this.formatter == null ? delegate.getFormatter() : this.formatter;
    }

    @Override
    public NativeTypeCaster getNativeTypeCaster() {
        return this.nativeTypeCaster == null ? delegate.getNativeTypeCaster() : this.nativeTypeCaster;
    }

    @Override
    public boolean supportsContains() {
        return delegate.supportsContains();
    }

    @Override
    public boolean supportsLessThanGreaterThan() {
        return delegate.supportsLessThanGreaterThan();
    }

    @Override
    public String toString() {
        return this.decoratorName;
    }

}