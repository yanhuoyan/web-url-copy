package com.rhw.weburlcopy.action;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.rhw.weburlcopy.util.RequestUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Web URL Copy菜单组
 * 提供复制URL的子选项
 */
public class WebCopyUrlActionGroup extends DefaultActionGroup {
    
    public WebCopyUrlActionGroup() {
        super("Web Copy URL", true); // 第二个参数设置popup为true，表示这是一个弹出菜单组
        setPopup(true);
        
        // 添加子操作到菜单组
        // 1. 复制URL路径（只有路径部分）
        add(new CopyUrlPathAction());
        // 2. 复制完整URL（带域名和协议）
        add(new CopyFullUrlAction());
        // 3. 复制相对URL（路径+参数）
        add(new CopyRelationUrlAction());
        // 4. 复制Bash Curl命令（原有功能）
        add(new CopyBashCurlAction());
        // 5. 复制Python请求代码（原有功能）
        add(new CopyPythonRequestAction());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        
        // 只有在选中了Java方法或类且是控制器或请求方法时才启用
        boolean enabled = false;
        if (project != null && element != null) {
            if (element instanceof PsiMethod) {
                enabled = RequestUtil.isRequestMethod((PsiMethod) element);
            } else if (element instanceof PsiClass) {
                enabled = RequestUtil.isControllerClass((PsiClass) element);
            }
        }
        
        e.getPresentation().setEnabledAndVisible(enabled);
    }
} 