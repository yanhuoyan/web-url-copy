package com.rhw.weburlcopy.window;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.rhw.weburlcopy.model.ConfigSettings;
import com.rhw.weburlcopy.model.UrlConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Window;
import java.awt.Frame;
import java.awt.Dialog;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置工具窗口面板
 * 
 * @author renhao.wang
 * @since 2023-03-22
 */
public class ConfigToolWindowPanel extends JBPanel<ConfigToolWindowPanel> {
    private final Project project;
    
    // URL配置列表
    private DefaultTableModel configsTableModel;
    private JBTable configsTable;
    private JBTextField configNameField;
    private JBTextField hostField;
    private JBTextField contextPathField;
    private JComboBox<String> protocolComboBox;
    
    // 请求头表格
    private DefaultTableModel headersTableModel;
    private JTable headersTable;
    
    // 默认参数表格
    private DefaultTableModel paramsTableModel;
    private JTable paramsTable;

    /**
     * 构造函数
     * 
     * @param project 当前项目
     */
    public ConfigToolWindowPanel(Project project) {
        this.project = project;
        initPanel();
    }

    /**
     * 初始化面板组件
     */
    private void initPanel() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(15));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // 创建标题
        JLabel titleLabel = new JLabel("请求配置", AllIcons.General.Settings, SwingConstants.LEFT);
        titleLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // URL配置部分
        JPanel urlConfigPanel = createUrlConfigPanel();
        
        // 创建选项卡面板
        JBTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        
        // 请求头选项卡
        JPanel headersPanel = createHeadersPanel();
        tabbedPane.addTab("请求头", AllIcons.General.Settings, headersPanel, "配置HTTP请求头");
        
        // 默认参数选项卡
        JPanel paramsPanel = createDefaultParamsPanel();
        tabbedPane.addTab("默认参数", AllIcons.Nodes.Parameter, paramsPanel, "配置请求参数的默认值");
        
        // 布局组件
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
                .addComponent(titleLabel)
                .addComponentFillVertically(new JPanel(), 5)
                .addComponent(urlConfigPanel)
                .addComponentFillVertically(new JPanel(), 10)
                .addComponentFillVertically(tabbedPane, 0);
                
        mainPanel.add(formBuilder.getPanel(), BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        
        // 加载配置
        loadSettings();
    }
    
    /**
     * 创建URL配置面板
     * 
     * @return URL配置面板
     */
    private JPanel createUrlConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.getBoundsColor(), 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // 添加标题
        JLabel panelTitle = new JLabel("环境配置", AllIcons.Nodes.ConfigFolder, SwingConstants.LEFT);
        panelTitle.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, 14f));
        panelTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
        
        // 单面板布局，只显示环境列表
        JPanel envListPanel = new JPanel(new BorderLayout());
        envListPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        
        // 创建表格模型 - 第一列是选择框，后面是环境信息
        String[] columnNames = {"选择", "环境名称", "主机地址", "协议", "上下文路径"};
        configsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 只有选择列可编辑
            }
        };
        
        // 创建表格
        configsTable = new JBTable(configsTableModel);
        configsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        configsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        configsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        configsTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        configsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        configsTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        configsTable.setRowHeight(32);
        configsTable.setShowGrid(false);
        configsTable.setIntercellSpacing(new Dimension(0, 0));
        configsTable.getTableHeader().setReorderingAllowed(false);
        configsTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtil.getBoundsColor()),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                label.setBackground(UIUtil.getPanelBackground());
                return label;
            }
        });
        configsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 监听复选框变化
        configsTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 0) { // 选择列
                int row = e.getFirstRow();
                if (row >= 0) {
                    Boolean selected = (Boolean) configsTableModel.getValueAt(row, 0);
                    if (selected) {
                        // 取消其他行的选中状态
                        for (int i = 0; i < configsTableModel.getRowCount(); i++) {
                            if (i != row) {
                                configsTableModel.setValueAt(false, i, 0);
                            }
                        }
                        
                        // 设置活动配置
                        ConfigSettings settings = ConfigSettings.getInstance(project);
                        List<UrlConfig> configs = settings.getUrlConfigs();
                        if (row < configs.size()) {
                            UrlConfig config = configs.get(row);
                            settings.setActiveConfigId(config.getId());
                            updateUrlPreview();
                        }
                    } else {
                        // 确保至少有一个选中项
                        boolean hasSelection = false;
                        for (int i = 0; i < configsTableModel.getRowCount(); i++) {
                            if ((Boolean) configsTableModel.getValueAt(i, 0)) {
                                hasSelection = true;
                                break;
                            }
                        }
                        
                        if (!hasSelection && configsTableModel.getRowCount() > 0) {
                            configsTableModel.setValueAt(true, row, 0);
                        }
                    }
                }
            }
        });
        
        // 添加双击编辑事件
        configsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = configsTable.getSelectedRow();
                    if (row >= 0) {
                        ConfigSettings settings = ConfigSettings.getInstance(project);
                        List<UrlConfig> configs = settings.getUrlConfigs();
                        if (row < configs.size()) {
                            showConfigDialog(configs.get(row), false);
                        }
                    }
                }
            }
        });
        
        // 滚动面板
        JBScrollPane scrollPane = new JBScrollPane(configsTable);
        envListPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("添加环境", AllIcons.General.Add);
        addButton.addActionListener(e -> addNewConfig());
        
        JButton editButton = new JButton("修改环境", AllIcons.Actions.Edit);
        editButton.addActionListener(e -> {
            int selectedRow = configsTable.getSelectedRow();
            if (selectedRow >= 0) {
                ConfigSettings settings = ConfigSettings.getInstance(project);
                List<UrlConfig> configs = settings.getUrlConfigs();
                if (selectedRow < configs.size()) {
                    showConfigDialog(configs.get(selectedRow), false);
                }
            } else {
                Messages.showInfoMessage(project, "请先选择要修改的环境", "提示");
            }
        });
        
        JButton removeButton = new JButton("删除环境", AllIcons.General.Remove);
        removeButton.addActionListener(e -> deleteSelectedConfig());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        
        envListPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // URL预览面板
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        JPanel previewContent = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel previewLabel = new JLabel("URL预览:");
        JLabel urlPreviewLabel = new JLabel("http://localhost");
        urlPreviewLabel.setForeground(UIUtil.getContextHelpForeground());
        
        previewContent.add(previewLabel);
        previewContent.add(urlPreviewLabel);
        
        previewPanel.add(previewContent, BorderLayout.NORTH);
        
        // 添加到主面板
        panel.add(envListPanel, BorderLayout.CENTER);
        panel.add(previewPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 显示环境配置对话框
     * 
     * @param config 要编辑的配置，如果为新建则传入null
     * @param isNew 是否为新建配置
     */
    private void showConfigDialog(UrlConfig config, boolean isNew) {
        // 创建对话框
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "环境配置", true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, "环境配置", true);
        } else {
            dialog = new JDialog(new JFrame(), "环境配置", true);
        }
        
        dialog.setSize(550, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        GridBagConstraints c = new GridBagConstraints();
        
        // 添加标题
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.anchor = GridBagConstraints.WEST;
        c.insets = JBUI.insets(0, 0, 15, 0);
        JLabel titleLabel = new JLabel(isNew ? "添加新环境" : "修改环境", isNew ? AllIcons.General.Add : AllIcons.Actions.Edit, SwingConstants.LEFT);
        titleLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, 16f));
        formPanel.add(titleLabel, c);
        
        // 配置名称
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.insets = JBUI.insets(5, 0, 5, 10);
        JBLabel configNameLabel = new JBLabel("环境名称:");
        configNameLabel.setFont(configNameLabel.getFont().deriveFont(Font.BOLD));
        formPanel.add(configNameLabel, c);
        
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = JBUI.insets(5, 0, 5, 0);
        JBTextField configNameField = new JBTextField();
        configNameField.setToolTipText("输入环境的描述性名称");
        formPanel.add(configNameField, c);
        
        // 协议选择框
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.insets = JBUI.insets(5, 0, 5, 10);
        c.anchor = GridBagConstraints.WEST;
        JBLabel protocolLabel = new JBLabel("协议:");
        protocolLabel.setFont(protocolLabel.getFont().deriveFont(Font.BOLD));
        formPanel.add(protocolLabel, c);
        
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.3;
        c.insets = JBUI.insets(5, 0, 5, 0);
        JComboBox<String> protocolComboBox = new JComboBox<>(new String[]{"http", "https"});
        protocolComboBox.setToolTipText("选择HTTP或HTTPS协议");
        formPanel.add(protocolComboBox, c);
        
        // 主机地址标签
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = JBUI.insets(5, 0, 5, 10);
        JBLabel hostLabel = new JBLabel("主机地址:");
        hostLabel.setFont(hostLabel.getFont().deriveFont(Font.BOLD));
        hostLabel.setToolTipText("例如: api.example.com");
        formPanel.add(hostLabel, c);
        
        // 主机地址输入框
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = JBUI.insets(5, 0, 5, 0);
        JBTextField hostField = new JBTextField();
        hostField.setToolTipText("请输入主机地址，例如: api.example.com（无需协议前缀）");
        formPanel.add(hostField, c);
        
        // 上下文路径标签
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = JBUI.insets(5, 0, 5, 10);
        JBLabel contextLabel = new JBLabel("上下文路径:");
        contextLabel.setFont(contextLabel.getFont().deriveFont(Font.BOLD));
        contextLabel.setToolTipText("例如: /api/v1");
        formPanel.add(contextLabel, c);
        
        // 上下文路径输入框
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = JBUI.insets(5, 0, 5, 0);
        JBTextField contextPathField = new JBTextField();
        contextPathField.setToolTipText("请输入API的上下文路径，例如: /api/v1");
        formPanel.add(contextPathField, c);
        
        // URL预览区域
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 4;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = JBUI.insets(15, 0, 5, 0);
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, UIUtil.getBoundsColor()),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        previewPanel.setBackground(UIUtil.getPanelBackground());
        
        JPanel previewLabelPanel = new JPanel(new BorderLayout());
        previewLabelPanel.setOpaque(false);
        
        JLabel previewTitle = new JLabel("URL预览", AllIcons.General.Web, SwingConstants.LEFT);
        previewTitle.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, 12f));
        
        JLabel urlPreviewLabel = new JLabel("http://localhost");
        urlPreviewLabel.setForeground(UIUtil.getContextHelpForeground());
        urlPreviewLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
        urlPreviewLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        previewLabelPanel.add(previewTitle, BorderLayout.NORTH);
        previewLabelPanel.add(urlPreviewLabel, BorderLayout.CENTER);
        
        previewPanel.add(previewLabelPanel, BorderLayout.CENTER);
        formPanel.add(previewPanel, c);
        
        // 初始化表单数据
        if (config != null) {
            configNameField.setText(config.getName());
            hostField.setText(config.getHost());
            contextPathField.setText(config.getContextPath());
            protocolComboBox.setSelectedItem(config.getProtocol());
            updateUrlPreviewLabel(urlPreviewLabel, protocolComboBox.getSelectedItem().toString(), 
                                 hostField.getText(), contextPathField.getText());
        }
        
        // 添加事件监听，实时更新URL预览
        DocumentListener docListener = new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
            
            private void updatePreview() {
                updateUrlPreviewLabel(urlPreviewLabel, protocolComboBox.getSelectedItem().toString(), 
                                     hostField.getText(), contextPathField.getText());
            }
        };
        
        hostField.getDocument().addDocumentListener(docListener);
        contextPathField.getDocument().addDocumentListener(docListener);
        protocolComboBox.addActionListener(e -> {
            updateUrlPreviewLabel(urlPreviewLabel, protocolComboBox.getSelectedItem().toString(), 
                                 hostField.getText(), contextPathField.getText());
        });
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = new JButton("保存", AllIcons.Actions.Commit);
        saveButton.addActionListener(e -> {
            // 保存配置
            String name = configNameField.getText().trim();
            if (name.isEmpty()) {
                Messages.showErrorDialog(dialog, "环境名称不能为空", "错误");
                return;
            }
            
            String host = hostField.getText().trim();
            if (host.isEmpty()) {
                Messages.showErrorDialog(dialog, "主机地址不能为空", "错误");
                return;
            }
            
            String protocol = protocolComboBox.getSelectedItem().toString();
            String contextPath = contextPathField.getText();
            
            if (isNew) {
                // 创建新配置
                UrlConfig newConfig = new UrlConfig(name, host, contextPath, protocol);
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.addUrlConfig(newConfig);
                
                // 更新UI并选中新创建的配置
                refreshEnvironmentTable();
                int rowIndex = findConfigRowIndex(newConfig.getId());
                if (rowIndex >= 0) {
                    configsTable.setRowSelectionInterval(rowIndex, rowIndex);
                    configsTableModel.setValueAt(true, rowIndex, 0);
                }
            } else {
                // 更新现有配置
                config.setName(name);
                config.setHost(host);
                config.setContextPath(contextPath);
                config.setProtocol(protocol);
                
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.updateUrlConfig(config);
                
                // 更新UI
                refreshEnvironmentTable();
                int rowIndex = findConfigRowIndex(config.getId());
                if (rowIndex >= 0) {
                    configsTable.setRowSelectionInterval(rowIndex, rowIndex);
                }
            }
            
            // 更新URL预览
            updateUrlPreview();
            
            // 关闭对话框
            dialog.dispose();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // 添加到对话框
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // 显示对话框
        dialog.setVisible(true);
    }
    
    /**
     * 更新URL预览标签
     */
    private void updateUrlPreviewLabel(JLabel previewLabel, String protocol, String host, String contextPath) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://");
        sb.append(host);
        
        if (contextPath != null && !contextPath.isEmpty()) {
            if (!contextPath.startsWith("/")) {
                sb.append("/");
            }
            sb.append(contextPath);
            if (contextPath.endsWith("/")) {
                sb.setLength(sb.length() - 1);
            }
        }
        
        previewLabel.setText(sb.toString());
    }
    
    /**
     * 创建请求头面板
     * 
     * @return 请求头面板
     */
    private JPanel createHeadersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 添加标题
        JLabel panelTitle = new JLabel("HTTP请求头配置", AllIcons.General.Settings, SwingConstants.LEFT);
        panelTitle.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, 14f));
        panelTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // 创建表格模型
        String[] columnNames = {"Header", "Value"};
        headersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        
        // 创建表格
        headersTable = new JBTable(headersTableModel);
        headersTable.setRowHeight(30);
        headersTable.setShowGrid(false);
        headersTable.setIntercellSpacing(new Dimension(0, 0));
        headersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        headersTable.getTableHeader().setReorderingAllowed(false);
        headersTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtil.getBoundsColor()),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                label.setBackground(UIUtil.getPanelBackground());
                return label;
            }
        });
        
        JBScrollPane scrollPane = new JBScrollPane(headersTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.getBoundsColor(), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        
        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton addHeaderButton = new JButton("添加", AllIcons.General.Add);
        addHeaderButton.setFocusPainted(false);
        addHeaderButton.addActionListener(e -> addHeader());
        
        JButton removeHeaderButton = new JButton("删除", AllIcons.General.Remove);
        removeHeaderButton.setFocusPainted(false);
        removeHeaderButton.addActionListener(e -> removeHeader());
        
        JButton saveHeadersButton = new JButton("保存", AllIcons.Actions.Commit);
        saveHeadersButton.setFocusPainted(false);
        saveHeadersButton.addActionListener(e -> saveHeaders());
        
        buttonPanel.add(addHeaderButton);
        buttonPanel.add(removeHeaderButton);
        buttonPanel.add(saveHeadersButton);
        
        // 组合布局
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panelTitle, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建默认参数面板
     * 
     * @return 默认参数面板
     */
    private JPanel createDefaultParamsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 添加标题
        JLabel panelTitle = new JLabel("默认参数配置", AllIcons.Nodes.Parameter, SwingConstants.LEFT);
        panelTitle.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, 14f));
        panelTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // 创建表格模型
        String[] columnNames = {"参数名", "默认值"};
        paramsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        
        // 创建表格
        paramsTable = new JBTable(paramsTableModel);
        paramsTable.setRowHeight(30);
        paramsTable.setShowGrid(false);
        paramsTable.setIntercellSpacing(new Dimension(0, 0));
        paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paramsTable.getTableHeader().setReorderingAllowed(false);
        paramsTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtil.getBoundsColor()),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                label.setBackground(UIUtil.getPanelBackground());
                return label;
            }
        });
        
        JBScrollPane scrollPane = new JBScrollPane(paramsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.getBoundsColor(), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        
        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton addParamButton = new JButton("添加", AllIcons.General.Add);
        addParamButton.setFocusPainted(false);
        addParamButton.addActionListener(e -> addDefaultParam());
        
        JButton removeParamButton = new JButton("删除", AllIcons.General.Remove);
        removeParamButton.setFocusPainted(false);
        removeParamButton.addActionListener(e -> removeDefaultParam());
        
        JButton saveParamsButton = new JButton("保存", AllIcons.Actions.Commit);
        saveParamsButton.setFocusPainted(false);
        saveParamsButton.addActionListener(e -> saveDefaultParams());
        
        buttonPanel.add(addParamButton);
        buttonPanel.add(removeParamButton);
        buttonPanel.add(saveParamsButton);
        
        // 组合布局
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panelTitle, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 添加新的URL配置
     */
    private void addNewConfig() {
        // 显示新建环境对话框
        showConfigDialog(null, true);
    }
    
    /**
     * 删除选中的URL配置
     */
    private void deleteSelectedConfig() {
        int selectedRow = configsTable.getSelectedRow();
        if (selectedRow < 0) {
            Messages.showInfoMessage(project, "请先选择要删除的环境", "提示");
            return;
        }
        
        ConfigSettings settings = ConfigSettings.getInstance(project);
        List<UrlConfig> configs = settings.getUrlConfigs();
        if (selectedRow >= configs.size()) {
            return;
        }
        
        UrlConfig selectedConfig = configs.get(selectedRow);
        
        // 确认对话框
        int result = Messages.showYesNoDialog(
                project,
                "确定要删除环境 \"" + selectedConfig.getDisplayName() + "\" 吗?",
                "删除环境",
                Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            boolean deleted = settings.removeUrlConfig(selectedConfig.getId());
            
            if (!deleted) {
                Messages.showMessageDialog(
                        project,
                        "至少需要保留一个环境配置，无法删除。",
                        "提示",
                        Messages.getInformationIcon()
                );
                return;
            }
            
            // 更新UI
            refreshEnvironmentTable();
            
            // 选中第一个配置
            if (configsTableModel.getRowCount() > 0) {
                configsTable.setRowSelectionInterval(0, 0);
                configsTableModel.setValueAt(true, 0, 0);
            }
        }
    }
    
    /**
     * 刷新环境表格
     */
    private void refreshEnvironmentTable() {
        // 清空表格
        while (configsTableModel.getRowCount() > 0) {
            configsTableModel.removeRow(0);
        }
        
        // 获取配置
        ConfigSettings settings = ConfigSettings.getInstance(project);
        String activeConfigId = settings.getActiveConfigId();
        
        // 添加配置到表格
        for (UrlConfig config : settings.getUrlConfigs()) {
            boolean isActive = config.getId().equals(activeConfigId);
            configsTableModel.addRow(new Object[]{
                isActive,
                config.getName(),
                config.getHost(),
                config.getProtocol(),
                config.getContextPath()
            });
        }
    }
    
    /**
     * 查找配置在列表中的索引
     * 
     * @param configId 配置ID
     * @return 配置的索引，如果没找到返回-1
     */
    private int findConfigRowIndex(String configId) {
        ConfigSettings settings = ConfigSettings.getInstance(project);
        List<UrlConfig> configs = settings.getUrlConfigs();
        
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).getId().equals(configId)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 添加请求头
     */
    private void addHeader() {
        headersTableModel.addRow(new Object[]{"", ""});
    }
    
    /**
     * 删除请求头
     */
    private void removeHeader() {
        int selectedRow = headersTable.getSelectedRow();
        if (selectedRow != -1) {
            headersTableModel.removeRow(selectedRow);
        }
    }
    
    /**
     * 保存请求头
     */
    private void saveHeaders() {
        ConfigSettings settings = ConfigSettings.getInstance(project);
        Map<String, String> headers = settings.getHeaders();
        headers.clear();
        
        for (int i = 0; i < headersTableModel.getRowCount(); i++) {
            String key = (String) headersTableModel.getValueAt(i, 0);
            String value = (String) headersTableModel.getValueAt(i, 1);
            if (key != null && !key.trim().isEmpty()) {
                headers.put(key, value);
            }
        }
        
        Messages.showInfoMessage(project, "请求头保存成功", "保存成功");
    }
    
    /**
     * 添加默认参数
     */
    private void addDefaultParam() {
        paramsTableModel.addRow(new Object[]{"", ""});
    }
    
    /**
     * 删除默认参数
     */
    private void removeDefaultParam() {
        int selectedRow = paramsTable.getSelectedRow();
        if (selectedRow != -1) {
            paramsTableModel.removeRow(selectedRow);
        }
    }
    
    /**
     * 保存默认参数
     */
    private void saveDefaultParams() {
        ConfigSettings settings = ConfigSettings.getInstance(project);
        Map<String, String> params = settings.getDefaultParameters();
        params.clear();
        
        for (int i = 0; i < paramsTableModel.getRowCount(); i++) {
            String key = (String) paramsTableModel.getValueAt(i, 0);
            String value = (String) paramsTableModel.getValueAt(i, 1);
            if (key != null && !key.trim().isEmpty()) {
                params.put(key, value);
            }
        }
        
        Messages.showInfoMessage(project, "默认参数保存成功", "保存成功");
    }
    
    /**
     * 加载配置设置
     */
    private void loadSettings() {
        ConfigSettings settings = ConfigSettings.getInstance(project);
        
        // 刷新环境表格
        refreshEnvironmentTable();
        
        // 加载当前选中的配置到表单
        UrlConfig activeConfig = settings.getActiveConfig();
        if (activeConfig != null) {
            // 选中活动配置行
            int rowIndex = findConfigRowIndex(activeConfig.getId());
            if (rowIndex >= 0) {
                configsTable.setRowSelectionInterval(rowIndex, rowIndex);
            }
        }
        
        // 更新URL预览
        updateUrlPreview();
        
        // 加载请求头
        loadHeaders(settings);
        
        // 加载默认参数
        loadDefaultParams(settings);
    }
    
    /**
     * 加载请求头
     * 
     * @param settings 配置设置
     */
    private void loadHeaders(ConfigSettings settings) {
        // 清空表格
        while (headersTableModel.getRowCount() > 0) {
            headersTableModel.removeRow(0);
        }
        
        // 添加请求头
        for (Map.Entry<String, String> entry : settings.getHeaders().entrySet()) {
            headersTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
    
    /**
     * 加载默认参数
     * 
     * @param settings 配置设置
     */
    private void loadDefaultParams(ConfigSettings settings) {
        // 清空表格
        while (paramsTableModel.getRowCount() > 0) {
            paramsTableModel.removeRow(0);
        }
        
        // 添加默认参数
        for (Map.Entry<String, String> entry : settings.getDefaultParameters().entrySet()) {
            paramsTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
    
    /**
     * 更新URL预览
     */
    private void updateUrlPreview() {
        JLabel urlPreviewLabel = findUrlPreviewLabel();
        if (urlPreviewLabel != null) {
            ConfigSettings settings = ConfigSettings.getInstance(project);
            UrlConfig config = settings.getActiveConfig();
            if (config != null) {
                urlPreviewLabel.setText(config.getFullUrlPrefix());
            }
        }
    }
    
    /**
     * 查找URL预览标签组件
     * 
     * @return URL预览标签组件
     */
    private JLabel findUrlPreviewLabel() {
        // 查找面板中的URL预览标签
        Component[] components = ((JPanel) getComponent(0)).getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                Component[] subComponents = ((JPanel) component).getComponents();
                for (Component subComponent : subComponents) {
                    if (subComponent instanceof JPanel && ((JPanel) subComponent).getBorder() instanceof TitledBorder) {
                        Component[] panelComponents = ((JPanel) subComponent).getComponents();
                        for (Component panelComponent : panelComponents) {
                            if (panelComponent instanceof JPanel && 
                                ((JPanel) panelComponent).getLayout() instanceof BorderLayout) {
                                Component southComponent = ((BorderLayout)((JPanel) panelComponent).getLayout())
                                        .getLayoutComponent(BorderLayout.SOUTH);
                                if (southComponent instanceof JPanel) {
                                    Component[] previewComponents = ((JPanel) southComponent).getComponents();
                                    for (Component previewComponent : previewComponents) {
                                        if (previewComponent instanceof JPanel) {
                                            Component[] labels = ((JPanel) previewComponent).getComponents();
                                            for (int i = 0; i < labels.length; i++) {
                                                if (labels[i] instanceof JLabel && 
                                                    ((JLabel) labels[i]).getText().equals("URL预览:") &&
                                                    i + 1 < labels.length &&
                                                    labels[i + 1] instanceof JLabel) {
                                                    return (JLabel) labels[i + 1];
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
