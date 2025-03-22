package com.rhw.weburlcopy.window;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.rhw.weburlcopy.model.ConfigSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * 配置工具窗口面板
 */
public class ConfigToolWindowPanel extends JBPanel<ConfigToolWindowPanel> {
    private final Project project;
    private JBTextField hostField;
    private DefaultTableModel tableModel;
    private JTable headersTable;
    private JBTextField headerKeyField;
    private JBTextField headerValueField;

    public ConfigToolWindowPanel(Project project) {
        this.project = project;
        initPanel();
    }

    private void initPanel() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(5));

        // 上部分 - 主机设置
        JPanel hostPanel = new JPanel(new BorderLayout());
        hostPanel.setBorder(BorderFactory.createTitledBorder("主机地址"));
        
        hostField = new JBTextField();
        JButton saveHostButton = new JButton("保存");
        saveHostButton.addActionListener(e -> saveHost());
        
        hostPanel.add(hostField, BorderLayout.CENTER);
        hostPanel.add(saveHostButton, BorderLayout.EAST);
        
        // 中部分 - 请求头表格
        JPanel headersPanel = new JPanel(new BorderLayout());
        headersPanel.setBorder(BorderFactory.createTitledBorder("请求头"));
        
        String[] columnNames = {"键", "值"};
        tableModel = new DefaultTableModel(columnNames, 0);
        headersTable = new JBTable(tableModel);
        headersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JBScrollPane scrollPane = new JBScrollPane(headersTable);
        
        // 表格下方的添加/删除按钮
        JPanel tableButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("删除选中行");
        deleteButton.addActionListener(e -> deleteSelectedHeader());
        tableButtonsPanel.add(deleteButton);
        
        headersPanel.add(scrollPane, BorderLayout.CENTER);
        headersPanel.add(tableButtonsPanel, BorderLayout.SOUTH);
        
        // 下部分 - 添加请求头
        JPanel addHeaderPanel = new JPanel(new BorderLayout());
        addHeaderPanel.setBorder(BorderFactory.createTitledBorder("添加请求头"));
        
        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        fieldsPanel.add(new JBLabel("键:"));
        headerKeyField = new JBTextField();
        fieldsPanel.add(headerKeyField);
        fieldsPanel.add(new JBLabel("值:"));
        headerValueField = new JBTextField();
        fieldsPanel.add(headerValueField);
        
        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> addHeader());
        
        addHeaderPanel.add(fieldsPanel, BorderLayout.CENTER);
        addHeaderPanel.add(addButton, BorderLayout.SOUTH);
        
        // 组装面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(hostPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(headersPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(addHeaderPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 加载配置
        loadSettings();
    }

    private void loadSettings() {
        ConfigSettings settings = ConfigSettings.getInstance(project);
        
        // 设置主机地址
        hostField.setText(settings.getHost());
        
        // 清空表格
        tableModel.setRowCount(0);
        
        // 加载请求头
        for (Map.Entry<String, String> entry : settings.getHeaders().entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void saveHost() {
        ConfigSettings settings = ConfigSettings.getInstance(project);
        settings.setHost(hostField.getText().trim());
    }

    private void addHeader() {
        String key = headerKeyField.getText().trim();
        String value = headerValueField.getText().trim();
        
        if (key.isEmpty()) {
            return;
        }
        
        // 保存到设置
        ConfigSettings settings = ConfigSettings.getInstance(project);
        settings.addHeader(key, value);
        
        // 更新表格
        tableModel.addRow(new Object[]{key, value});
        
        // 清空输入框
        headerKeyField.setText("");
        headerValueField.setText("");
    }

    private void deleteSelectedHeader() {
        int selectedRow = headersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String key = (String) tableModel.getValueAt(selectedRow, 0);
            
            // 从设置中删除
            ConfigSettings settings = ConfigSettings.getInstance(project);
            settings.removeHeader(key);
            
            // 从表格中删除
            tableModel.removeRow(selectedRow);
        }
    }
}
