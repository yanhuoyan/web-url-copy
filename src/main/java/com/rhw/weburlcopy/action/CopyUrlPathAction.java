package com.rhw.weburlcopy.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.rhw.weburlcopy.util.RequestUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

/**
 * 复制URL地址段
 * 仅复制URL的路径部分，不包含host和参数
 * 
 * @author renhao.wang
 * @since 2023-03-22
 */
public class CopyUrlPathAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取当前选中的元素
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (element == null) {
            return;
        }

        // 生成URL路径
        StringBuilder urlPaths = new StringBuilder();

        if (element instanceof PsiMethod) {
            // 处理方法
            PsiMethod method = (PsiMethod) element;
            if (RequestUtil.isRequestMethod(method)) {
                String urlPath = RequestUtil.copyUrlPath(project, method);
                urlPaths.append(urlPath);
            }
        } else if (element instanceof PsiClass) {
            // 处理类
            PsiClass psiClass = (PsiClass) element;
            if (RequestUtil.isControllerClass(psiClass)) {
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod method : methods) {
                    if (RequestUtil.isRequestMethod(method)) {
                        String urlPath = RequestUtil.copyUrlPath(project, method);
                        urlPaths.append("# ").append(method.getName()).append("\n");
                        urlPaths.append(urlPath).append("\n\n");
                    }
                }
            }
        }

        // 复制到剪贴板
        if (urlPaths.length() > 0) {
            CopyPasteManager.getInstance().setContents(new StringSelection(urlPaths.toString().trim()));
        }
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