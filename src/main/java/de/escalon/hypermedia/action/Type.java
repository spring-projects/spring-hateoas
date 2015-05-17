/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.action;

/**
 * Specifies explicit HTML5 input types.
 *
 * @author Dietrich Schulten
 */
public enum Type {
    /**
     * Determine input type text or number automatically, depending on the annotated parameter
     */
    FROM_JAVA(null),
    /**
     * input type text
     */
    TEXT("text"),
//    /** input type checkbox */
//    CHECKBOX("checkbox"),
    /**
     * input type hidden
     */
    HIDDEN("hidden"),
    /**
     * input type password
     */
    PASSWORD("password"), COLOR("color"), DATE("date"), DATETIME("datetime"), DATETIME_LOCAL("datetime-local"), EMAIL(
            "email"), MONTH("month"), NUMBER("number"), RANGE("range"), SEARCH("search"), TEL("tel"), TIME("time"), URL("url"), WEEK(
            "week"), SUBMIT("submit");

    private String value;

    Type(String value) {
        this.value = value;
    }

    /**
     * Returns the correct html input type string value, or null if type should be determined from Java type.
     */
    public String toString() {
        return value;
    }

}
