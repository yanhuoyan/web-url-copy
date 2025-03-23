package com.rhw.weburlcopy.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBPanel;
import com.rhw.weburlcopy.util.DisposerUtil;
import java.awt.BorderLayout;

/**
 * 兼容 IntelliJ IDEA 2024.3.5 版本的配置工具窗口面板
 * 
 * 此面板包装了原有的 ConfigToolWindowPanel，并确保所有资源正确释放
 * 主要解决 Disposer 机制导致的内存泄漏问题
 * 
 * @author renhao.wang
 * @since 2025-03-23
 */
public class CompatConfigToolWindowPanel extends JBPanel<CompatConfigToolWindowPanel> implements Disposable {
    
    private final ConfigToolWindowPanel delegatePanel;
    private final Project project;
    
    /**
     * 构造函数
     * 
     * @param project 当前项目
     */
    public CompatConfigToolWindowPanel(Project project) {
        this.project = project;
        this.delegatePanel = new ConfigToolWindowPanel(project);
        
        // 使用当前面板作为所有子组件的 parent disposable
        Disposer.register(project, this);
        
        // 代理到原始面板的布局和样式
        setLayout(new BorderLayout());
        add(delegatePanel, BorderLayout.CENTER);
        setPreferredSize(delegatePanel.getPreferredSize());
    }
    
    @Override
    public void dispose() {
        // 确保释放所有资源
        removeAll();
        if (delegatePanel instanceof Disposable) {
            DisposerUtil.dispose((Disposable) delegatePanel);
        }
    }
}