package com.rhw.weburlcopy.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置持久化类
 */
@State(
        name = "WebUrlCopySettings",
        storages = {@Storage("WebUrlCopySettings.xml")}
)
public class ConfigSettings implements PersistentStateComponent<ConfigSettings> {

    private String host = "localhost";
    private Map<String, String> headers = new HashMap<>();

    public static ConfigSettings getInstance(Project project) {
        return project.getService(ConfigSettings.class);
    }

    @Nullable
    @Override
    public ConfigSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConfigSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void removeHeader(String key) {
        this.headers.remove(key);
    }
}
