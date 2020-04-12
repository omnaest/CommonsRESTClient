package org.omnaest.utils.rest.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FormBuilder
{
    private Map<String, String> formMap = new LinkedHashMap<>();

    public FormBuilder put(String key, String value)
    {
        this.formMap.put(key, value);
        return this;
    }

    public String build()
    {
        String form = this.formMap.entrySet()
                                  .stream()
                                  .map(entry -> entry.getKey() + "=" + entry.getValue())
                                  .collect(Collectors.joining("&"));
        return form;
    }
}
