package ch.ehi.umleditor.application;

/* This file is part of the UML/INTERLIS-Editor.
 * For more information, please see <http://www.umleditor.org/>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.awt.*;
import javax.swing.*;
import ch.softenvironment.util.DeveloperException;
import ch.softenvironment.view.BasePanel;

import java.awt.event.*;
import javax.swing.table.DefaultTableModel;
import ch.ehi.interlis.modeltopicclass.AbstractClassDef;
import ch.ehi.uml1_4.foundation.core.ModelElement;

/**
 * panel to view/edit a list of participants (class+restriction).
 *
 * @author ce
 * @version $Revision: 1.1 $ $Date: 2007-12-27 17:00:24 $
 */
public class ParticipantsPanel extends BasePanel implements ch.softenvironment.view.DataPanel {
	
	private static final long serialVersionUID = -6631380805540174738L;

	static java.util.ResourceBundle rsrc = ch.ehi.basics.i18n.ResourceBundle.getBundle(ParticipantsPanel.class);

	private JScrollPane scpRestricted = new JScrollPane();
	private JTable tabRestricted = new JTable();
	private DefaultTableModel model = new DefaultTableModel() {
		public Class getColumnClass(int columnIndex) {
			return AbstractClassDef.class;
		}
	};
	private JPopupMenu popup = new JPopupMenu();
	private JMenuItem mniAdd = new JMenuItem();
	private JMenuItem mniRemove = new JMenuItem();
	private JMenuItem mniEdit = new JMenuItem();
	private ModelElement context = null;
	private JDialog thisDialog = null;

	public ParticipantsPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		// change default layout manager to gridbag, so that scrollpanel fills
		// the
		// available space
		setLayout(new GridBagLayout());
		model.addColumn(ElementUtils.getDisplayName(AbstractClassDef.class));
		scpRestricted.setViewportView(tabRestricted);
		tabRestricted.setModel(model);
		tabRestricted.setDefaultRenderer(AbstractClassDef.class, new javax.swing.table.DefaultTableCellRenderer() {
			public void setValue(Object aValue) {
				Object result = aValue;
				if ((aValue != null) && (aValue instanceof ParticipantsPanelItem)) {
					ParticipantsPanelItem stock = (ParticipantsPanelItem) aValue;
					result = ElementUtils.formatWithPackageName(stock.getTarget());
					if (stock.getRestrictions().length > 0) {
						result = "*" + result; // mark it so that user becomes
												// aware of restrictions
					}
					/*
					 * StringBuffer tooltip = new
					 * StringBuffer("Yahoo Ticker: ");
					 * tooltip.append(stock.getTicker()); String suffix =
					 * stock.getExchange().getTickerSuffix(); if (
					 * Util.textHasContent(suffix) ) { tooltip.append(".");
					 * tooltip.append(suffix); } setToolTipText(
					 * tooltip.toString() );
					 */
				}
				super.setValue(result);
			}
		});
		// add popup-menu
		popup.setName("MnuClassTable");
		MouseAdapter popupListener = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					boolean isSelected = tabRestricted.getSelectedRow() >= 0;
					mniRemove.setEnabled(isSelected);
					mniEdit.setEnabled(isSelected);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};
		tabRestricted.addMouseListener(popupListener);
		scpRestricted.addMouseListener(popupListener);
		// add 'add' menu-item
		mniAdd.setName("MniAddClassDef");
		mniAdd.setText(rsrc.getString("MniAdd_text"));
		mniAdd.setEnabled(true);
		mniAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelElementSelectionDialog dialog = new ModelElementSelectionDialog(thisDialog, "AbstractClassDef",
						true, context, AbstractClassDef.class);
				java.util.List selv = dialog.getSelectedItems();
				for (int i = 0; i < selv.size(); i++) {
					model.addRow(new Object[] { new ParticipantsPanelItem((AbstractClassDef) selv.get(i)) });
				}
			}
		});
		popup.add(mniAdd);
		// add 'remove' menu-item
		mniRemove.setName("MniRemoveClassDef");
		mniRemove.setText(rsrc.getString("MniRemove_text"));
		mniRemove.setEnabled(true);
		mniRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.removeRow(tabRestricted.getSelectedRow());
			}
		});
		popup.add(mniRemove);
		// add 'edit' menu-item
		mniEdit.setName("MniEditParticipant");
		mniEdit.setText(rsrc.getString("MniEdit_text"));
		mniEdit.setEnabled(true);
		mniEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selIdx = tabRestricted.getSelectedRow();
				ParticipantsPanelItem item = (ParticipantsPanelItem) model.getValueAt(selIdx, 0);
				ParticipantDialog dlg = new ParticipantDialog(thisDialog, "Edit Restrictions", true, item, context);
				if (dlg.isSaved()) {
					dlg.getItem();
					model.setValueAt(dlg.getItem(), selIdx, 0);
				}
			}
		});
		popup.add(mniEdit);

		this.add(scpRestricted,
				new GridBagConstraints(0 // gridx
						, 0 // gridy
						, 1 // gridwidth
						, 1 // gridheight
						, 1.0 // weightx
						, 1.0 // weighty
						, GridBagConstraints.CENTER // anchor
						, GridBagConstraints.BOTH // fill
						, new Insets(0, 0, 0, 0) // insets
						, 0 // ipadx
						, 0 // ipady
				));
	}

	protected void setOwnerDialog(javax.swing.JDialog ownerDialog) {
		thisDialog = ownerDialog;
	}

	/**
	 * Set the Object to be displayed by panel.
	 * 
	 * @see #setObject(Object, ModelElement) -> call first
	 */
	public final void setObject(java.lang.Object object) throws DeveloperException {
		throw new DeveloperException("call setObject(Object, ModelElement) instead");//$NON-NLS-1$
	}

	/**
	 * Set the Object to be displayed by panel.
	 */
	public void setObject(java.lang.Object object, ch.ehi.uml1_4.foundation.core.ModelElement context) {
		this.context = context;
		// update View
		ParticipantsPanelItem[] classv = (ParticipantsPanelItem[]) object;
		Object[][] data = new Object[classv.length][1];
		for (int i = 0; i < classv.length; i++) {
			data[i][0] = classv[i];
		}
		model.setDataVector(data, new Object[] { ElementUtils.getDisplayName(AbstractClassDef.class) });
	}

	/**
	 * Return the changed object displayed.
	 * 
	 * @returns AbstractClassDef[]
	 */
	public java.lang.Object getObject() {
		// get values from view
		ParticipantsPanelItem[] result = new ParticipantsPanelItem[model.getRowCount()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (ParticipantsPanelItem) model.getValueAt(i, 0);
		}
		return result;
	}

}
