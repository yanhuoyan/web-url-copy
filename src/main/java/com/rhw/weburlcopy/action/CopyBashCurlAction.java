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
 * 复制Bash Curl命令动作
 */
public class CopyBashCurlAction extends AnAction {

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

        // 生成curl命令
        StringBuilder curlCommands = new StringBuilder();

        if (element instanceof PsiMethod) {
            // 处理方法
            PsiMethod method = (PsiMethod) element;
            if (RequestUtil.isRequestMethod(method)) {
                String curl = RequestUtil.generateCurlCommand(project, method);
                curlCommands.append(curl).append("\n\n");
            }
        } else if (element instanceof PsiClass) {
            // 处理类
            PsiClass psiClass = (PsiClass) element;
            if (RequestUtil.isControllerClass(psiClass)) {
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod method : methods) {
                    if (RequestUtil.isRequestMethod(method)) {
                        String curl = RequestUtil.generateCurlCommand(project, method);
                        curlCommands.append("# ").append(method.getName()).append("\n");
                        curlCommands.append(curl).append("\n\n");
                    }
                }
            }
        }

        // 复制到剪贴板
        if (curlCommands.length() > 0) {
            CopyPasteManager.getInstance().setContents(new StringSelection(curlCommands.toString().trim()));
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
