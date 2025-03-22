package com.rhw.weburlcopy.util;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.rhw.weburlcopy.model.ConfigSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求工具类
 */
public class RequestUtil {

    /**
     * 判断是否为控制器类
     */
    public static boolean isControllerClass(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName != null && (
                    qualifiedName.equals("org.springframework.web.bind.annotation.RestController") ||
                    qualifiedName.equals("org.springframework.stereotype.Controller") ||
                    qualifiedName.equals("javax.ws.rs.Path")
            )) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为请求处理方法
     */
    public static boolean isRequestMethod(PsiMethod psiMethod) {
        if (psiMethod == null) {
            return false;
        }
        PsiAnnotation[] annotations = psiMethod.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName != null && (
                    qualifiedName.contains("RequestMapping") ||
                    qualifiedName.contains("GetMapping") ||
                    qualifiedName.contains("PostMapping") ||
                    qualifiedName.contains("PutMapping") ||
                    qualifiedName.contains("DeleteMapping") ||
                    qualifiedName.contains("PatchMapping") ||
                    qualifiedName.equals("javax.ws.rs.GET") ||
                    qualifiedName.equals("javax.ws.rs.POST") ||
                    qualifiedName.equals("javax.ws.rs.PUT") ||
                    qualifiedName.equals("javax.ws.rs.DELETE")
            )) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取请求路径
     */
    public static String getRequestPath(PsiMethod method) {
        if (method == null) {
            return "";
        }
        
        PsiClass containingClass = method.getContainingClass();
        String classPath = "";
        if (containingClass != null) {
            classPath = getRequestPathFromAnnotation(containingClass.getAnnotations());
        }
        
        String methodPath = getRequestPathFromAnnotation(method.getAnnotations());
        
        // 拼接并清理路径
        String path = classPath + methodPath;
        if (path.isEmpty()) {
            return "/";
        }
        
        // 规范化路径格式
        path = path.replaceAll("//", "/");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        return path;
    }

    /**
     * 从注解中获取请求路径
     */
    private static String getRequestPathFromAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) continue;

            // Spring MVC注解
            if (qualifiedName.contains("Mapping")) {
                PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
                for (PsiNameValuePair attribute : attributes) {
                    String name = attribute.getName();
                    if (name == null || name.equals("value") || name.equals("path")) {
                        // 处理数组形式的值，例如 @RequestMapping(value = {"/path1", "/path2"})
                        PsiElement valueElement = attribute.getValue();
                        if (valueElement instanceof PsiArrayInitializerMemberValue) {
                            // 处理数组形式，获取第一个值
                            PsiArrayInitializerMemberValue arrayValue = (PsiArrayInitializerMemberValue) valueElement;
                            PsiAnnotationMemberValue[] initializers = arrayValue.getInitializers();
                            if (initializers.length > 0) {
                                // 获取第一个元素并提取字符串值
                                String arrayFirstValue = extractStringFromMemberValue(initializers[0]);
                                if (arrayFirstValue != null) {
                                    return arrayFirstValue;
                                }
                            }
                        } else {
                            // 处理单个值形式
                            String literalValue = attribute.getLiteralValue();
                            if (literalValue != null) {
                                return literalValue;
                            }
                        }
                    }
                }
            }
            
            // JAX-RS注解
            if (qualifiedName.equals("javax.ws.rs.Path")) {
                PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
                for (PsiNameValuePair attribute : attributes) {
                    String value = attribute.getLiteralValue();
                    if (value != null) {
                        return value;
                    }
                }
            }
        }
        return "";
    }
    
    /**
     * 从PsiAnnotationMemberValue中提取字符串值
     */
    private static String extractStringFromMemberValue(PsiAnnotationMemberValue memberValue) {
        if (memberValue instanceof PsiLiteralExpression) {
            Object value = ((PsiLiteralExpression) memberValue).getValue();
            return value != null ? value.toString() : null;
        } else {
            // 尝试获取文本并清理引号
            String text = memberValue.getText();
            if (text != null && text.startsWith("\"") && text.endsWith("\"") && text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
            return text;
        }
    }

    /**
     * 判断请求方法类型
     */
    public static String getRequestMethod(PsiMethod method) {
        if (method == null) {
            return "GET";
        }
        
        PsiAnnotation[] annotations = method.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) continue;
            
            // Spring MVC
            if (qualifiedName.contains("GetMapping")) {
                return "GET";
            } else if (qualifiedName.contains("PostMapping")) {
                return "POST";
            } else if (qualifiedName.contains("PutMapping")) {
                return "PUT";
            } else if (qualifiedName.contains("DeleteMapping")) {
                return "DELETE";
            } else if (qualifiedName.contains("PatchMapping")) {
                return "PATCH";
            } else if (qualifiedName.contains("RequestMapping")) {
                PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
                for (PsiNameValuePair attribute : attributes) {
                    String name = attribute.getName();
                    if ("method".equals(name)) {
                        String value = attribute.getValue().getText();
                        if (value.contains("GET")) {
                            return "GET";
                        } else if (value.contains("POST")) {
                            return "POST";
                        } else if (value.contains("PUT")) {
                            return "PUT";
                        } else if (value.contains("DELETE")) {
                            return "DELETE";
                        } else if (value.contains("PATCH")) {
                            return "PATCH";
                        }
                    }
                }
            }
            
            // JAX-RS
            if (qualifiedName.equals("javax.ws.rs.GET")) {
                return "GET";
            } else if (qualifiedName.equals("javax.ws.rs.POST")) {
                return "POST";
            } else if (qualifiedName.equals("javax.ws.rs.PUT")) {
                return "PUT";
            } else if (qualifiedName.equals("javax.ws.rs.DELETE")) {
                return "DELETE";
            }
        }
        
        // 默认为GET
        return "GET";
    }

    /**
     * 获取参数Map
     */
    public static Map<String, String> getParameters(PsiMethod method) {
        Map<String, String> parameters = new HashMap<>();
        if (method == null) {
            return parameters;
        }
        
        PsiParameter[] params = method.getParameterList().getParameters();
        boolean hasJsonParam = false;
        
        for (PsiParameter param : params) {
            PsiType type = param.getType();
            
            // 检查是否是JSON参数（复杂对象且不是基本类型或String）
            if (!isPrimitiveOrString(type)) {
                if (isRequestBody(param)) {
                    // 对于@RequestBody, 尝试解析对象属性
                    parameters.put("body", generateJsonForType(type));
                    hasJsonParam = true;
                } else {
                    // 对于未标记为@RequestBody但也不是基本类型的参数，视为JSON类型
                    String complexTypeName = type.getPresentableText();
                    if (!complexTypeName.startsWith("java.") && !type.equals(PsiType.VOID)) {
                        parameters.put(param.getName(), generateJsonForType(type));
                        hasJsonParam = true;
                    }
                }
            } else {
                // 处理基本类型和String
                String paramName = param.getName();
                String defaultValue = getDefaultValueForType(type);
                parameters.put(paramName, defaultValue);
            }
        }
        
        // 设置标记，表示包含JSON参数
        if (hasJsonParam) {
            parameters.put("_hasJsonParam", "true");
        }
        
        return parameters;
    }

    /**
     * 判断是否是基本类型或String
     */
    private static boolean isPrimitiveOrString(PsiType type) {
        String canonicalText = type.getCanonicalText();
        return PsiType.BOOLEAN.equals(type) || 
               PsiType.BYTE.equals(type) || 
               PsiType.CHAR.equals(type) || 
               PsiType.DOUBLE.equals(type) || 
               PsiType.FLOAT.equals(type) || 
               PsiType.INT.equals(type) || 
               PsiType.LONG.equals(type) || 
               PsiType.SHORT.equals(type) || 
               canonicalText.equals("java.lang.String") ||
               canonicalText.equals("java.lang.Boolean") ||
               canonicalText.equals("java.lang.Integer") ||
               canonicalText.equals("java.lang.Long") ||
               canonicalText.equals("java.lang.Double") ||
               canonicalText.equals("java.lang.Float");
    }

    /**
     * 判断参数是否是@RequestBody
     */
    private static boolean isRequestBody(PsiParameter param) {
        PsiAnnotation[] annotations = param.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String name = annotation.getQualifiedName();
            if (name != null && name.endsWith("RequestBody")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据类型生成默认值
     */
    private static String getDefaultValueForType(PsiType type) {
        if (PsiType.BOOLEAN.equals(type) || type.getCanonicalText().equals("java.lang.Boolean")) {
            return "false";
        } else if (PsiType.INT.equals(type) || type.getCanonicalText().equals("java.lang.Integer") || 
                  PsiType.LONG.equals(type) || type.getCanonicalText().equals("java.lang.Long") ||
                  PsiType.SHORT.equals(type) || 
                  PsiType.BYTE.equals(type)) {
            return "1";
        } else if (PsiType.DOUBLE.equals(type) || type.getCanonicalText().equals("java.lang.Double") ||
                  PsiType.FLOAT.equals(type) || type.getCanonicalText().equals("java.lang.Float")) {
            return "1.0";
        } else if (type.getCanonicalText().equals("java.lang.String")) {
            return "x";
        } else {
            return "";
        }
    }

    /**
     * 为复杂类型生成JSON
     */
    private static String generateJsonForType(PsiType type) {
        if (!(type instanceof PsiClassType)) {
            return "{}";
        }
        
        PsiClass psiClass = ((PsiClassType) type).resolve();
        if (psiClass == null) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        PsiField[] fields = psiClass.getAllFields();
        boolean first = true;
        
        for (PsiField field : fields) {
            if (field.hasModifierProperty(PsiModifier.STATIC) || 
                field.hasModifierProperty(PsiModifier.TRANSIENT)) {
                continue;
            }
            
            if (!first) {
                json.append(", ");
            }
            
            String fieldName = field.getName();
            PsiType fieldType = field.getType();
            
            json.append("\"").append(fieldName).append("\": ");
            
            if (isPrimitiveOrString(fieldType)) {
                if (fieldType.getCanonicalText().equals("java.lang.String")) {
                    json.append("\"").append(getDefaultValueForType(fieldType)).append("\"");
                } else {
                    json.append(getDefaultValueForType(fieldType));
                }
            } else {
                json.append("{}");
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }

    /**
     * 生成curl命令
     */
    public static String generateCurlCommand(Project project, PsiMethod method) {
        if (method == null) {
            return "";
        }
        
        String requestMethod = getRequestMethod(method);
        String path = getRequestPath(method);
        Map<String, String> parameters = getParameters(method);
        
        // 如果有JSON参数，强制使用POST方法
        boolean hasJsonParam = parameters.containsKey("_hasJsonParam");
        if (hasJsonParam) {
            requestMethod = "POST";
        }
        parameters.remove("_hasJsonParam"); // 移除标记
        
        ConfigSettings settings = ConfigSettings.getInstance(project);
        String host = settings.getHost();
        String contextPath = settings.getContextPath();
        Map<String, String> headers = settings.getHeaders();
        
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(requestMethod).append(" ");
        
        // 添加headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            curl.append("-H \"").append(entry.getKey()).append(": ").append(entry.getValue()).append("\" ");
        }
        
        // 构建URL
        curl.append("\"");
        // 使用完整URL前缀，包含协议、主机和上下文路径
        curl.append(settings.getFullUrlPrefix());
        
        // 添加API路径，确保路径之间只有一个斜杠
        if (settings.getContextPath().endsWith("/") && path.startsWith("/")) {
            curl.append(path.substring(1));
        } else if (!settings.getContextPath().endsWith("/") && !path.startsWith("/")) {
            curl.append("/").append(path);
        } else {
            curl.append(path);
        }
        
        // 处理参数
        if (requestMethod.equals("GET") && !parameters.isEmpty() && !parameters.containsKey("body")) {
            curl.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (!first) {
                    curl.append("&");
                }
                curl.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        } else if (parameters.containsKey("body")) {
            // 处理RequestBody
            curl.append("\" -H \"Content-Type: application/json\" ");
            curl.append("-d '").append(parameters.get("body")).append("'");
        } else if (!parameters.isEmpty()) {
            // 处理POST参数
            curl.append("\" ");
            if (!headers.containsKey("Content-Type")) {
                // 如果有JSON参数，使用JSON Content-Type
                if (hasJsonParam) {
                    curl.append("-H \"Content-Type: application/json\" ");
                    curl.append("-d '{");
                    boolean first = true;
                    for (Map.Entry<String, String> entry : parameters.entrySet()) {
                        if (!first) {
                            curl.append(", ");
                        }
                        curl.append("\"").append(entry.getKey()).append("\": ");
                        // 尝试检测值是否已经是JSON格式
                        String value = entry.getValue();
                        if (value.startsWith("{") && value.endsWith("}")) {
                            curl.append(value);
                        } else if (value.matches("\\d+(\\.\\d+)?")) {
                            curl.append(value); // 数字不加引号
                        } else if (value.equals("true") || value.equals("false")) {
                            curl.append(value); // 布尔值不加引号
                        } else {
                            curl.append("\"").append(value).append("\"");
                        }
                        first = false;
                    }
                    curl.append("}'");
                } else {
                    curl.append("-H \"Content-Type: application/x-www-form-urlencoded\" ");
                    curl.append("-d \"");
                    boolean first = true;
                    for (Map.Entry<String, String> entry : parameters.entrySet()) {
                        if (!first) {
                            curl.append("&");
                        }
                        curl.append(entry.getKey()).append("=").append(entry.getValue());
                        first = false;
                    }
                    curl.append("\"");
                }
            }
        } else {
            curl.append("\"");
        }
        
        return curl.toString();
    }

    /**
     * 生成Python请求代码
     */
    public static String generatePythonRequest(Project project, PsiMethod method) {
        if (method == null) {
            return "";
        }
        
        String requestMethod = getRequestMethod(method);
        String path = getRequestPath(method);
        Map<String, String> parameters = getParameters(method);
        
        // 如果有JSON参数，强制使用POST方法
        boolean hasJsonParam = parameters.containsKey("_hasJsonParam");
        if (hasJsonParam) {
            requestMethod = "POST";
        }
        parameters.remove("_hasJsonParam"); // 移除标记
        
        ConfigSettings settings = ConfigSettings.getInstance(project);
        String host = settings.getHost();
        String contextPath = settings.getContextPath();
        Map<String, String> headers = settings.getHeaders();
        
        StringBuilder python = new StringBuilder();
        python.append("import requests\n\n");
        
        // 构建URL
        python.append("url = \"");
        // 使用完整URL前缀，包含协议、主机和上下文路径
        python.append(settings.getFullUrlPrefix());
        
        // 添加API路径，确保路径之间只有一个斜杠
        if (settings.getContextPath().endsWith("/") && path.startsWith("/")) {
            python.append(path.substring(1));
        } else if (!settings.getContextPath().endsWith("/") && !path.startsWith("/")) {
            python.append("/").append(path);
        } else {
            python.append(path);
        }
        python.append("\"\n");
        
        // 添加headers
        if (!headers.isEmpty()) {
            python.append("headers = {\n");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                python.append("    \"").append(entry.getKey()).append("\": \"")
                      .append(entry.getValue()).append("\",\n");
            }
            python.append("}\n");
        } else {
            python.append("headers = {}\n");
        }
        
        // 处理参数
        if (parameters.containsKey("body")) {
            python.append("payload = ").append(parameters.get("body")).append("\n");
            python.append("response = requests.").append(requestMethod.toLowerCase())
                  .append("(url, json=payload, headers=headers)\n");
        } else if (!parameters.isEmpty()) {
            if (hasJsonParam) {
                // 处理JSON格式参数
                python.append("json_data = {\n");
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    String value = entry.getValue();
                    if (value.startsWith("{") && value.endsWith("}")) {
                        // 已经是JSON格式
                        python.append("    \"").append(entry.getKey()).append("\": ")
                              .append(value).append(",\n");
                    } else if (value.matches("\\d+(\\.\\d+)?")) {
                        // 数字不需要引号
                        python.append("    \"").append(entry.getKey()).append("\": ")
                              .append(value).append(",\n");
                    } else if (value.equals("true") || value.equals("false")) {
                        // 布尔值不需要引号，且Python使用首字母大写
                        python.append("    \"").append(entry.getKey()).append("\": ")
                              .append(value.substring(0, 1).toUpperCase() + value.substring(1)).append(",\n");
                    } else {
                        python.append("    \"").append(entry.getKey()).append("\": \"")
                              .append(value).append("\",\n");
                    }
                }
                python.append("}\n");
                python.append("response = requests.").append(requestMethod.toLowerCase())
                      .append("(url, json=json_data, headers=headers)\n");
            } else {
                // 处理常规参数
                python.append("params = {\n");
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    String value = entry.getValue();
                    if (value.matches("\\d+(\\.\\d+)?")) {
                        // 数字不需要引号
                        python.append("    \"").append(entry.getKey()).append("\": ")
                              .append(value).append(",\n");
                    } else if (value.equals("true") || value.equals("false")) {
                        // 布尔值不需要引号，且Python使用首字母大写
                        python.append("    \"").append(entry.getKey()).append("\": ")
                              .append(value.substring(0, 1).toUpperCase() + value.substring(1)).append(",\n");
                    } else {
                        python.append("    \"").append(entry.getKey()).append("\": \"")
                              .append(value).append("\",\n");
                    }
                }
                python.append("}\n");
                
                if (requestMethod.equals("GET")) {
                    python.append("response = requests.get(url, params=params, headers=headers)\n");
                } else {
                    python.append("response = requests.").append(requestMethod.toLowerCase())
                          .append("(url, data=params, headers=headers)\n");
                }
            }
        } else {
            python.append("response = requests.").append(requestMethod.toLowerCase())
                  .append("(url, headers=headers)\n");
        }
        
        python.append("\n# 打印响应\n");
        python.append("print(response.status_code)\n");
        python.append("print(response.text)\n");
        
        return python.toString();
    }
}
