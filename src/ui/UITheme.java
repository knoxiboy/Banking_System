package Projects_java.Banking_System.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Centralized theme and UI styling helper for the Banking System GUI.
 * Provides consistent modern palette, fonts, styled buttons, and form components.
 */
public class UITheme {

    // ─── Color Palette ────────────────────────────────────────────────────────
    public static final Color PRIMARY      = new Color(13, 71, 161);    // Deep Indigo/Navy
    public static final Color PRIMARY_HOVER= new Color(25, 118, 210);   // Accent Blue
    public static final Color SUCCESS      = new Color(46, 125, 50);    // Forest Green
    public static final Color DANGER       = new Color(198, 40, 40);    // Crimson
    public static final Color WARNING      = new Color(239, 108, 0);    // Amber
    public static final Color BG_LIGHT     = new Color(245, 247, 250);  // Soft Cloud
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color TEXT_DARK    = new Color(33, 33, 33);
    public static final Color TEXT_MUTED   = new Color(117, 117, 117);
    public static final Color BORDER_COLOR = new Color(207, 216, 220);

    // ─── Fonts ────────────────────────────────────────────────────────────────
    public static final Font TITLE_FONT    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font HEADER_FONT   = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font LABEL_FONT    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font BUTTON_FONT   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font MONO_FONT     = new Font("Consolas", Font.BOLD, 16);

    /**
     * Creates a standardized branded header banner.
     */
    public static JPanel createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(5, 5));
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(TITLE_FONT);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(SUBTITLE_FONT);
        subLbl.setForeground(new Color(227, 242, 253));

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(subLbl, BorderLayout.SOUTH);
        return header;
    }

    /**
     * Creates a styled modern JButton with hover effects.
     */
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    /**
     * Creates a styled JTextField with padding and clean border.
     */
    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(BODY_FONT);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    /**
     * Creates a styled JPasswordField with padding and clean border.
     */
    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(BODY_FONT);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    /**
     * Helper to wrap a form field with its label into a container panel.
     */
    public static JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_DARK);

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
}
