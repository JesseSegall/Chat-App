package edu.stevens.cs522.chat.rest.client;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/*
 * Filters out entity object fields that should not be exchanged with the server.
 * https://stackoverflow.com/a/27986860/1654265
 */
public class ExcludeStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes field) {
        return field.getAnnotation(Exclude.class) != null;
    }

}
