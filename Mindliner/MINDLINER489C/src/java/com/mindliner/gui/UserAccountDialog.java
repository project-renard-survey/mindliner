/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.common.ObjectCheckBox;
import com.mindliner.comparatorsS.ConfidentialityComparator;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 * Displays account and user information.
 *
 * @author Marius Messerli
 */
public class UserAccountDialog extends javax.swing.JDialog {

    /**
     * Creates new form UserAccountDialog
     * @param parent The parent component
     * @param modal true if the dialog is displayed modal
     */
    public UserAccountDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configureComponents();
    }

    private void configureComponents() {
        mlcUser u = CacheEngineStatic.getCurrentUser();
        PrefsUsernameLabel.setText(u.getLoginName());
        FirstnameInput.setText(u.getFirstName());
        LastnameInput.setText(u.getLastName());
        EmailInput.setText(u.getEmail());
        UpdateUserDetails.setEnabled(false);
        PrefsLoginCountLabel.setText(Integer.toString(u.getLoginCount()));
        SimpleDateFormat sdf = new SimpleDateFormat();
        String dateString;
        if (u.getLastLogout() != null) {
            dateString = sdf.format(u.getLastLogout());
        } else {
            dateString = "never";
        }
        PrefsAcntLastLogoutLabel.setText(dateString);

        if (u.getLastLogin() != null) {
            dateString = sdf.format(u.getLastLogin());
        } else {
            dateString = "never";
        }
        AccountLastLoginLabel.setText(dateString);

        // set the confidentiality label
        List<mlsConfidentiality> confidentialities = CacheEngineStatic.getConfidentialities();
        Collections.sort(confidentialities, new ConfidentialityComparator());
        StringBuilder sb = new StringBuilder();
        Iterator<Integer> clientIdIterator = u.getClientIds().iterator();
        while (clientIdIterator.hasNext()) {
            int cid = clientIdIterator.next();
            mlcClient client = CacheEngineStatic.getClient(cid);
            if (client != null) {
                sb.append(client.getName());
                if (clientIdIterator.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        PrefsAcntDatapools.setText(sb.toString());
        DefaultListModel dlm = new DefaultListModel();
        for (SoftwareFeature f : CacheEngineStatic.getFeatures()) {
            try {
                SoftwareFeature.CurrentFeatures feature = SoftwareFeature.CurrentFeatures.valueOf(f.getName());
                boolean authorized = u.isAuthorizedForFeature(feature);
                dlm.addElement(new ObjectCheckBox(f, authorized));
            } catch (IllegalArgumentException ex) {
                // most probably a cached feature was removed from the new version of the software
            }
        }
        FeatureList.setModel(dlm);
        FeatureList.setEnabled(false);
        setLocationRelativeTo(MindlinerMain.getInstance());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        FeatureList = new com.mindliner.common.CheckBoxList();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        PrefsUsernameLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        PrefsLoginCountLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        AccountLastLoginLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        PrefsAcntLastLogoutLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        FirstnameInput = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        LastnameInput = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        EmailInput = new javax.swing.JTextField();
        UpdateUserDetails = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        PrefsAcntDatapools = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/General"); // NOI18N
        setTitle(bundle.getString("AccountDialogTitle")); // NOI18N

        jLabel7.setText(bundle.getString("PrefsAcntSoftwareFeatures")); // NOI18N

        FeatureList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(FeatureList);

        jLabel1.setText(bundle.getString("PrefsAcntUsername")); // NOI18N

        PrefsUsernameLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        PrefsUsernameLabel.setText("jLabel2");

        jLabel2.setText(bundle.getString("PrefsAcnt_LoginCountLabel")); // NOI18N

        PrefsLoginCountLabel.setText("jLabel5");

        jLabel3.setText(bundle.getString("AccountLastLoginLabel")); // NOI18N

        AccountLastLoginLabel.setText("jLabel8");

        jLabel5.setText(bundle.getString("PrefsAcnt_LastLogoutLabel")); // NOI18N

        PrefsAcntLastLogoutLabel.setText("jLabel6");

        jLabel4.setText(bundle.getString("UserAccountDialog_FirstnameLabel")); // NOI18N

        FirstnameInput.setText("jTextField1");
        FirstnameInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                FirstnameInputKeyTyped(evt);
            }
        });

        jLabel8.setText(bundle.getString("UserAccountDialog_LastnameLabel")); // NOI18N

        LastnameInput.setText("jTextField2");
        LastnameInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                LastnameInputKeyTyped(evt);
            }
        });

        jLabel9.setText(bundle.getString("UserAccountDialog_EmailLabel")); // NOI18N

        EmailInput.setText("jTextField3");
        EmailInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                EmailInputKeyTyped(evt);
            }
        });

        UpdateUserDetails.setText(bundle.getString("UserAccountDialog_UpdateButtonLabel")); // NOI18N
        UpdateUserDetails.setFocusable(false);
        UpdateUserDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateUserDetailsActionPerformed(evt);
            }
        });

        jLabel6.setText(bundle.getString("PrefsDialogDataPools")); // NOI18N

        PrefsAcntDatapools.setText("jLabel10");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel6))
                .addGap(51, 51, 51)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PrefsAcntDatapools, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(UpdateUserDetails, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(LastnameInput, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .addComponent(AccountLastLoginLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PrefsLoginCountLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PrefsUsernameLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PrefsAcntLastLogoutLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(FirstnameInput, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(EmailInput))
                        .addGap(0, 69, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(PrefsUsernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(FirstnameInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(LastnameInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(EmailInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UpdateUserDetails)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(PrefsLoginCountLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AccountLastLoginLabel)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PrefsAcntLastLogoutLabel)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(PrefsAcntDatapools))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void UpdateUserDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateUserDetailsActionPerformed
        try {
            mlcUser u = CacheEngineStatic.getCurrentUser();
            UserManagerRemote userManager = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class);
            userManager.setContactDetails(FirstnameInput.getText(), LastnameInput.getText(), EmailInput.getText());
            u.setFirstName(FirstnameInput.getText());
            u.setLastName(LastnameInput.getText());
            u.setEmail(EmailInput.getText());
        } catch (NamingException ex) {
            JOptionPane.showMessageDialog(null, "User Update Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_UpdateUserDetailsActionPerformed

    private void FirstnameInputKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FirstnameInputKeyTyped
        UpdateUserDetails.setEnabled(true);
    }//GEN-LAST:event_FirstnameInputKeyTyped

    private void LastnameInputKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LastnameInputKeyTyped
        UpdateUserDetails.setEnabled(true);
    }//GEN-LAST:event_LastnameInputKeyTyped

    private void EmailInputKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EmailInputKeyTyped
        UpdateUserDetails.setEnabled(true);
    }//GEN-LAST:event_EmailInputKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AccountLastLoginLabel;
    private javax.swing.JTextField EmailInput;
    private com.mindliner.common.CheckBoxList FeatureList;
    private javax.swing.JTextField FirstnameInput;
    private javax.swing.JTextField LastnameInput;
    private javax.swing.JLabel PrefsAcntDatapools;
    private javax.swing.JLabel PrefsAcntLastLogoutLabel;
    private javax.swing.JLabel PrefsLoginCountLabel;
    private javax.swing.JLabel PrefsUsernameLabel;
    private javax.swing.JButton UpdateUserDetails;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
