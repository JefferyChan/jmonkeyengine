/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.android.properties;

import com.jme3.gde.android.AndroidSdkTool;
import java.io.File;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

final class MobilePanel extends javax.swing.JPanel {

    private final MobileOptionsPanelController controller;

    MobilePanel(MobileOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MobilePanel.class, "MobilePanel.jLabel1.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(MobilePanel.class, "MobilePanel.jTextField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(MobilePanel.class, "MobilePanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(MobilePanel.class, "MobilePanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jButton2))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addContainerGap(122, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    FileChooserBuilder builder = new FileChooserBuilder(AndroidSdkTool.class);
    builder.setTitle("Please select Android SDK Folder");
    builder.setDirectoriesOnly(true);
    File file = builder.showOpenDialog();
    if (file != null) {
        FileObject folder = FileUtil.toFileObject(file);
        if (folder.getFileObject("tools") == null) {
            Message msg = new NotifyDescriptor.Message(
                    "Not a valid SDK folder!",
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);

        } else {
            String name = file.getPath();
            jTextField1.setText(name);
        }
    }
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    AndroidSdkTool.startAndroidTool();
}//GEN-LAST:event_jButton2ActionPerformed

    void load() {
//         jTextField2.setText(NbPreferences.forModule(AndroidSdkTool.class).get("assetpack_path", null));
        jTextField1.setText(NbPreferences.forModule(AndroidSdkTool.class).get("sdk_path", null));
//         jPasswordField1.setText(NbPreferences.forModule(AndroidSdkTool.class).get("assetpack_pass", null));
    }

    void store() {
//        NbPreferences.forModule(AndroidSdkTool.class).put("assetpack_path", jTextField2.getText());
        NbPreferences.forModule(AndroidSdkTool.class).put("sdk_path", jTextField1.getText());
//        NbPreferences.forModule(AndroidSdkTool.class).put("assetpack_pass", new String(jPasswordField1.getPassword()));
    }
//    void load() {
//        // TODO read settings and initialize GUI
//        // Example:        
//        // someCheckBox.setSelected(Preferences.userNodeForPackage(MobilePanel.class).getBoolean("someFlag", false));
//        // or for org.openide.util with API spec. version >= 7.4:
//        // someCheckBox.setSelected(NbPreferences.forModule(MobilePanel.class).getBoolean("someFlag", false));
//        // or:
//        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
//    }
//
//    void store() {
//        // TODO store modified settings
//        // Example:
//        // Preferences.userNodeForPackage(MobilePanel.class).putBoolean("someFlag", someCheckBox.isSelected());
//        // or for org.openide.util with API spec. version >= 7.4:
//        // NbPreferences.forModule(MobilePanel.class).putBoolean("someFlag", someCheckBox.isSelected());
//        // or:
//        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
//    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
