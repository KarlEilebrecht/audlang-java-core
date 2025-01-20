//@formatter:off
/*
 * DefaultArgNameValueMapper
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

package de.calamanari.adl.cnv;

import java.util.HashMap;
import java.util.Map;

import de.calamanari.adl.AudlangErrorInfo;
import de.calamanari.adl.CommonErrors;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class DefaultArgNameValueMapper implements ArgNameValueMapper {

    private static final long serialVersionUID = -3865296743066754989L;

    private final ArgNameValueMapping argNameValueMapping;

    /**
     * For mapper chaining
     */
    private final ArgNameValueMapper fallBackMapper;

    /**
     * Once created we cache a reverse mapper
     */
    private DefaultArgNameValueMapper reverseMapper = null;

    /**
     * Creates a new instance with an optional fallback mapper, so this mapper can concentrate on a subset of mappings
     * 
     * @param argNameValueMapping
     * @param fallBackMapper null turns the fallback feature off
     */
    public DefaultArgNameValueMapper(ArgNameValueMapping argNameValueMapping, ArgNameValueMapper fallBackMapper) {
        this.argNameValueMapping = argNameValueMapping;
        this.fallBackMapper = fallBackMapper;
    }

    /**
     * Creates a new instance without fallback mapper
     * 
     * @param argNameValueMapping
     */
    public DefaultArgNameValueMapper(ArgNameValueMapping argNameValueMapping) {
        this(argNameValueMapping, null);
    }

    @Override
    public QualifiedArgValue mapArgValue(String argName, String value) {
        QualifiedArgValue key = new QualifiedArgValue(argName, value);
        QualifiedArgValue res = argNameValueMapping.mappings().get(key);
        if (res == null) {
            if (fallBackMapper != null) {
                res = fallBackMapper.mapArgValue(argName, value);
            }
            else {
                AudlangErrorInfo errorInfo = AudlangErrorInfo.argValueError(CommonErrors.ERR_3000_MAPPING_FAILED, argName, value);
                throw new MappingNotFoundException(String.format("No suitable mapping for argName=%s, value=%s", argName, value), errorInfo);
            }
        }
        return res;
    }

    @Override
    public ArgNameValueMapper reverse() {
        if (reverseMapper == null) {
            this.reverseMapper = new DefaultArgNameValueMapper(this.argNameValueMapping.reverse(),
                    this.fallBackMapper == null ? null : this.fallBackMapper.reverse());
            this.reverseMapper.reverseMapper = this;
        }
        return reverseMapper;
    }

    @Override
    public boolean isArgumentStructurePreserving() {
        if (fallBackMapper != null && !fallBackMapper.isArgumentStructurePreserving()) {
            return false;
        }
        Map<String, String> nameToNameMap = new HashMap<>();
        for (Map.Entry<QualifiedArgValue, QualifiedArgValue> entry : argNameValueMapping.mappings().entrySet()) {
            QualifiedArgValue key = entry.getKey();
            QualifiedArgValue value = entry.getValue();
            String prevArgName = nameToNameMap.putIfAbsent(key.argName(), value.argName());
            if (prevArgName != null && !prevArgName.equals(value.argName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isBijective() {
        if (fallBackMapper != null && !fallBackMapper.isBijective()) {
            return false;
        }
        if (reverseMapper != null) {
            return true;
        }
        Map<QualifiedArgValue, QualifiedArgValue> reverseMappings = new HashMap<>();
        for (Map.Entry<QualifiedArgValue, QualifiedArgValue> entry : argNameValueMapping.mappings().entrySet()) {
            QualifiedArgValue key = entry.getValue();
            QualifiedArgValue value = entry.getKey();
            if (reverseMappings.containsKey(key)) {
                return false;
            }
            reverseMappings.put(key, value);
        }
        return true;
    }

}
