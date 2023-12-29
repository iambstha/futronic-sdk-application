 

package com.futronicApp.workedex;

import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;

/**
 * 
 * @author Shustikov
 */
public class SelectUser extends javax.swing.JDialog {

	/** Creates new form SelectUser */
	public SelectUser(java.awt.Frame parent, boolean modal, Vector<DbRecord> Users, String szDbDir) {
		super(parent, modal);
		initComponents();
		setLocationRelativeTo(parent);
		m_Users = Users;
		m_txtDatabaseDir.setText(szDbDir);
		DefaultListModel listModel = new DefaultListModel();
		for (int i = 0; i < m_Users.size(); i++) {
			listModel.addElement(m_Users.get(i).getUserName());
		}
		m_lstUsers.setModel(listModel);
		m_lstUsers.setSelectedIndex(0);
	}

	public DbRecord getRecord() {
		if ((m_Users.size() == 0) || (m_lstUsers.getSelectedIndex() == -1))
			return null;

		return m_Users.get(m_lstUsers.getSelectedIndex());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code
	// ">//GEN-BEGIN:initComponents
	private void initComponents() {
		jLabel1 = new javax.swing.JLabel();
		m_txtDatabaseDir = new javax.swing.JTextField();
		jScrollPane1 = new javax.swing.JScrollPane();
		m_lstUsers = new javax.swing.JList();
		m_btnSelect = new javax.swing.JButton();

		setTitle("Select User");
		setFont(new java.awt.Font("Tahoma", 0, 12));
		setModal(true);
		setName("");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12));
		jLabel1.setText("Database folder: ");

		m_txtDatabaseDir.setFont(new java.awt.Font("Tahoma", 0, 12));
		m_txtDatabaseDir.setEnabled(false);

		jScrollPane1.setFont(new java.awt.Font("Tahoma", 0, 12));
		m_lstUsers.setFont(new java.awt.Font("Tahoma", 0, 12));
		m_lstUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		jScrollPane1.setViewportView(m_lstUsers);

		m_btnSelect.setFont(new java.awt.Font("Tahoma", 0, 12));
		m_btnSelect.setText("Select");
		m_btnSelect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				m_btnSelectActionPerformed(evt);
			}
		});

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup().addComponent(jLabel1)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(m_txtDatabaseDir, GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)))
				.addContainerGap())
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addContainerGap(159, Short.MAX_VALUE).addComponent(m_btnSelect).addGap(164, 164, 164)));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(
						m_txtDatabaseDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 224, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(m_btnSelect, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void formWindowClosing(java.awt.event.WindowEvent evt)// GEN-FIRST:event_formWindowClosing
	{// GEN-HEADEREND:event_formWindowClosing
		m_lstUsers.clearSelection();
	}// GEN-LAST:event_formWindowClosing

	private void m_btnSelectActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_m_btnSelectActionPerformed
	{// GEN-HEADEREND:event_m_btnSelectActionPerformed
		setVisible(false);
	}// GEN-LAST:event_m_btnSelectActionPerformed

	private Vector<DbRecord> m_Users;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel jLabel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JButton m_btnSelect;
	private javax.swing.JList m_lstUsers;
	private javax.swing.JTextField m_txtDatabaseDir;
	// End of variables declaration//GEN-END:variables

}
