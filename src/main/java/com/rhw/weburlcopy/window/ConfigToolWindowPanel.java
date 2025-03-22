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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private DefaultComboBoxModel<UrlConfig> configsComboBoxModel;
    private JComboBox<UrlConfig> configsComboBox;
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
        setBorder(JBUI.Borders.empty(10));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // 创建标题
        JLabel titleLabel = new JLabel("请求配置", AllIcons.General.Settings, SwingConstants.LEFT);
        titleLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // URL配置部分
        JPanel urlConfigPanel = createUrlConfigPanel();
        
        // 创建选项卡面板
        JBTabbedPane tabbedPane = new JBTabbedPane();
        
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
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtil.getBoundsColor(), 1, true),
                "URL 配置",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtil.getLabelFont().deriveFont(Font.BOLD),
                UIUtil.getLabelForeground()));
        
        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        
        // 配置选择下拉框
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = JBUI.insets(5, 0, 5, 10);
        JBLabel configsLabel = new JBLabel("配置选择:");
        innerPanel.add(configsLabel, c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        configsComboBoxModel = new DefaultComboBoxModel<>();
        configsComboBox = new JComboBox<>(configsComboBoxModel);
        configsComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UrlConfig) {
                    setText(((UrlConfig) value).getDisplayName());
                }
                return this;
            }
        });
        configsComboBox.setToolTipText("选择要使用的URL配置");
        configsComboBox.addActionListener(e -> {
            UrlConfig selectedConfig = (UrlConfig) configsComboBox.getSelectedItem();
            if (selectedConfig != null) {
                // 保存当前选中配置
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.setActiveConfigId(selectedConfig.getId());
                
                // 更新表单显示
                loadConfigToForm(selectedConfig);
                
                // 更新URL预览
                updateUrlPreview();
            }
        });
        innerPanel.add(configsComboBox, c);
        
        // 配置操作按钮
        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        JPanel configButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        // 添加配置按钮
        JButton addConfigButton = new JButton("", AllIcons.General.Add);
        addConfigButton.setToolTipText("添加新配置");
        addConfigButton.addActionListener(e -> addNewConfig());
        configButtonsPanel.add(addConfigButton);
        
        // 删除配置按钮
        JButton deleteConfigButton = new JButton("", AllIcons.General.Remove);
        deleteConfigButton.setToolTipText("删除当前配置");
        deleteConfigButton.addActionListener(e -> deleteSelectedConfig());
        configButtonsPanel.add(deleteConfigButton);
        
        innerPanel.add(configButtonsPanel, c);
        
        // 配置名称
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        JBLabel configNameLabel = new JBLabel("配置名称:");
        innerPanel.add(configNameLabel, c);
        
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        configNameField = new JBTextField();
        configNameField.setToolTipText("输入配置的描述性名称");
        configNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
            }
        });
        innerPanel.add(configNameField, c);
        
        // 协议选择框
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        JBLabel protocolLabel = new JBLabel("协议:");
        innerPanel.add(protocolLabel, c);
        
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.3;
        protocolComboBox = new JComboBox<>(new String[]{"http", "https"});
        protocolComboBox.setToolTipText("选择HTTP或HTTPS协议");
        protocolComboBox.addActionListener(e -> {
            updateCurrentConfig();
            updateUrlPreview();
        });
        innerPanel.add(protocolComboBox, c);
        
        // 主机地址标签
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        JBLabel hostLabel = new JBLabel("主机地址:");
        hostLabel.setToolTipText("例如: api.example.com");
        innerPanel.add(hostLabel, c);
        
        // 主机地址输入框
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        hostField = new JBTextField();
        hostField.setToolTipText("请输入主机地址，例如: api.example.com（无需协议前缀）");
        hostField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
                updateUrlPreview();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
                updateUrlPreview();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
                updateUrlPreview();
            }
        });
        innerPanel.add(hostField, c);
        
        // 上下文路径标签
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        JBLabel contextLabel = new JBLabel("上下文路径:");
        contextLabel.setToolTipText("例如: /api/v1");
        innerPanel.add(contextLabel, c);
        
        // 上下文路径输入框
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        contextPathField = new JBTextField();
        contextPathField.setToolTipText("请输入API的上下文路径，例如: /api/v1");
        contextPathField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
                updateUrlPreview();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
                updateUrlPreview();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCurrentConfig();
                updateUrlPreview();
            }
        });
        innerPanel.add(contextPathField, c);
        
        // URL预览标签
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        JBLabel previewLabel = new JBLabel("URL预览:");
        innerPanel.add(previewLabel, c);
        
        // URL预览内容
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        JLabel urlPreviewLabel = new JLabel("http://localhost");
        urlPreviewLabel.setForeground(UIUtil.getContextHelpForeground());
        innerPanel.add(urlPreviewLabel, c);
        
        panel.add(innerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建请求头面板
     * 
     * @return 请求头面板
     */
    private JPanel createHeadersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
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
        headersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        headersTable.getTableHeader().setReorderingAllowed(false);
        
        JBScrollPane scrollPane = new JBScrollPane(headersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addHeaderButton = new JButton("添加", AllIcons.General.Add);
        addHeaderButton.addActionListener(e -> addHeader());
        
        JButton removeHeaderButton = new JButton("删除", AllIcons.General.Remove);
        removeHeaderButton.addActionListener(e -> removeHeader());
        
        JButton saveHeadersButton = new JButton("保存", AllIcons.Actions.Commit);
        saveHeadersButton.addActionListener(e -> saveHeaders());
        
        buttonPanel.add(addHeaderButton);
        buttonPanel.add(removeHeaderButton);
        buttonPanel.add(saveHeadersButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建默认参数面板
     * 
     * @return 默认参数面板
     */
    private JPanel createDefaultParamsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
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
        paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paramsTable.getTableHeader().setReorderingAllowed(false);
        
        JBScrollPane scrollPane = new JBScrollPane(paramsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addParamButton = new JButton("添加", AllIcons.General.Add);
        addParamButton.addActionListener(e -> addDefaultParam());
        
        JButton removeParamButton = new JButton("删除", AllIcons.General.Remove);
        removeParamButton.addActionListener(e -> removeDefaultParam());
        
        JButton saveParamsButton = new JButton("保存", AllIcons.Actions.Commit);
        saveParamsButton.addActionListener(e -> saveDefaultParams());
        
        buttonPanel.add(addParamButton);
        buttonPanel.add(removeParamButton);
        buttonPanel.add(saveParamsButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 添加新的URL配置
     */
    private void addNewConfig() {
        String name = Messages.showInputDialog(project, "请输入配置名称:", "添加新配置", Messages.getQuestionIcon());
        if (name != null && !name.trim().isEmpty()) {
            // 创建新配置
            UrlConfig newConfig = new UrlConfig(name, "localhost", "", "http");
            
            // 添加到配置
            ConfigSettings settings = ConfigSettings.getInstance(project);
            settings.addUrlConfig(newConfig);
            
            // 更新UI
            refreshConfigsComboBox();
            
            // 选中新创建的配置
            configsComboBox.setSelectedItem(newConfig);
        }
    }
    
    /**
     * 删除选中的URL配置
     */
    private void deleteSelectedConfig() {
        UrlConfig selectedConfig = (UrlConfig) configsComboBox.getSelectedItem();
        if (selectedConfig == null) {
            return;
        }
        
        // 确认对话框
        int result = Messages.showYesNoDialog(
                project,
                "确定要删除配置 \"" + selectedConfig.getDisplayName() + "\" 吗?",
                "删除配置",
                Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            ConfigSettings settings = ConfigSettings.getInstance(project);
            boolean deleted = settings.removeUrlConfig(selectedConfig.getId());
            
            if (!deleted) {
                Messages.showMessageDialog(
                        project,
                        "至少需要保留一个配置，无法删除。",
                        "提示",
                        Messages.getInformationIcon()
                );
                return;
            }
            
            // 更新UI
            refreshConfigsComboBox();
            
            // 选中第一个配置
            if (configsComboBoxModel.getSize() > 0) {
                configsComboBox.setSelectedIndex(0);
            }
        }
    }
    
    /**
     * 更新当前选中的配置
     */
    private void updateCurrentConfig() {
        UrlConfig selectedConfig = (UrlConfig) configsComboBox.getSelectedItem();
        if (selectedConfig == null) {
            return;
        }
        
        // 更新配置数据
        selectedConfig.setName(configNameField.getText());
        selectedConfig.setHost(hostField.getText());
        selectedConfig.setContextPath(contextPathField.getText());
        selectedConfig.setProtocol((String) protocolComboBox.getSelectedItem());
        
        // 保存到设置
        ConfigSettings settings = ConfigSettings.getInstance(project);
        settings.updateUrlConfig(selectedConfig);
        
        // 刷新下拉框显示
        configsComboBox.repaint();
    }
    
    /**
     * 刷新配置下拉框
     */
    private void refreshConfigsComboBox() {
        // 保存当前选中的配置ID
        ConfigSettings settings = ConfigSettings.getInstance(project);
        String activeConfigId = settings.getActiveConfigId();
        
        // 清空下拉框并重新加载配置
        configsComboBoxModel.removeAllElements();
        for (UrlConfig config : settings.getUrlConfigs()) {
            configsComboBoxModel.addElement(config);
            
            // 如果是当前活动配置，选中它
            if (config.getId().equals(activeConfigId)) {
                configsComboBox.setSelectedItem(config);
            }
        }
    }
    
    /**
     * 将配置加载到表单
     * 
     * @param config 要加载的配置
     */
    private void loadConfigToForm(UrlConfig config) {
        if (config == null) {
            return;
        }
        
        configNameField.setText(config.getName());
        hostField.setText(config.getHost());
        contextPathField.setText(config.getContextPath());
        protocolComboBox.setSelectedItem(config.getProtocol());
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
        
        // 加载URL配置
        refreshConfigsComboBox();
        
        // 加载当前选中的配置到表单
        UrlConfig activeConfig = settings.getActiveConfig();
        if (activeConfig != null) {
            loadConfigToForm(activeConfig);
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
            UrlConfig config = (UrlConfig) configsComboBox.getSelectedItem();
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
        Component[] components = ((JPanel) getComponent(0)).getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                Component[] subComponents = ((JPanel) component).getComponents();
                for (Component subComponent : subComponents) {
                    if (subComponent instanceof JPanel && ((JPanel) subComponent).getBorder() instanceof TitledBorder) {
                        Component[] panelComponents = ((JPanel) subComponent).getComponents();
                        for (Component panelComponent : panelComponents) {
                            if (panelComponent instanceof JPanel) {
                                Component[] innerComponents = ((JPanel) panelComponent).getComponents();
                                for (int i = 0; i < innerComponents.length; i++) {
                                    if (innerComponents[i] instanceof JLabel && 
                                        ((JLabel) innerComponents[i]).getText().equals("URL预览:") &&
                                        i + 1 < innerComponents.length &&
                                        innerComponents[i + 1] instanceof JLabel) {
                                        return (JLabel) innerComponents[i + 1];
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
