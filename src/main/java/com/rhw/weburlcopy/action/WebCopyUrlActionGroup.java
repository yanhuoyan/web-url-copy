package com.rhw.weburlcopy.action;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
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
        // 使用默认构造函数
        super();
        // 设置组ID和显示文本
        getTemplatePresentation().setText("Web Copy URL");
        // 确保这是一个弹出菜单组
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
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 菜单组始终可见和可用，不再依赖PSI元素判断
        e.getPresentation().setEnabled(true);
        e.getPresentation().setVisible(true);
        
        // 确保菜单显示
        getTemplatePresentation().setEnabled(true);
        getTemplatePresentation().setVisible(true);
    }
} 