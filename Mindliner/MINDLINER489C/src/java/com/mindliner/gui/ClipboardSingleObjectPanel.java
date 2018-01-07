/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Corresponds to one object which should be created from the content of the
 * clipboard
 *
 * @author Dominic Plangger
 */
public class ClipboardSingleObjectPanel extends javax.swing.JPanel {

    private mlcObject object = null;
    private ClipboardObjectsPanel parent = null;
    public static final String NO_HEADLINE_TEXT = "Enter Headline";

    /**
     * Creates new form ClipboardSingleObjectPanel
     */
    public ClipboardSingleObjectPanel() {
        initComponents();
        jScrollPane1.setSize(499, 100);
    }

    private void configureComponents() {
        if (object != null) {
            if (object.getHeadline() == null || object.getHeadline().isEmpty()) {
                setNoHeadlineText();
            } else {
                ObjectHeadlinePane.setText(object.getHeadline());
            }
            ObjectDescriptionPane.setText(object.getDescription());
            updateTypeLabel();
        }
    }

    public void setObject(mlcObject object) {
        this.object = object;
        configureComponents();
    }

    public void setParent(ClipboardObjectsPanel parent) {
        this.parent = parent;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        ObjectTypeLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ObjectHeadlinePane = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        ObjectDescriptionPane = new javax.swing.JTextArea();
        CloseLabel = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPanel1.setMaximumSize(new java.awt.Dimension(499, 126));
        jPanel1.setPreferredSize(new java.awt.Dimension(700, 150));

        ObjectTypeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/information2.png"))); // NOI18N
        ObjectTypeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ObjectTypeLabelMouseClicked(evt);
            }
        });

        jScrollPane1.setBorder(null);

        ObjectHeadlinePane.setBorder(javax.swing.BorderFactory.createTitledBorder("Headline"));
        ObjectHeadlinePane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ObjectHeadlinePaneFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                ObjectHeadlinePaneFocusLost(evt);
            }
        });
        jScrollPane1.setViewportView(ObjectHeadlinePane);

        jScrollPane2.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));

        ObjectDescriptionPane.setColumns(20);
        ObjectDescriptionPane.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ObjectDescriptionPane.setLineWrap(true);
        ObjectDescriptionPane.setRows(2);
        ObjectDescriptionPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ObjectDescriptionPaneFocusLost(evt);
            }
        });
        jScrollPane2.setViewportView(ObjectDescriptionPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(ObjectTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ObjectTypeLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        CloseLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/1616/delete2-2.png"))); // NOI18N
        CloseLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                CloseLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 564, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CloseLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CloseLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ObjectHeadlinePaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ObjectHeadlinePaneFocusLost
        object.setHeadline(ObjectHeadlinePane.getText());
        if (ObjectHeadlinePane.getText() == null || ObjectHeadlinePane.getText().isEmpty()) {
            setNoHeadlineText();
        }
    }//GEN-LAST:event_ObjectHeadlinePaneFocusLost

    private void ObjectTypeLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ObjectTypeLabelMouseClicked
        JPopupMenu jpopup = createTypePopup();
        jpopup.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_ObjectTypeLabelMouseClicked

    private void CloseLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseLabelMouseClicked
        setVisible(false);
        parent.removeObject(object);
    }//GEN-LAST:event_CloseLabelMouseClicked

    private void ObjectHeadlinePaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ObjectHeadlinePaneFocusGained
        if (NO_HEADLINE_TEXT.equals(ObjectHeadlinePane.getText())) {
            ObjectHeadlinePane.setForeground(Color.black);
            ObjectHeadlinePane.setText("");
        }
    }//GEN-LAST:event_ObjectHeadlinePaneFocusGained

    private void ObjectDescriptionPaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ObjectDescriptionPaneFocusLost
        object.setDescription(ObjectDescriptionPane.getText());
    }//GEN-LAST:event_ObjectDescriptionPaneFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CloseLabel;
    private javax.swing.JTextArea ObjectDescriptionPane;
    private javax.swing.JTextPane ObjectHeadlinePane;
    private javax.swing.JLabel ObjectTypeLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    private void updateTypeLabel() {
        String icon;
        if (object instanceof mlcKnowlet) {
            icon = "/com/mindliner/img/icons/2424/information2.png";
        } else if (object instanceof mlcObjectCollection) {
            icon = "/com/mindliner/img/icons/2424/folder2_blue.png";
        } else if (object instanceof mlcTask) {
            icon = "/com/mindliner/img/icons/2424/checkbox.png";
        } else {
            return;
        }
        MlClassHandler.MindlinerObjectType type = MlClientClassHandler.getTypeByClass(object.getClass());
        String text = MlClientClassHandler.getNameByType(type);
        ObjectTypeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(icon)));
        ObjectTypeLabel.setText(text);
    }

    public JPopupMenu createTypePopup() {
        JPopupMenu jPopup = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();
        List<Class> objectTypes = new ArrayList<>();
        objectTypes.add(mlcKnowlet.class);
        objectTypes.add(mlcObjectCollection.class);
        objectTypes.add(mlcTask.class);
        mlcUser user = CacheEngineStatic.getCurrentUser();
        for (final Class c : objectTypes) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
            item.addActionListener((ActionEvent e) -> {
                changeObjectType(c);
            });
            ClipboardObjectsPanel.setMenuItemLabel(item, c);
            group.add(item);
            jPopup.add(item);
        }
        return jPopup;
    }

    private void changeObjectType(Class c) {
        try {
            mlcObject newObj = (mlcObject) c.newInstance();
            newObj.setDescription(object.getDescription());
            newObj.setHeadline(object.getHeadline());
            newObj.setId(object.getId());
            parent.replaceObject(object, newObj);
            object = newObj;
            updateTypeLabel();
        } catch (Exception ex) {
            Logger.getLogger(ClipboardSingleObjectPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Unexpected error while changing object type. Keeping old object type.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setNoHeadlineText() {
        ObjectHeadlinePane.setText(NO_HEADLINE_TEXT);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setItalic(sas, true);
        ObjectHeadlinePane.setForeground(Color.red);
        ObjectHeadlinePane.getStyledDocument().setCharacterAttributes(0, NO_HEADLINE_TEXT.length(), sas, true);
    }
}
