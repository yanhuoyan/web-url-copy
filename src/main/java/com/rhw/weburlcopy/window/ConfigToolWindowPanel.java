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

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * 配置工具窗口面板
 */
public class ConfigToolWindowPanel extends JBPanel<ConfigToolWindowPanel> {
    private final Project project;
    private JBTextField hostField;
    private JBTextField contextPathField;
    private JComboBox<String> protocolComboBox;
    
    // 请求头表格
    private DefaultTableModel headersTableModel;
    private JTable headersTable;
    
    // 默认参数表格
    private DefaultTableModel paramsTableModel;
    private JTable paramsTable;

    public ConfigToolWindowPanel(Project project) {
        this.project = project;
        initPanel();
    }

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
        
        // 协议选择框
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = JBUI.insets(5, 0, 5, 10);
        JBLabel protocolLabel = new JBLabel("协议:");
        innerPanel.add(protocolLabel, c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.3;
        protocolComboBox = new JComboBox<>(new String[]{"http", "https"});
        protocolComboBox.setToolTipText("选择HTTP或HTTPS协议");
        innerPanel.add(protocolComboBox, c);
        
        // 主机地址标签
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = JBUI.insets(5, 0, 5, 10);
        JBLabel hostLabel = new JBLabel("主机地址:");
        hostLabel.setToolTipText("例如: api.example.com");
        innerPanel.add(hostLabel, c);
        
        // 主机地址输入框
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        hostField = new JBTextField();
        hostField.setToolTipText("请输入主机地址，例如: api.example.com（无需协议前缀）");
        innerPanel.add(hostField, c);
        
        // 上下文路径标签
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        JBLabel contextLabel = new JBLabel("上下文路径:");
        contextLabel.setToolTipText("例如: /api/v1");
        innerPanel.add(contextLabel, c);
        
        // 上下文路径输入框
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        contextPathField = new JBTextField();
        contextPathField.setToolTipText("请输入API的上下文路径，例如: /api/v1");
        innerPanel.add(contextPathField, c);
        
        // URL预览标签
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        JBLabel previewLabel = new JBLabel("URL预览:");
        innerPanel.add(previewLabel, c);
        
        // URL预览内容
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        JLabel urlPreviewLabel = new JLabel("http://localhost");
        urlPreviewLabel.setForeground(UIUtil.getContextHelpForeground());
        innerPanel.add(urlPreviewLabel, c);
        
        // 添加URL预览更新监听器
        protocolComboBox.addActionListener(e -> updateUrlPreview(urlPreviewLabel));
        hostField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateUrlPreview(urlPreviewLabel);
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateUrlPreview(urlPreviewLabel);
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateUrlPreview(urlPreviewLabel);
            }
        });
        contextPathField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateUrlPreview(urlPreviewLabel);
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateUrlPreview(urlPreviewLabel);
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateUrlPreview(urlPreviewLabel);
            }
        });
        
        // 保存按钮
        c.gridx = 2;
        c.gridy = 4;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = JBUI.insets(15, 0, 5, 0);
        JButton saveButton = new JButton("保存配置", AllIcons.Actions.MenuSaveall);
        saveButton.addActionListener(e -> saveUrlConfig());
        innerPanel.add(saveButton, c);
        
        panel.add(innerPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void updateUrlPreview(JLabel previewLabel) {
        String protocol = (String) protocolComboBox.getSelectedItem();
        String host = hostField.getText().trim();
        String contextPath = contextPathField.getText().trim();
        
        StringBuilder preview = new StringBuilder();
        preview.append(protocol).append("://").append(host);
        
        if (!contextPath.isEmpty()) {
            if (!contextPath.startsWith("/")) {
                preview.append("/");
            }
            preview.append(contextPath);
        }
        
        previewLabel.setText(preview.toString());
    }
    
    private JPanel createHeadersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel innerPanel = new JPanel(new BorderLayout(0, 10));
        
        // 创建表格
        String[] columnNames = {"键", "值"};
        headersTableModel = new DefaultTableModel(columnNames, 0);
        headersTable = new JBTable(headersTableModel);
        headersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        headersTable.setRowHeight(25);
        
        // 双击行进行编辑
        headersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = headersTable.getSelectedRow();
                    if (row >= 0) {
                        editHeader(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JBScrollPane(headersTable);
        scrollPane.setPreferredSize(new Dimension(-1, 150));
        
        // 表格按钮面板
        JPanel tableButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        JButton addButton = new JButton("添加", AllIcons.General.Add);
        addButton.addActionListener(e -> showAddHeaderDialog());
        tableButtonsPanel.add(addButton);
        
        JButton editButton = new JButton("编辑", AllIcons.Actions.Edit);
        editButton.addActionListener(e -> {
            int selectedRow = headersTable.getSelectedRow();
            if (selectedRow >= 0) {
                editHeader(selectedRow);
            } else {
                Messages.showInfoMessage("请先选择一行", "编辑请求头");
            }
        });
        tableButtonsPanel.add(editButton);
        
        JButton deleteButton = new JButton("删除", AllIcons.General.Remove);
        deleteButton.addActionListener(e -> deleteSelectedHeader());
        tableButtonsPanel.add(deleteButton);
        
        // 添加到布局
        innerPanel.add(scrollPane, BorderLayout.CENTER);
        innerPanel.add(tableButtonsPanel, BorderLayout.SOUTH);
        
        panel.add(innerPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createDefaultParamsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel innerPanel = new JPanel(new BorderLayout(0, 10));
        
        // 创建表格
        String[] columnNames = {"参数名", "默认值"};
        paramsTableModel = new DefaultTableModel(columnNames, 0);
        paramsTable = new JBTable(paramsTableModel);
        paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paramsTable.setRowHeight(25);
        
        // 双击行进行编辑
        paramsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = paramsTable.getSelectedRow();
                    if (row >= 0) {
                        editDefaultParam(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JBScrollPane(paramsTable);
        scrollPane.setPreferredSize(new Dimension(-1, 150));
        
        // 表格说明
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel(
            "<html><body>配置请求参数的默认值。当生成请求时，如果参数名匹配，将使用默认值替代生成的值。</body></html>",
            AllIcons.General.Information, SwingConstants.LEFT);
        infoLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.PLAIN));
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // 表格按钮面板
        JPanel tableButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        JButton addButton = new JButton("添加", AllIcons.General.Add);
        addButton.addActionListener(e -> showAddParamDialog());
        tableButtonsPanel.add(addButton);
        
        JButton editButton = new JButton("编辑", AllIcons.Actions.Edit);
        editButton.addActionListener(e -> {
            int selectedRow = paramsTable.getSelectedRow();
            if (selectedRow >= 0) {
                editDefaultParam(selectedRow);
            } else {
                Messages.showInfoMessage("请先选择一行", "编辑默认参数");
            }
        });
        tableButtonsPanel.add(editButton);
        
        JButton deleteButton = new JButton("删除", AllIcons.General.Remove);
        deleteButton.addActionListener(e -> deleteSelectedParam());
        tableButtonsPanel.add(deleteButton);
        
        // 创建一个容器包含信息面板和表格
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(infoPanel, BorderLayout.NORTH);
        topPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加到布局
        innerPanel.add(topPanel, BorderLayout.CENTER);
        innerPanel.add(tableButtonsPanel, BorderLayout.SOUTH);
        
        panel.add(innerPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void showAddHeaderDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JTextField keyField = new JTextField(15);
        JTextField valueField = new JTextField(15);
        
        panel.add(new JLabel("键:"));
        panel.add(keyField);
        panel.add(new JLabel("值:"));
        panel.add(valueField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "添加请求头", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String key = keyField.getText().trim();
            String value = valueField.getText().trim();
            
            if (!key.isEmpty()) {
                // 保存到设置
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.addHeader(key, value);
                
                // 更新表格
                headersTableModel.addRow(new Object[]{key, value});
            }
        }
    }
    
    private void showAddParamDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JTextField keyField = new JTextField(15);
        JTextField valueField = new JTextField(15);
        
        panel.add(new JLabel("参数名:"));
        panel.add(keyField);
        panel.add(new JLabel("默认值:"));
        panel.add(valueField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "添加默认参数", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String key = keyField.getText().trim();
            String value = valueField.getText().trim();
            
            if (!key.isEmpty()) {
                // 保存到设置
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.addDefaultParameter(key, value);
                
                // 更新表格
                paramsTableModel.addRow(new Object[]{key, value});
            }
        }
    }
    
    private void editHeader(int row) {
        String key = (String) headersTableModel.getValueAt(row, 0);
        String value = (String) headersTableModel.getValueAt(row, 1);
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JTextField keyField = new JTextField(key, 15);
        JTextField valueField = new JTextField(value, 15);
        
        panel.add(new JLabel("键:"));
        panel.add(keyField);
        panel.add(new JLabel("值:"));
        panel.add(valueField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "编辑请求头", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newKey = keyField.getText().trim();
            String newValue = valueField.getText().trim();
            
            if (!newKey.isEmpty()) {
                // 保存到设置
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.removeHeader(key);
                settings.addHeader(newKey, newValue);
                
                // 更新表格
                headersTableModel.setValueAt(newKey, row, 0);
                headersTableModel.setValueAt(newValue, row, 1);
            }
        }
    }
    
    private void editDefaultParam(int row) {
        String key = (String) paramsTableModel.getValueAt(row, 0);
        String value = (String) paramsTableModel.getValueAt(row, 1);
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JTextField keyField = new JTextField(key, 15);
        JTextField valueField = new JTextField(value, 15);
        
        panel.add(new JLabel("参数名:"));
        panel.add(keyField);
        panel.add(new JLabel("默认值:"));
        panel.add(valueField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "编辑默认参数", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newKey = keyField.getText().trim();
            String newValue = valueField.getText().trim();
            
            if (!newKey.isEmpty()) {
                // 保存到设置
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.removeDefaultParameter(key);
                settings.addDefaultParameter(newKey, newValue);
                
                // 更新表格
                paramsTableModel.setValueAt(newKey, row, 0);
                paramsTableModel.setValueAt(newValue, row, 1);
            }
        }
    }

    private void loadSettings() {
        ConfigSettings settings = ConfigSettings.getInstance(project);
        
        // 设置URL相关字段
        protocolComboBox.setSelectedItem(settings.getProtocol());
        hostField.setText(settings.getHost());
        contextPathField.setText(settings.getContextPath());
        
        // 清空表格
        headersTableModel.setRowCount(0);
        paramsTableModel.setRowCount(0);
        
        // 加载请求头
        for (Map.Entry<String, String> entry : settings.getHeaders().entrySet()) {
            headersTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        
        // 加载默认参数
        for (Map.Entry<String, String> entry : settings.getDefaultParameters().entrySet()) {
            paramsTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void saveUrlConfig() {
        ConfigSettings settings = ConfigSettings.getInstance(project);
        settings.setProtocol((String) protocolComboBox.getSelectedItem());
        settings.setHost(hostField.getText().trim());
        settings.setContextPath(contextPathField.getText().trim());
        Messages.showInfoMessage("URL配置已保存", "保存成功");
    }

    private void deleteSelectedHeader() {
        int selectedRow = headersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String key = (String) headersTableModel.getValueAt(selectedRow, 0);
            
            // 确认删除
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "确定要删除 '" + key + "' 吗?", 
                    "删除请求头", 
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // 从设置中删除
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.removeHeader(key);
                
                // 从表格中删除
                headersTableModel.removeRow(selectedRow);
            }
        }
    }
    
    private void deleteSelectedParam() {
        int selectedRow = paramsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String key = (String) paramsTableModel.getValueAt(selectedRow, 0);
            
            // 确认删除
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "确定要删除默认参数 '" + key + "' 吗?", 
                    "删除默认参数", 
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // 从设置中删除
                ConfigSettings settings = ConfigSettings.getInstance(project);
                settings.removeDefaultParameter(key);
                
                // 从表格中删除
                paramsTableModel.removeRow(selectedRow);
            }
        }
    }
}
