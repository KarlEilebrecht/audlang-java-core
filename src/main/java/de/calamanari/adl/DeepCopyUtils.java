//@formatter:off
/*
 * DeepCopyUtils
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

package de.calamanari.adl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Utility meant for testing purposes, serializes and de-serializes an object to create deep copy.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class DeepCopyUtils {

    /**
     * Creates a deep-copy of the given input object.
     * 
     * @param <T> serializable type of the object to be copied
     * @param input (null supported)
     * @return copy, unrelated to the input
     * @throws DeepCopyException
     */
    public static <T extends Serializable> T deepCopy(T input) throws DeepCopyException {
        if (input == null) {
            return input;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(input);
            }
            bos.close();

            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray()); ObjectInputStream ois = new ObjectInputStream(bis)) {
                @SuppressWarnings("unchecked")
                T res = (T) input.getClass().cast(ois.readObject());
                return res;
            }
        }
        catch (IOException | ClassNotFoundException | RuntimeException ex) {
            throw new DeepCopyException("Unable to copy the given object of type: " + input.getClass(), ex);
        }
    }

    private DeepCopyUtils() {
        // utility
    }

}
