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

/**
 * 复制Python Request代码动作
 */
public class CopyPythonRequestAction extends AnAction {

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

        // 生成Python请求代码
        StringBuilder pythonCode = new StringBuilder();

        if (element instanceof PsiMethod) {
            // 处理方法
            PsiMethod method = (PsiMethod) element;
            if (RequestUtil.isRequestMethod(method)) {
                String python = RequestUtil.generatePythonRequest(project, method);
                pythonCode.append(python);
            }
        } else if (element instanceof PsiClass) {
            // 处理类
            PsiClass psiClass = (PsiClass) element;
            if (RequestUtil.isControllerClass(psiClass)) {
                // 导入模块部分只需要一次
                pythonCode.append("import requests\n\n");
                
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod method : methods) {
                    if (RequestUtil.isRequestMethod(method)) {
                        // 方法名作为注释
                        pythonCode.append("# ").append(method.getName()).append("\n");
                        
                        // 函数定义
                        pythonCode.append("def ").append(method.getName()).append("():\n");
                        
                        // 获取生成的Python代码，但跳过import部分
                        String pythonMethod = RequestUtil.generatePythonRequest(project, method);
                        String[] lines = pythonMethod.split("\n");
                        boolean skipImport = true;
                        
                        for (String line : lines) {
                            if (skipImport && line.trim().startsWith("import")) {
                                continue;
                            }
                            skipImport = false;
                            // 添加缩进
                            pythonCode.append("    ").append(line).append("\n");
                        }
                        
                        pythonCode.append("\n\n");
                    }
                }
                
                // 添加主函数调用
                pythonCode.append("if __name__ == '__main__':\n");
                for (PsiMethod method : methods) {
                    if (RequestUtil.isRequestMethod(method)) {
                        pythonCode.append("    # ").append(method.getName()).append("()\n");
                    }
                }
            }
        }

        // 复制到剪贴板
        if (pythonCode.length() > 0) {
            CopyPasteManager.getInstance().setContents(new StringSelection(pythonCode.toString().trim()));
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
