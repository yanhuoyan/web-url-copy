package com.rhw.weburlcopy.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
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
 * 复制相对URL
 * 复制URL的地址段和参数，不包含主机
 * 
 * @author renhao.wang
 * @since 2023-03-22
 */
public class CopyRelationUrlAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

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

        // 生成相对URL
        StringBuilder relationUrls = new StringBuilder();

        if (element instanceof PsiMethod) {
            // 处理方法
            PsiMethod method = (PsiMethod) element;
            if (RequestUtil.isRequestMethod(method)) {
                String relationUrl = RequestUtil.copyRelationUrl(project, method);
                relationUrls.append(relationUrl);
            }
        } else if (element instanceof PsiClass) {
            // 处理类
            PsiClass psiClass = (PsiClass) element;
            if (RequestUtil.isControllerClass(psiClass)) {
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod method : methods) {
                    if (RequestUtil.isRequestMethod(method)) {
                        String relationUrl = RequestUtil.copyRelationUrl(project, method);
                        relationUrls.append("# ").append(method.getName()).append("\n");
                        relationUrls.append(relationUrl).append("\n\n");
                    }
                }
            }
        }

        // 复制到剪贴板
        if (relationUrls.length() > 0) {
            CopyPasteManager.getInstance().setContents(new StringSelection(relationUrls.toString().trim()));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 子菜单始终可见和可用，不再依赖PSI元素判断
        e.getPresentation().setEnabled(true);
        e.getPresentation().setVisible(true);
        
        // 确保模板状态也是可用的
        getTemplatePresentation().setEnabled(true);
        getTemplatePresentation().setVisible(true);
    }
} 