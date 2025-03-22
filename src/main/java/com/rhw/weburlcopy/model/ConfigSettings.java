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
    private String contextPath = "";
    private String protocol = "http";
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
        // 移除任何协议前缀，因为这将单独存储
        if (host != null) {
            if (host.startsWith("http://")) {
                host = host.substring(7);
            } else if (host.startsWith("https://")) {
                host = host.substring(8);
            }
        }
        this.host = host;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        // 确保上下文路径始终以斜杠开头，且不以斜杠结尾
        if (contextPath == null) {
            this.contextPath = "";
            return;
        }
        
        String path = contextPath.trim();
        if (!path.isEmpty() && !path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        this.contextPath = path;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        // 确保协议是http或https
        if ("https".equalsIgnoreCase(protocol)) {
            this.protocol = "https";
        } else {
            this.protocol = "http";
        }
    }
    
    /**
     * 获取完整URL前缀（协议+主机+上下文路径）
     */
    public String getFullUrlPrefix() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(protocol).append("://").append(host);
        
        if (!contextPath.isEmpty()) {
            urlBuilder.append(contextPath);
        }
        
        return urlBuilder.toString();
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
