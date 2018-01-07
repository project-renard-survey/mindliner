/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.exporter.ObjectTextExporter;
import java.awt.Dimension;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.main.MindlinerMain;

/**
 *
 * @author Marius Messerli, 4-feb-2015
 */
public class MapClipboardDialog extends javax.swing.JDialog {

    private MlMapNode currentNode = null;

    /**
     * Creates new form MapClipboardDialog
     * @param parent The parent frame
     * @param modal Whether the dialog is presented in model or non-model form
     * @param currentNode The node from which the content is taken
     */
    public MapClipboardDialog(java.awt.Frame parent, boolean modal, MlMapNode currentNode) {
        super(parent, modal);
        initComponents();
        this.currentNode = currentNode;
        setLocationRelativeTo(MindlinerMain.getInstance());
        updateClipboardPrepPane();
    }

    private void updateClipboardPrepPane() {
        if (currentNode != null) {
            ClipboardPrepPane.setText(
                    ObjectTextExporter.formatNodeAsHTML(
                            currentNode,
                            ClipId.isSelected(),
                            ClipOwner.isSelected(),
                            ClipModificationDate.isSelected(),
                            ClipDescription.isSelected()));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane4 = new javax.swing.JScrollPane();
        ClipboardPrepPane = new javax.swing.JEditorPane();
        jPanel5 = new javax.swing.JPanel();
        ClipId = new javax.swing.JCheckBox();
        ClipModificationDate = new javax.swing.JCheckBox();
        ClipOwner = new javax.swing.JCheckBox();
        ClipDescription = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        ClipboardPrepPane.setEditable(false);
        ClipboardPrepPane.setContentType("text/html"); // NOI18N
        ClipboardPrepPane.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/Mapper"); // NOI18N
        ClipboardPrepPane.setToolTipText(bundle.getString("ClipboardPrepEditor_TT")); // NOI18N
        ClipboardPrepPane.setPreferredSize(new java.awt.Dimension(500, 500));
        jScrollPane4.setViewportView(ClipboardPrepPane);

        getContentPane().add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 0));

        ClipId.setSelected(true);
        ClipId.setText(bundle.getString("MapClipboardExportID")); // NOI18N
        ClipId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClipIdActionPerformed(evt);
            }
        });
        jPanel5.add(ClipId);

        ClipModificationDate.setText(bundle.getString("MapClipboardExportModificationDate")); // NOI18N
        ClipModificationDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClipModificationDateActionPerformed(evt);
            }
        });
        jPanel5.add(ClipModificationDate);

        ClipOwner.setText(bundle.getString("MapClipboardExportOwner")); // NOI18N
        ClipOwner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClipOwnerActionPerformed(evt);
            }
        });
        jPanel5.add(ClipOwner);

        ClipDescription.setText(bundle.getString("ClipboardExportDescription")); // NOI18N
        ClipDescription.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClipDescriptionActionPerformed(evt);
            }
        });
        jPanel5.add(ClipDescription);

        getContentPane().add(jPanel5, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ClipIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClipIdActionPerformed
        updateClipboardPrepPane();
    }//GEN-LAST:event_ClipIdActionPerformed

    private void ClipModificationDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClipModificationDateActionPerformed
        updateClipboardPrepPane();
    }//GEN-LAST:event_ClipModificationDateActionPerformed

    private void ClipOwnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClipOwnerActionPerformed
        updateClipboardPrepPane();
    }//GEN-LAST:event_ClipOwnerActionPerformed

    private void ClipDescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClipDescriptionActionPerformed
        updateClipboardPrepPane();
    }//GEN-LAST:event_ClipDescriptionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox ClipDescription;
    private javax.swing.JCheckBox ClipId;
    private javax.swing.JCheckBox ClipModificationDate;
    private javax.swing.JCheckBox ClipOwner;
    private javax.swing.JEditorPane ClipboardPrepPane;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane4;
    // End of variables declaration//GEN-END:variables
}
