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
     * 
     * @param psiClass 需要判断的类
     * @return 如果是控制器类则返回true，否则返回false
     */
    public static boolean isControllerClass(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        
        try {
            // 检查注解
            PsiAnnotation[] annotations = psiClass.getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                String qualifiedName = annotation.getQualifiedName();
                if (qualifiedName != null && (
                        qualifiedName.contains("Controller") || // 更宽松的匹配，包括各种Controller注解
                        qualifiedName.contains("RestController") || 
                        qualifiedName.contains("Path") // 支持JAX-RS的Path注解
                )) {
                    return true;
                }
            }
            
            // 检查类名是否包含Controller
            String className = psiClass.getName();
            if (className != null && (
                    className.contains("Controller") || 
                    className.contains("Resource") || 
                    className.contains("Api") ||
                    className.contains("Endpoint")
            )) {
                return true;
            }
            
            // 检查父类和接口
            PsiClass[] interfaces = psiClass.getInterfaces();
            for (PsiClass anInterface : interfaces) {
                String interfaceName = anInterface.getQualifiedName();
                if (interfaceName != null && (
                        interfaceName.contains("Controller") ||
                        interfaceName.contains("Resource") ||
                        interfaceName.contains("Api")
                )) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 捕获任何异常，防止插件崩溃
            return false;
        }
        
        return false;
    }

    /**
     * 判断是否为请求处理方法
     * 
     * @param psiMethod 需要判断的方法
     * @return 如果是请求处理方法则返回true，否则返回false
     */
    public static boolean isRequestMethod(PsiMethod psiMethod) {
        if (psiMethod == null) {
            return false;
        }
        
        try {
            // 首先检查所在类是否为控制器
            PsiClass containingClass = psiMethod.getContainingClass();
            if (containingClass != null && isControllerClass(containingClass)) {
                // 检查方法注解
                PsiAnnotation[] annotations = psiMethod.getAnnotations();
                if (annotations.length == 0) {
                    // 如果方法没有注解但所在类是控制器，也认为是API方法
                    return true;
                }
                
                for (PsiAnnotation annotation : annotations) {
                    String qualifiedName = annotation.getQualifiedName();
                    if (qualifiedName != null && (
                            qualifiedName.contains("Mapping") || // 匹配各种Mapping注解
                            qualifiedName.contains("GET") ||
                            qualifiedName.contains("POST") ||
                            qualifiedName.contains("PUT") ||
                            qualifiedName.contains("DELETE") ||
                            qualifiedName.contains("PATCH") ||
                            qualifiedName.contains("Path") // JAX-RS Path注解
                    )) {
                        return true;
                    }
                }
                
                // 没有特定的注解，但方法是公开的，也视为API
                if (psiMethod.hasModifierProperty(PsiModifier.PUBLIC)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 捕获任何异常，防止插件崩溃
            return false;
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
        boolean hasComplexObjectParam = false;
        
        for (PsiParameter param : params) {
            PsiType type = param.getType();
            
            // 检查是否是JSON参数（复杂对象且不是基本类型或String）
            if (!isPrimitiveOrString(type)) {
                if (isRequestBody(param)) {
                    // 对于@RequestBody, 尝试解析对象属性
                    parameters.put("body", generateJsonForType(type));
                    hasJsonParam = true;
                } else {
                    // 对于未标记为@RequestBody但也不是基本类型的参数，视为复杂对象
                    String complexTypeName = type.getPresentableText();
                    if (!complexTypeName.startsWith("java.") && !type.equals(PsiType.VOID)) {
                        // 默认将复杂对象拆分为多个参数，除非明确标记为RequestBody
                        Map<String, String> objectParams = extractObjectParameters(type);
                        // 为对象参数添加前缀（参数名）
                        Map<String, String> prefixedParams = new HashMap<>(objectParams);
                        parameters.putAll(prefixedParams);
                        hasComplexObjectParam = true;
                    }
                }
            } else {
                // 处理基本类型和String
                // 首先尝试从@RequestParam注解中获取参数名
                String paramName = getRequestParamValue(param);
                if (paramName == null) {
                    paramName = param.getName(); // 如果没有注解或注解没有value值，使用参数原名
                }
                String defaultValue = getDefaultValueForType(type);
                parameters.put(paramName, defaultValue);
            }
        }
        
        // 设置标记，表示包含JSON参数
        if (hasJsonParam) {
            parameters.put("_hasJsonParam", "true");
        }
        
        // 设置标记，表示包含复杂对象参数但不是JSON
        if (hasComplexObjectParam && !hasJsonParam) {
            parameters.put("_hasComplexObjectParam", "true");
        }
        
        return parameters;
    }

    /**
     * 从@RequestParam注解中获取value值
     * 
     * @param param 参数对象
     * @return 注解中指定的参数名，如果没有则返回null
     */
    private static String getRequestParamValue(PsiParameter param) {
        PsiAnnotation[] annotations = param.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String name = annotation.getQualifiedName();
            if (name != null && name.endsWith("RequestParam")) {
                // 从注解中获取value属性或name属性
                PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
                for (PsiNameValuePair attribute : attributes) {
                    String attrName = attribute.getName();
                    // value和name属性都可以指定参数名
                    if ("value".equals(attrName) || "name".equals(attrName) || attrName == null) {
                        String literalValue = attribute.getLiteralValue();
                        if (literalValue != null && !literalValue.isEmpty()) {
                            return literalValue;
                        }
                        
                        // 处理可能的复杂表达式
                        PsiElement valueElement = attribute.getValue();
                        if (valueElement != null) {
                            String text = valueElement.getText();
                            if (text != null && text.startsWith("\"") && text.endsWith("\"") && text.length() >= 2) {
                                return text.substring(1, text.length() - 1);
                            }
                        }
                    }
                }
            }
        }
        return null;
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
     * 检查参数是否有@RequestParam注解
     */
    private static boolean hasRequestParamAnnotation(PsiParameter param) {
        PsiAnnotation[] annotations = param.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String name = annotation.getQualifiedName();
            if (name != null && name.endsWith("RequestParam")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从复杂对象提取参数
     */
    private static Map<String, String> extractObjectParameters(PsiType type) {
        Map<String, String> result = new HashMap<>();
        if (!(type instanceof PsiClassType)) {
            return result;
        }
        
        PsiClass psiClass = ((PsiClassType) type).resolve();
        if (psiClass == null) {
            return result;
        }
        
        // 获取所有字段并创建参数
        PsiField[] fields = psiClass.getAllFields();
        for (PsiField field : fields) {
            if (field.hasModifierProperty(PsiModifier.STATIC) || 
                field.hasModifierProperty(PsiModifier.TRANSIENT)) {
                continue;
            }
            
            String fieldName = field.getName();
            PsiType fieldType = field.getType();
            
            if (isPrimitiveOrString(fieldType)) {
                result.put(fieldName, getDefaultValueForType(fieldType));
            } else {
                // 如果是嵌套对象，递归处理
                Map<String, String> nestedParams = extractObjectParameters(fieldType);
                result.putAll(nestedParams);
            }
        }
        
        return result;
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
        
        // 如果有复杂对象参数但不是JSON，也使用POST方法
        boolean hasComplexObjectParam = parameters.containsKey("_hasComplexObjectParam");
        if (hasComplexObjectParam) {
            requestMethod = "POST";
        }
        parameters.remove("_hasComplexObjectParam"); // 移除标记
        
        ConfigSettings settings = ConfigSettings.getInstance(project);
        
        // 应用默认参数
        parameters = settings.applyDefaultParameters(parameters);
        
        Map<String, String> headers = settings.getHeaders();
        
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(requestMethod).append(" ");
        
        // 添加headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            curl.append("-H \"").append(entry.getKey()).append(": ").append(entry.getValue()).append("\" ");
        }
        
        // 构建URL，确保双引号位置正确
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
            curl.append("\"");
        } else if (parameters.containsKey("body")) {
            // 处理RequestBody
            curl.append("\" -H \"Content-Type: application/json\" ");
            curl.append("-d '").append(parameters.get("body")).append("'");
        } else if (!parameters.isEmpty()) {
            // 处理POST参数
            curl.append("\" ");
            if (!headers.containsKey("Content-Type")) {
                // 优先使用form-urlencoded格式，除非明确要求使用JSON
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
                    // 使用form-urlencoded格式，适用于拆解的复杂对象和普通参数
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
        
        // 如果有复杂对象参数但不是JSON，也使用POST方法
        boolean hasComplexObjectParam = parameters.containsKey("_hasComplexObjectParam");
        if (hasComplexObjectParam) {
            requestMethod = "POST";
        }
        parameters.remove("_hasComplexObjectParam"); // 移除标记
        
        ConfigSettings settings = ConfigSettings.getInstance(project);
        
        // 应用默认参数
        parameters = settings.applyDefaultParameters(parameters);
        
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
                // 处理常规参数或拆解的复杂对象参数，统一使用表单数据
                python.append("data = {\n");
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
                
                // 无论GET还是其他方法，都使用统一的参数传递方式
                if (requestMethod.equals("GET")) {
                    python.append("response = requests.get(url, params=data, headers=headers)\n");
                } else {
                    python.append("response = requests.").append(requestMethod.toLowerCase())
                          .append("(url, data=data, headers=headers)\n");
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

    /**
     * 复制URL路径
     * 只复制URL地址段，不包含host和参数
     *
     * @param project 当前项目
     * @param method 目标方法
     * @return URL路径
     */
    public static String copyUrlPath(Project project, PsiMethod method) {
        if (method == null) {
            return "";
        }
        
        // 获取请求路径
        String path = getRequestPath(method);
        
        // 确保路径不以斜杠开头，以适应拼接
        if (path.startsWith("/") && path.length() > 1) {
            path = path.substring(1);
        }
        
        return path;
    }
    
    /**
     * 复制完整URL
     * 复制包含协议、主机、路径的完整URL，如果是GET方法则带上参数
     *
     * @param project 当前项目
     * @param method 目标方法
     * @return 完整URL
     */
    public static String copyFullUrl(Project project, PsiMethod method) {
        if (method == null) {
            return "";
        }
        
        String requestMethod = getRequestMethod(method);
        String path = getRequestPath(method);
        Map<String, String> parameters = getParameters(method);
        
        // 移除内部标记
        parameters.remove("_hasJsonParam");
        parameters.remove("_hasComplexObjectParam");
        
        ConfigSettings settings = ConfigSettings.getInstance(project);
        
        // 应用默认参数
        parameters = settings.applyDefaultParameters(parameters);
        
        StringBuilder urlBuilder = new StringBuilder();
        // 添加协议、主机和上下文路径
        urlBuilder.append(settings.getFullUrlPrefix());
        
        // 添加API路径，确保路径之间只有一个斜杠
        if (settings.getContextPath().endsWith("/") && path.startsWith("/")) {
            urlBuilder.append(path.substring(1));
        } else if (!settings.getContextPath().endsWith("/") && !path.startsWith("/")) {
            urlBuilder.append("/").append(path);
        } else {
            urlBuilder.append(path);
        }
        
        // 如果是GET方法且有参数，则添加参数
        if (requestMethod.equals("GET") && !parameters.isEmpty() && !parameters.containsKey("body")) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }
        
        return urlBuilder.toString();
    }
    
    /**
     * 复制相对URL
     * 复制URL的地址段和参数，不包含主机
     *
     * @param project 当前项目
     * @param method 目标方法
     * @return 相对URL
     */
    public static String copyRelationUrl(Project project, PsiMethod method) {
        if (method == null) {
            return "";
        }
        
        String requestMethod = getRequestMethod(method);
        String path = getRequestPath(method);
        Map<String, String> parameters = getParameters(method);
        
        // 移除内部标记
        parameters.remove("_hasJsonParam");
        parameters.remove("_hasComplexObjectParam");
        
        ConfigSettings settings = ConfigSettings.getInstance(project);
        
        // 应用默认参数
        parameters = settings.applyDefaultParameters(parameters);
        
        StringBuilder urlBuilder = new StringBuilder();
        
        // 添加上下文路径
        urlBuilder.append(settings.getContextPath());
        
        // 添加API路径，确保路径之间只有一个斜杠
        if (settings.getContextPath().endsWith("/") && path.startsWith("/")) {
            urlBuilder.append(path.substring(1));
        } else if (!settings.getContextPath().endsWith("/") && !path.startsWith("/")) {
            urlBuilder.append("/").append(path);
        } else {
            urlBuilder.append(path);
        }
        
        // 添加参数（无论什么请求方法）
        if (!parameters.isEmpty() && !parameters.containsKey("body")) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }
        
        return urlBuilder.toString();
    }
}
