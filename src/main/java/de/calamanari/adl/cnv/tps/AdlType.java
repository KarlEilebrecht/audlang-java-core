//@formatter:off
/*
 * AdlType
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

import java.io.Serializable;

import de.calamanari.adl.irl.CoreExpression;

/**
 * {@link AdlType} is an interface to describe a target type in a flexible (potentially extendible) type system.
 * <p/>
 * While Audlang itself is type-agnostic (see also
 * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#2-type-conventions">§2 Audience Definition
 * Language Specification</a>), databases are usually not. {@link AdlType}s allow to <i>negotiate and translate</i> values to overcome the impedance mismatch
 * between {@link CoreExpression}s and any underlying storage layer.
 * <p/>
 * Two types should be behave in the same way if they carry the same {@link #name()}, however, typically different instances should have unique names.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface AdlType extends Serializable {

    /**
     * @return unique name (identifier) of this type
     */
    String name();

    /**
     * @return formatter to format values from an expression in a the correct way
     */
    ArgValueFormatter getFormatter();

    /**
     * Types can be decorated (caster/formatter). This method returns the original type that was originally decorated ("center of the onion"). However,
     * decoration means different formatter or type caster than originally. Be aware that the same base type does not guarantee that the native fields
     * underneath are directly compatible to each other because the {@link AdlType} is <i>the type we make a native field compatible <b>to</b></i> by providing
     * a formatter or a caster.
     * 
     * @return either this type if it is a base type or the type that was decorated
     */
    default AdlType getBaseType() {
        return this;
    }

    /**
     * Returns a native type caster for bridging incompatible types when dealing with target storage layers.
     * <p/>
     * The default implementation returns the {@link PassThroughTypeCaster}.
     * 
     * @return type caster
     */
    default NativeTypeCaster getNativeTypeCaster() {
        return PassThroughTypeCaster.getInstance();
    }

    /**
     * Tells if this type supports the Audlang-operator CONTAINS
     * <p/>
     * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#36-contains-text-snippet">§3.6
     * Audlang Spec</a>
     * 
     * @return true if the type supports the CONTAINS operator
     */
    boolean supportsContains();

    /**
     * Tells if this type supports the Audlang-operators LESS THAN and GREATER THAN.
     * <p/>
     * See also
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3
     * Audlang Spec</a>
     * 
     * @return true if the type supports less-than/greater-than comparisons
     */
    boolean supportsLessThanGreaterThan();

    /**
     * Allows adding a type caster to an existing type to refine the behavior of the composed {@link AdlType}
     * <p/>
     * Specifying a custom name may be useful if you know that the effectively <i>identical</i> type setup would otherwise occur multiple times with different
     * names (edge-case). Usually, the auto-generated wrapper names should be preferred.
     * 
     * @param name unique name (or null to auto-generate a unique one)
     * @param nativeTypeCaster
     * @return composed type
     */
    default AdlType withNativeTypeCaster(String name, NativeTypeCaster nativeTypeCaster) {
        if (nativeTypeCaster == null) {
            return this;
        }
        return new AdlTypeDecorator(name, this, null, nativeTypeCaster);
    }

    /**
     * Allows adding a type caster to an existing type to refine the behavior of the composed {@link AdlType}
     * 
     * @param nativeTypeCaster
     * @return composed type or <i>this instance</i> if the provided type caster was null
     */
    default AdlType withNativeTypeCaster(NativeTypeCaster nativeTypeCaster) {
        return withNativeTypeCaster(null, nativeTypeCaster);
    }

    /**
     * Allows adding a new formatter to an existing type to refine the behavior of the composed {@link AdlType}
     * <p/>
     * Specifying a custom name may be useful if you know that the effectively <i>identical</i> type setup would otherwise occur multiple times with different
     * names (edge-case). Usually, the auto-generated wrapper names should be preferred.
     * 
     * @param name unique name (or null to auto-generate a unique one)
     * @param formatter
     * @return composed type or <i>this instance</i> if the provided formatter was null
     */
    default AdlType withFormatter(String name, ArgValueFormatter formatter) {
        if (formatter == null) {
            return this;
        }
        return new AdlTypeDecorator(name, this, formatter, null);
    }

    /**
     * Allows adding a new formatter to an existing type to refine the behavior of the composed {@link AdlType}
     * 
     * @param formatter
     * @return composed type
     */
    default AdlType withFormatter(ArgValueFormatter formatter) {
        return withFormatter(null, formatter);
    }

}
