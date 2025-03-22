package com.rhw.weburlcopy.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置持久化类
 * 管理URL配置、请求头和默认参数设置
 * 
 * @author renhao.wang
 * @since 2023-03-22
 */
@State(
        name = "WebUrlCopySettings",
        storages = {@Storage("WebUrlCopySettings.xml")}
)
public class ConfigSettings implements PersistentStateComponent<ConfigSettings> {

    // 所有URL配置列表
    private List<UrlConfig> urlConfigs = new ArrayList<>();
    
    // 当前选中配置的ID
    private String activeConfigId;
    
    // 请求头映射
    private Map<String, String> headers = new HashMap<>();
    
    // 默认参数映射
    private Map<String, String> defaultParameters = new HashMap<>();
    
    /**
     * 默认构造函数
     * 初始化默认配置
     */
    public ConfigSettings() {
        // 创建默认本地主机配置
        UrlConfig defaultConfig = new UrlConfig("本地环境", "localhost", "", "http");
        urlConfigs.add(defaultConfig);
        activeConfigId = defaultConfig.getId();
    }

    /**
     * 获取插件配置实例
     * 
     * @param project 当前项目
     * @return 配置实例
     */
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
        
        // 确保至少有一个配置
        if (urlConfigs.isEmpty()) {
            UrlConfig defaultConfig = new UrlConfig("本地环境", "localhost", "", "http");
            urlConfigs.add(defaultConfig);
            activeConfigId = defaultConfig.getId();
        }
        
        // 确保有选中的配置
        if (activeConfigId == null || getActiveConfig() == null) {
            activeConfigId = urlConfigs.get(0).getId();
        }
    }
    
    /**
     * 获取URL配置列表
     * 
     * @return URL配置列表
     */
    public List<UrlConfig> getUrlConfigs() {
        return urlConfigs;
    }
    
    /**
     * 设置URL配置列表
     * 
     * @param urlConfigs URL配置列表
     */
    public void setUrlConfigs(List<UrlConfig> urlConfigs) {
        this.urlConfigs = urlConfigs;
    }
    
    /**
     * 获取当前活动配置的ID
     * 
     * @return 当前活动配置的ID
     */
    public String getActiveConfigId() {
        return activeConfigId;
    }
    
    /**
     * 设置当前活动配置的ID
     * 
     * @param activeConfigId 当前活动配置的ID
     */
    public void setActiveConfigId(String activeConfigId) {
        this.activeConfigId = activeConfigId;
    }
    
    /**
     * 获取当前活动配置
     * 
     * @return 当前活动配置，如果不存在则返回第一个配置
     */
    public UrlConfig getActiveConfig() {
        // 查找当前活动配置
        for (UrlConfig config : urlConfigs) {
            if (config.getId().equals(activeConfigId)) {
                return config;
            }
        }
        
        // 如果找不到活动配置，使用第一个配置
        if (!urlConfigs.isEmpty()) {
            activeConfigId = urlConfigs.get(0).getId();
            return urlConfigs.get(0);
        }
        
        // 如果没有配置，创建一个默认配置
        UrlConfig defaultConfig = new UrlConfig("本地环境", "localhost", "", "http");
        urlConfigs.add(defaultConfig);
        activeConfigId = defaultConfig.getId();
        return defaultConfig;
    }
    
    /**
     * 添加URL配置
     * 
     * @param config 要添加的配置
     */
    public void addUrlConfig(UrlConfig config) {
        urlConfigs.add(config);
        
        // 如果这是第一个配置，设置为活动配置
        if (urlConfigs.size() == 1) {
            activeConfigId = config.getId();
        }
    }
    
    /**
     * 删除URL配置
     * 
     * @param configId 要删除的配置ID
     * @return 是否删除成功
     */
    public boolean removeUrlConfig(String configId) {
        // 确保至少保留一个配置
        if (urlConfigs.size() <= 1) {
            return false;
        }
        
        // 查找并删除配置
        UrlConfig toRemove = null;
        for (UrlConfig config : urlConfigs) {
            if (config.getId().equals(configId)) {
                toRemove = config;
                break;
            }
        }
        
        if (toRemove != null) {
            urlConfigs.remove(toRemove);
            
            // 如果删除的是当前活动配置，选择第一个配置作为新的活动配置
            if (configId.equals(activeConfigId)) {
                activeConfigId = urlConfigs.get(0).getId();
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 更新URL配置
     * 
     * @param updatedConfig 更新后的配置
     * @return 是否更新成功
     */
    public boolean updateUrlConfig(UrlConfig updatedConfig) {
        for (int i = 0; i < urlConfigs.size(); i++) {
            if (urlConfigs.get(i).getId().equals(updatedConfig.getId())) {
                urlConfigs.set(i, updatedConfig);
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取主机地址
     * 兼容旧版本
     * 
     * @return 当前活动配置的主机地址
     */
    public String getHost() {
        return getActiveConfig().getHost();
    }

    /**
     * 设置主机地址
     * 兼容旧版本
     * 
     * @param host 主机地址
     */
    public void setHost(String host) {
        getActiveConfig().setHost(host);
    }

    /**
     * 获取上下文路径
     * 兼容旧版本
     * 
     * @return 当前活动配置的上下文路径
     */
    public String getContextPath() {
        return getActiveConfig().getContextPath();
    }

    /**
     * 设置上下文路径
     * 兼容旧版本
     * 
     * @param contextPath 上下文路径
     */
    public void setContextPath(String contextPath) {
        getActiveConfig().setContextPath(contextPath);
    }
    
    /**
     * 获取协议
     * 兼容旧版本
     * 
     * @return 当前活动配置的协议
     */
    public String getProtocol() {
        return getActiveConfig().getProtocol();
    }
    
    /**
     * 设置协议
     * 兼容旧版本
     * 
     * @param protocol 协议
     */
    public void setProtocol(String protocol) {
        getActiveConfig().setProtocol(protocol);
    }
    
    /**
     * 获取完整URL前缀（协议+主机+上下文路径）
     * 
     * @return 完整URL前缀
     */
    public String getFullUrlPrefix() {
        return getActiveConfig().getFullUrlPrefix();
    }

    /**
     * 获取请求头
     * 
     * @return 请求头映射
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 设置请求头
     * 
     * @param headers 请求头映射
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * 添加请求头
     * 
     * @param key 请求头名称
     * @param value 请求头值
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * 删除请求头
     * 
     * @param key 请求头名称
     */
    public void removeHeader(String key) {
        this.headers.remove(key);
    }
    
    /**
     * 获取默认参数
     * 
     * @return 默认参数映射
     */
    public Map<String, String> getDefaultParameters() {
        return defaultParameters;
    }
    
    /**
     * 设置默认参数
     * 
     * @param defaultParameters 默认参数映射
     */
    public void setDefaultParameters(Map<String, String> defaultParameters) {
        this.defaultParameters = defaultParameters;
    }
    
    /**
     * 添加默认参数
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void addDefaultParameter(String key, String value) {
        this.defaultParameters.put(key, value);
    }
    
    /**
     * 删除默认参数
     * 
     * @param key 参数名
     */
    public void removeDefaultParameter(String key) {
        this.defaultParameters.remove(key);
    }
    
    /**
     * 获取参数的默认值（如果配置了）
     * 
     * @param paramName 参数名
     * @return 默认值，如果没有配置则返回null
     */
    public String getDefaultParameterValue(String paramName) {
        return defaultParameters.get(paramName);
    }
    
    /**
     * 应用默认参数值到参数映射
     * 
     * @param parameters 原始参数映射
     * @return 应用默认值后的参数映射
     */
    public Map<String, String> applyDefaultParameters(Map<String, String> parameters) {
        Map<String, String> result = new HashMap<>(parameters);
        
        // 对每个参数，检查是否有默认值并应用
        for (String paramName : parameters.keySet()) {
            if (defaultParameters.containsKey(paramName)) {
                // 用默认值替换原始值
                result.put(paramName, defaultParameters.get(paramName));
            }
        }
        
        return result;
    }
}
