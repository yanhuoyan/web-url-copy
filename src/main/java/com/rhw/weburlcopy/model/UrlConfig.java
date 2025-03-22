package com.rhw.weburlcopy.model;

import java.util.UUID;

/**
 * URL配置类
 * 表示单个URL配置项，包含名称、主机、上下文路径、协议等信息
 * 
 * @author renhao.wang
 * @since 2023-03-22
 */
public class UrlConfig {
    
    // 配置ID，用于唯一标识一个配置
    private String id;
    
    // 配置名称
    private String name;
    
    // 主机地址
    private String host;
    
    // 上下文路径
    private String contextPath;
    
    // 协议 (http/https)
    private String protocol;
    
    /**
     * 默认构造函数
     * 创建一个带有唯一ID的空配置
     */
    public UrlConfig() {
        this.id = UUID.randomUUID().toString();
        this.name = "";
        this.host = "localhost";
        this.contextPath = "";
        this.protocol = "http";
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param name 配置名称
     * @param host 主机地址
     * @param contextPath 上下文路径
     * @param protocol 协议
     */
    public UrlConfig(String name, String host, String contextPath, String protocol) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        setHost(host);
        setContextPath(contextPath);
        setProtocol(protocol);
    }
    
    /**
     * 获取配置ID
     * 
     * @return 配置ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 设置配置ID
     * 
     * @param id 配置ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * 获取配置名称
     * 
     * @return 配置名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置配置名称
     * 
     * @param name 配置名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取主机地址
     * 
     * @return 主机地址
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 设置主机地址
     * 移除任何协议前缀，因为这将单独存储
     * 
     * @param host 主机地址
     */
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
    
    /**
     * 获取上下文路径
     * 
     * @return 上下文路径
     */
    public String getContextPath() {
        return contextPath;
    }
    
    /**
     * 设置上下文路径
     * 确保上下文路径始终以斜杠开头，且不以斜杠结尾
     * 
     * @param contextPath 上下文路径
     */
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
    
    /**
     * 获取协议
     * 
     * @return 协议
     */
    public String getProtocol() {
        return protocol;
    }
    
    /**
     * 设置协议
     * 确保协议是http或https
     * 
     * @param protocol 协议
     */
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
     * 
     * @return 完整URL前缀
     */
    public String getFullUrlPrefix() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(protocol).append("://").append(host);
        
        if (!contextPath.isEmpty()) {
            urlBuilder.append(contextPath);
        }
        
        return urlBuilder.toString();
    }
    
    /**
     * 生成配置的显示名称
     * 如果设置了名称则使用名称，否则使用URL
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return getFullUrlPrefix();
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
} 