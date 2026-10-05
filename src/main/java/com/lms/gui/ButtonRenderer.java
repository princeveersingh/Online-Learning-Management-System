package com.lms.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * ButtonRenderer — renders a JButton inside a JTable cell.
 * Used in the StudentDashboard "Browse Courses" table for the "Enroll" button
 * column.
 */
public class ButtonRenderer extends JButton implements TableCellRenderer {

    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value != null ? value.toString() : "");
        setForeground(Color.WHITE);
        setBackground(value != null && value.toString().startsWith("Enrolled")
                ? new Color(60, 120, 60)
                : new Color(66, 135, 245));
        setFont(new Font("SansSerif", Font.BOLD, 11));
        setBorderPainted(false);
        setFocusPainted(false);
        return this;
    }
}
