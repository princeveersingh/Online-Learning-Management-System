package com.lms.gui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ButtonEditor — makes a JButton inside a JTable cell clickable.
 * Works with ButtonRenderer to create an interactive "Enroll" button.
 */
public class ButtonEditor extends DefaultCellEditor {

    private final JButton button;
    private final Runnable onClick;
    private String label;

    public ButtonEditor(JCheckBox checkBox, Runnable onClick) {
        super(checkBox);
        this.onClick = onClick;
        this.button = new JButton();
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.addActionListener(e -> {
            fireEditingStopped();
            onClick.run();
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        label = value != null ? value.toString() : "";
        button.setText(label);
        if (label.startsWith("Enrolled")) {
            button.setBackground(new Color(60, 120, 60));
        } else {
            button.setBackground(new Color(66, 135, 245));
        }
        button.setForeground(Color.WHITE);
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return label;
    }
}
