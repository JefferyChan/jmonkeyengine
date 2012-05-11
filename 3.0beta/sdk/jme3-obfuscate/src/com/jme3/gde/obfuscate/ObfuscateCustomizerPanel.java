/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LwjglAppletCustomizerPanel.java
 *
 * Created on 11.11.2010, 16:56:53
 */
package com.jme3.gde.obfuscate;

import com.jme3.gde.core.j2seproject.ProjectExtensionProperties;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.HelpCtx;

/**
 *
 * @author normenhansen
 */
public class ObfuscateCustomizerPanel extends javax.swing.JPanel implements ActionListener {

    private ProjectExtensionProperties properties;

    /** Creates new form LwjglAppletCustomizerPanel */
    public ObfuscateCustomizerPanel(ProjectExtensionProperties properties) {
        this.properties = properties;
        initComponents();
        loadProperties();
        HelpCtx.setHelpIDString(this, "sdk.application_deployment");
    }

    private void loadProperties() {
        String str = properties.getProperty("obfuscate");
        if ("true".equals(str)) {
            jCheckBox1.setSelected(true);
        } else {
            jCheckBox1.setSelected(false);
        }
    }

    private void saveProperties() {
        if (jCheckBox1.isSelected()) {
            properties.setProperty("obfuscate", "true");
        } else {
            properties.setProperty("obfuscate", "");
        }
    }

    public void actionPerformed(ActionEvent e) {
        saveProperties();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new javax.swing.JCheckBox();

        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(ObfuscateCustomizerPanel.class, "ObfuscateCustomizerPanel.jCheckBox1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addContainerGap(241, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    // End of variables declaration//GEN-END:variables
}
