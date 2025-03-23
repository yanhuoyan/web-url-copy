package com.rhw.weburlcopy.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * 配置工具窗口工厂类
 */
public class ConfigToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 根据不同版本创建面板
        // 直接使用原始面板，因为已经修改了兼容性问题
        ConfigToolWindowPanel panel = new ConfigToolWindowPanel(project);
        Content content = ContentFactory.getInstance().createContent(panel, "设置", false);
        toolWindow.getContentManager().addContent(content);
    }
}



