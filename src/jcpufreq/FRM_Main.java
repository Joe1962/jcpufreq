/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcpufreq;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import static jcpufreq.CLS_cpufreq.AutoUnitsFreq;
import static jcpufreq.CLS_cpufreq.CPUCount;
import static jcpufreq.CLS_cpufreq.getPrettyFreqString;
import static jcpufreq.CLS_cpufreq.getScalingAvailableFreqs;
import static jcpufreq.CLS_cpufreq.getScalingAvailableGovernors;
import static jcpufreq.CLS_cpufreq.getScalingCurrFreq;
import static jcpufreq.CLS_cpufreq.getScalingDriver;
import static jcpufreq.CLS_cpufreq.getScalingGovernor;
import static jcpufreq.CLS_cpufreq.getScalingMaxFreq;
import static jcpufreq.CLS_cpufreq.getScalingMinFreq;
import static jcpufreq.CLS_cpufreq.getUnPrettyFreqString;
import static jcpufreq.CLS_cpufreq.setScalingGovernor;
import static jcpufreq.CLS_cpufreq.setScalingMaxFreq;
import static jcpufreq.CLS_cpufreq.setScalingMinFreq;
import static jcpufreq.CLS_cpufreq.setScalingSetSpeed;
import jsoft.jutils.extcomponents.CLS_ListCellRenderer_String_RightAligned;
import tablecellmods.CLS_CellRenderer_String_RightAligned;
import types.TYP_AutoUnitsFreq;

/**
 *
 * @author joe1962
 */
public class FRM_Main extends javax.swing.JFrame {
	private Timer tmrRefresh;

	/**
	 * Creates new form FRM_Main
	 */
	public FRM_Main() {
		initComponents();

		this.setTitle(AppInfo.getTITLE() +  " " + AppInfo.getVERSION() + " (" + AppInfo.getBUILD() + ")");
		this.setLocationRelativeTo(null);

		GLOBAL.isRoot = "root".equals(System.getProperty("user.name"));

		setupScreen();
	}

	private void chkRoot() {
		if (GLOBAL.isRoot) {
			cmbScalingGovernor.setEnabled(true);
			cmbCurrFreq.setEnabled(true);
			cmbScalingMaxFreq.setEnabled(true);
			cmbScalingMinFreq.setEnabled(true);
		} else {
			cmbScalingGovernor.setEnabled(false);
			cmbCurrFreq.setEnabled(false);
			cmbScalingMaxFreq.setEnabled(false);
			cmbScalingMinFreq.setEnabled(false);
//			chkSaveOnExit.setEnabled(false);
//			chkLoadOnStart.setEnabled(false);
//			chkLoadOnBoot.setEnabled(false);
		}
	}

	private void setupScreen() {
		chkRoot();

		lblDriverValue.setText(getScalingDriver(0));

		// Set up possible scaling governors (up to date for kernel 2.6.32.3):
//		GLOBAL.AvailableGovs.add("userspace");
//		GLOBAL.AvailableGovs.add("performance");
//		GLOBAL.AvailableGovs.add("powersave");
//		GLOBAL.AvailableGovs.add("ondemand");
//		GLOBAL.AvailableGovs.add("conservative");
		GLOBAL.AvailableGovs = getScalingAvailableGovernors(0);
		for (Object obj : GLOBAL.AvailableGovs) {
			cmbScalingGovernor.addItem(obj);
		}
		cmbScalingGovernor.setSelectedItem(getScalingGovernor(0));

		// Get number of CPUs:
		GLOBAL.vCPUCount = CPUCount();
		lblNumCPUValue.setText(String.valueOf(GLOBAL.vCPUCount));

		// Fill tblCPUFreq:
		setupTableCPUFreq();
		PopulateTableCPUFreq(GLOBAL.vCPUCount);

		//Check for MultiCPUs handling:
		GLOBAL.vCPUMulti = CLS_cpufreq.CheckMultiCPUs();
		if (GLOBAL.vCPUMulti) {
			lblLinkedCPUs.setText("(independent)");
		} else {
			lblLinkedCPUs.setText("(linked)");
		}

		//Integer.parseInt((String) cmbRefresh.getSelectedItem())
		tmrRefresh = new Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PopulateTableCPUFreq(GLOBAL.vCPUCount);
			}
		});

		tmrRefresh.setRepeats(true);
		tmrRefresh.setCoalesce(true);
		tmrRefresh.setInitialDelay(1000);
		tmrRefresh.setDelay(1000);
		if (!"Manual".equals(cmbRefresh.getSelectedItem())) {
			int MyInterval = getTimerInterval((String) cmbRefresh.getSelectedItem());
			tmrRefresh.setInitialDelay(MyInterval);
			tmrRefresh.setDelay(MyInterval);
			tmrRefresh.start();
		} else {
			tmrRefresh.setInitialDelay(1000);
			tmrRefresh.setDelay(1000);
		}
	}

	private void RefreshFreqs() {
		System.out.println("RefreshFreqs()...");
		GLOBAL.vCPUFreq = getScalingCurrFreq(0);
		GLOBAL.vCPUFreqMaxLimit = getScalingMaxFreq(0);
		lblMaxCPUFreqValue.setText(AutoUnitsFreq(GLOBAL.vCPUFreqMaxLimit, true).getMyFreqFormatted());
		GLOBAL.vCPUFreqMinLimit = getScalingMinFreq(0);
		lblMinCPUFreqValue.setText(AutoUnitsFreq(GLOBAL.vCPUFreqMinLimit, true).getMyFreqFormatted());

		cmbCurrFreq.setRenderer(new CLS_ListCellRenderer_String_RightAligned());
		cmbScalingMaxFreq.setRenderer(new CLS_ListCellRenderer_String_RightAligned());
		cmbScalingMinFreq.setRenderer(new CLS_ListCellRenderer_String_RightAligned());

		cmbCurrFreq.removeAllItems();
		cmbScalingMaxFreq.removeAllItems();
		cmbScalingMinFreq.removeAllItems();
		GLOBAL.AvailableFreqs = getScalingAvailableFreqs(0);
		if (GLOBAL.AvailableFreqs != null && !GLOBAL.AvailableFreqs.isEmpty()) {
			for (Object obj : GLOBAL.AvailableFreqs) {
				cmbCurrFreq.addItem(getPrettyFreqString(Integer.parseInt((String) obj)));
				cmbScalingMaxFreq.addItem(getPrettyFreqString(Integer.parseInt((String) obj)));
				cmbScalingMinFreq.addItem(getPrettyFreqString(Integer.parseInt((String) obj)));
			}
			cmbCurrFreq.setSelectedItem(getPrettyFreqString(getScalingCurrFreq(0)));
			cmbScalingMaxFreq.setSelectedItem(getPrettyFreqString(GLOBAL.vCPUFreqMaxLimit));
			cmbScalingMinFreq.setSelectedItem(getPrettyFreqString(GLOBAL.vCPUFreqMinLimit));
		}
	}

	private void setupTableCPUFreq() {
		tblCPUFreq.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableColumn column; // = new TableColumn();
//		column = tblCPUFreq.getColumns(true).get(colCounter);

		column = tblCPUFreq.getColumn("CPU");
//		column.setHeaderValue("CPU");
		column.setPreferredWidth(45);
		column.setMinWidth(45);
		column.setMaxWidth(45);
//		column = tblCPUFreq.getColumns(true).get(colCounter);

		column = tblCPUFreq.getColumn("Frequency");
//		column.setHeaderValue("Frequency");
		column.setPreferredWidth(60);
		column.setMinWidth(60);
		//column.setMaxWidth(100);
		column.setCellRenderer(new CLS_CellRenderer_String_RightAligned());

		column = tblCPUFreq.getColumn("Unit");
//		column.setHeaderValue("Unit");
		column.setPreferredWidth(45);
		column.setMinWidth(45);
		column.setMaxWidth(45);
	}

	private void PopulateTableCPUFreq(int MyCPUCount) {
		// Load tblMaster with ArrayList:
		DefaultTableModel MyDTM = (DefaultTableModel) tblCPUFreq.getModel();
		MyDTM.setNumRows(0);

		for (int i = 0; i < MyCPUCount; i++) {
			MyDTM.addRow(new Object[2]);
			MyDTM.setValueAt(i + 1, i, 0);
			TYP_AutoUnitsFreq tmpObj = AutoUnitsFreq(getScalingCurrFreq(i), false);
//			MyDTM.setValueAt(Integer.parseInt(AutoUnitsFreq(Integer.parseInt(getScalingCurrFreq(i)))), i, 1);
//			MyDTM.setValueAt(tmpObj.getMyFreq(), i, 1);
			MyDTM.setValueAt(tmpObj.getMyFreqFormatted(), i, 1);
			MyDTM.setValueAt(tmpObj.getMyUnit(), i, 2);
		}

//		tblCPUFreq.packAll();
	}

	private int getTimerInterval(String MyInterval) {
		switch (MyInterval) {
			case "0.25 sec":
				return 250;
			case "0.5 sec":
				return 500;
			case "1 sec":
				return 1000;
			case "2 sec":
				return 2000;
			case "5 sec":
				return 5000;
			case "10 sec":
				return 10000;
			case "15 sec":
				return 15000;
			case "30 sec":
				return 30000;
			case "1 min":
				return 60000;
			default:
				return 1000;
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
      java.awt.GridBagConstraints gridBagConstraints;

      pnlHeader = new javax.swing.JPanel();
      lblLeftLogo = new javax.swing.JLabel();
      pnlTitle = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();
      jLabel2 = new javax.swing.JLabel();
      lblRightLogo = new javax.swing.JLabel();
      jPanel1 = new javax.swing.JPanel();
      jPanel5 = new javax.swing.JPanel();
      lblDriver = new javax.swing.JLabel();
      lblScalingGovernor = new javax.swing.JLabel();
      lblCurrFreq = new javax.swing.JLabel();
      lblMaxCPUFreq = new javax.swing.JLabel();
      lblScalingMaxFreq = new javax.swing.JLabel();
      lblMinCPUFreq = new javax.swing.JLabel();
      lblScalingMinFreq = new javax.swing.JLabel();
      jPanel6 = new javax.swing.JPanel();
      lblDriverValue = new javax.swing.JLabel();
      cmbScalingGovernor = new javax.swing.JComboBox();
      cmbCurrFreq = new javax.swing.JComboBox();
      pnlMaxCPUFreqValue = new javax.swing.JPanel();
      lblMaxCPUFreqValue = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      cmbScalingMaxFreq = new javax.swing.JComboBox();
      pnlMinCPUFreqValue = new javax.swing.JPanel();
      lblMinCPUFreqValue = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      cmbScalingMinFreq = new javax.swing.JComboBox();
      jPanel2 = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      tblCPUFreq = new javax.swing.JTable();
      jPanel3 = new javax.swing.JPanel();
      lblNumCPU = new javax.swing.JLabel();
      lblNumCPUValue = new javax.swing.JLabel();
      lblLinkedCPUs = new javax.swing.JLabel();
      jPanel4 = new javax.swing.JPanel();
      cmbRefresh = new javax.swing.JComboBox();
      butRefresh = new javax.swing.JButton();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      setMinimumSize(new java.awt.Dimension(475, 350));
      setPreferredSize(new java.awt.Dimension(475, 350));
      setResizable(false);
      getContentPane().setLayout(new java.awt.GridBagLayout());

      pnlHeader.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      pnlHeader.setLayout(new java.awt.BorderLayout());

      lblLeftLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/cpu-48.png"))); // NOI18N
      lblLeftLogo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      pnlHeader.add(lblLeftLogo, java.awt.BorderLayout.WEST);

      pnlTitle.setLayout(new java.awt.GridLayout(2, 1));

      jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
      jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel1.setText("JCPUFreq");
      pnlTitle.add(jLabel1);

      jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel2.setText("A Linux cpufreq handler in Java");
      pnlTitle.add(jLabel2);

      pnlHeader.add(pnlTitle, java.awt.BorderLayout.CENTER);

      lblRightLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/javalogo-about-48x48.png"))); // NOI18N
      lblRightLogo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      pnlHeader.add(lblRightLogo, java.awt.BorderLayout.EAST);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 1.0;
      getContentPane().add(pnlHeader, gridBagConstraints);

      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Configuration:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
      java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
      jPanel1Layout.columnWidths = new int[] {0, 5, 0};
      jPanel1Layout.rowHeights = new int[] {0};
      jPanel1.setLayout(jPanel1Layout);

      jPanel5.setLayout(new java.awt.GridLayout(7, 1, 5, 5));

      lblDriver.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblDriver.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblDriver.setText("Driver:");
      lblDriver.setMaximumSize(new java.awt.Dimension(90, 17));
      lblDriver.setMinimumSize(new java.awt.Dimension(90, 17));
      lblDriver.setPreferredSize(new java.awt.Dimension(90, 17));
      jPanel5.add(lblDriver);

      lblScalingGovernor.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblScalingGovernor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblScalingGovernor.setText("Governor:");
      lblScalingGovernor.setMaximumSize(new java.awt.Dimension(90, 17));
      lblScalingGovernor.setMinimumSize(new java.awt.Dimension(90, 17));
      lblScalingGovernor.setPreferredSize(new java.awt.Dimension(90, 17));
      jPanel5.add(lblScalingGovernor);

      lblCurrFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblCurrFreq.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblCurrFreq.setText("Current freq.:");
      lblCurrFreq.setMaximumSize(new java.awt.Dimension(85, 17));
      lblCurrFreq.setMinimumSize(new java.awt.Dimension(85, 17));
      lblCurrFreq.setPreferredSize(new java.awt.Dimension(85, 17));
      jPanel5.add(lblCurrFreq);

      lblMaxCPUFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblMaxCPUFreq.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblMaxCPUFreq.setText("Max CPU freq:");
      lblMaxCPUFreq.setMaximumSize(new java.awt.Dimension(90, 17));
      lblMaxCPUFreq.setMinimumSize(new java.awt.Dimension(90, 17));
      lblMaxCPUFreq.setPreferredSize(new java.awt.Dimension(90, 17));
      jPanel5.add(lblMaxCPUFreq);

      lblScalingMaxFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblScalingMaxFreq.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblScalingMaxFreq.setText("Max freq. limit:");
      lblScalingMaxFreq.setMaximumSize(new java.awt.Dimension(90, 17));
      lblScalingMaxFreq.setMinimumSize(new java.awt.Dimension(90, 17));
      lblScalingMaxFreq.setPreferredSize(new java.awt.Dimension(90, 17));
      jPanel5.add(lblScalingMaxFreq);

      lblMinCPUFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblMinCPUFreq.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblMinCPUFreq.setText("Min CPU freq:");
      lblMinCPUFreq.setMaximumSize(new java.awt.Dimension(90, 17));
      lblMinCPUFreq.setMinimumSize(new java.awt.Dimension(90, 17));
      lblMinCPUFreq.setPreferredSize(new java.awt.Dimension(90, 17));
      jPanel5.add(lblMinCPUFreq);

      lblScalingMinFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblScalingMinFreq.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblScalingMinFreq.setText("Min freq. limit:");
      lblScalingMinFreq.setMaximumSize(new java.awt.Dimension(90, 17));
      lblScalingMinFreq.setMinimumSize(new java.awt.Dimension(90, 17));
      lblScalingMinFreq.setPreferredSize(new java.awt.Dimension(90, 17));
      jPanel5.add(lblScalingMinFreq);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      jPanel1.add(jPanel5, gridBagConstraints);

      jPanel6.setLayout(new java.awt.GridLayout(7, 1, 5, 5));

      lblDriverValue.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblDriverValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      jPanel6.add(lblDriverValue);

      cmbScalingGovernor.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      cmbScalingGovernor.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            cmbScalingGovernorItemStateChanged(evt);
         }
      });
      jPanel6.add(cmbScalingGovernor);

      cmbCurrFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      cmbCurrFreq.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            cmbCurrFreqItemStateChanged(evt);
         }
      });
      jPanel6.add(cmbCurrFreq);

      pnlMaxCPUFreqValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      java.awt.GridBagLayout pnlMaxCPUFreqValueLayout = new java.awt.GridBagLayout();
      pnlMaxCPUFreqValueLayout.columnWidths = new int[] {0, 5, 0};
      pnlMaxCPUFreqValueLayout.rowHeights = new int[] {0};
      pnlMaxCPUFreqValue.setLayout(pnlMaxCPUFreqValueLayout);

      lblMaxCPUFreqValue.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblMaxCPUFreqValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      pnlMaxCPUFreqValue.add(lblMaxCPUFreqValue, gridBagConstraints);

      jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      jLabel3.setText("Hz");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.weighty = 1.0;
      pnlMaxCPUFreqValue.add(jLabel3, gridBagConstraints);

      jPanel6.add(pnlMaxCPUFreqValue);

      cmbScalingMaxFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      cmbScalingMaxFreq.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            cmbScalingMaxFreqItemStateChanged(evt);
         }
      });
      jPanel6.add(cmbScalingMaxFreq);

      pnlMinCPUFreqValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      java.awt.GridBagLayout pnlMinCPUFreqValueLayout = new java.awt.GridBagLayout();
      pnlMinCPUFreqValueLayout.columnWidths = new int[] {0, 5, 0};
      pnlMinCPUFreqValueLayout.rowHeights = new int[] {0};
      pnlMinCPUFreqValue.setLayout(pnlMinCPUFreqValueLayout);

      lblMinCPUFreqValue.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblMinCPUFreqValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      pnlMinCPUFreqValue.add(lblMinCPUFreqValue, gridBagConstraints);

      jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      jLabel4.setText("Hz");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.weighty = 1.0;
      pnlMinCPUFreqValue.add(jLabel4, gridBagConstraints);

      jPanel6.add(pnlMinCPUFreqValue);

      cmbScalingMinFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      cmbScalingMinFreq.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            cmbScalingMinFreqItemStateChanged(evt);
         }
      });
      jPanel6.add(cmbScalingMinFreq);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      jPanel1.add(jPanel6, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      getContentPane().add(jPanel1, gridBagConstraints);

      jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current frequency:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
      jPanel2.setMinimumSize(new java.awt.Dimension(120, 66));
      jPanel2.setPreferredSize(new java.awt.Dimension(120, 66));
      jPanel2.setLayout(new java.awt.GridLayout(1, 1));

      jScrollPane1.setMinimumSize(null);

      tblCPUFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      tblCPUFreq.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {

         },
         new String [] {
            "CPU", "Frequency", "Unit"
         }
      ) {
         Class[] types = new Class [] {
            java.lang.Integer.class, java.lang.Double.class, java.lang.String.class
         };
         boolean[] canEdit = new boolean [] {
            false, false, false
         };

         public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
         }

         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
         }
      });
      tblCPUFreq.setFillsViewportHeight(true);
      tblCPUFreq.setMinimumSize(null);
      tblCPUFreq.setPreferredSize(null);
      jScrollPane1.setViewportView(tblCPUFreq);
      if (tblCPUFreq.getColumnModel().getColumnCount() > 0) {
         tblCPUFreq.getColumnModel().getColumn(0).setResizable(false);
         tblCPUFreq.getColumnModel().getColumn(1).setResizable(false);
         tblCPUFreq.getColumnModel().getColumn(2).setResizable(false);
      }

      jPanel2.add(jScrollPane1);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
      gridBagConstraints.weightx = 0.75;
      gridBagConstraints.weighty = 1.0;
      getContentPane().add(jPanel2, gridBagConstraints);

      jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CPU settings:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
      java.awt.GridBagLayout jPanel3Layout = new java.awt.GridBagLayout();
      jPanel3Layout.columnWidths = new int[] {0, 5, 0, 5, 0};
      jPanel3Layout.rowHeights = new int[] {0};
      jPanel3.setLayout(jPanel3Layout);

      lblNumCPU.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblNumCPU.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblNumCPU.setText("# CPUs:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weighty = 1.0;
      jPanel3.add(lblNumCPU, gridBagConstraints);

      lblNumCPUValue.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblNumCPUValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      lblNumCPUValue.setToolTipText("Number of active CPUs");
      lblNumCPUValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      jPanel3.add(lblNumCPUValue, gridBagConstraints);

      lblLinkedCPUs.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      lblLinkedCPUs.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 4;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      jPanel3.add(lblLinkedCPUs, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      getContentPane().add(jPanel3, gridBagConstraints);

      jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Refresh view:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
      jPanel4.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

      cmbRefresh.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Manual", "0.25 sec", "0.5 sec", "1 sec", "2 sec", "5 sec", "10 sec", "15 sec", "30 sec", "1 min" }));
      cmbRefresh.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            cmbRefreshItemStateChanged(evt);
         }
      });
      jPanel4.add(cmbRefresh);

      butRefresh.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
      butRefresh.setText("Refresh");
      butRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            butRefreshMouseClicked(evt);
         }
      });
      jPanel4.add(butRefresh);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.weightx = 0.75;
      getContentPane().add(jPanel4, gridBagConstraints);

      pack();
   }// </editor-fold>//GEN-END:initComponents

   private void cmbScalingGovernorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbScalingGovernorItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			switch (cmbScalingGovernor.getSelectedItem().toString()) {
				case "userspace":
					cmbCurrFreq.setEnabled(true);
					cmbScalingMaxFreq.setEnabled(false);
					cmbScalingMinFreq.setEnabled(false);
					break;
				case "performance":
					cmbCurrFreq.setEnabled(false);
					cmbScalingMaxFreq.setEnabled(false);
					cmbScalingMinFreq.setEnabled(false);
					break;
				case "powersave":
					cmbCurrFreq.setEnabled(false);
					cmbScalingMaxFreq.setEnabled(false);
					cmbScalingMinFreq.setEnabled(false);
					break;
				case "ondemand":
					cmbCurrFreq.setEnabled(false);
					cmbScalingMaxFreq.setEnabled(true);
					cmbScalingMinFreq.setEnabled(true);
					break;
				case "conservative":
					cmbCurrFreq.setEnabled(false);
					cmbScalingMaxFreq.setEnabled(true);
					cmbScalingMinFreq.setEnabled(true);
					break;
				default:
					break;
			}
			for (int i = 0; i < GLOBAL.vCPUCount; i++) {
				setScalingGovernor(i, cmbScalingGovernor.getSelectedItem().toString());
			}
			// Refresh frequency info:
			RefreshFreqs();
		}
   }//GEN-LAST:event_cmbScalingGovernorItemStateChanged

   private void butRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_butRefreshMouseClicked
		PopulateTableCPUFreq(GLOBAL.vCPUCount);
   }//GEN-LAST:event_butRefreshMouseClicked

   private void cmbCurrFreqItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbCurrFreqItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			for (int i = 0; i < GLOBAL.vCPUCount; i++) {
				boolean retBool = setScalingSetSpeed(i, getUnPrettyFreqString((String)cmbCurrFreq.getSelectedItem()));
			}
		}
   }//GEN-LAST:event_cmbCurrFreqItemStateChanged

   private void cmbScalingMaxFreqItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbScalingMaxFreqItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			for (int i = 0; i < GLOBAL.vCPUCount; i++) {
				boolean retBool = setScalingMaxFreq(i, getUnPrettyFreqString((String)cmbScalingMaxFreq.getSelectedItem()));
			}
		}
   }//GEN-LAST:event_cmbScalingMaxFreqItemStateChanged

   private void cmbScalingMinFreqItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbScalingMinFreqItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			for (int i = 0; i < GLOBAL.vCPUCount; i++) {
				boolean retBool = setScalingMinFreq(i, getUnPrettyFreqString((String)cmbScalingMinFreq.getSelectedItem()));
			}
		}
   }//GEN-LAST:event_cmbScalingMinFreqItemStateChanged

   private void cmbRefreshItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbRefreshItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			if ("Manual".equals(cmbRefresh.getSelectedItem())) {
				tmrRefresh.stop();
				butRefresh.setEnabled(true);
			} else {
				butRefresh.setEnabled(false);
				int MyInterval = getTimerInterval((String) cmbRefresh.getSelectedItem());
				tmrRefresh.setInitialDelay(MyInterval);
				tmrRefresh.setDelay(MyInterval);
				tmrRefresh.restart();
			}
		}
   }//GEN-LAST:event_cmbRefreshItemStateChanged




	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Metal".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(FRM_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(FRM_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(FRM_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(FRM_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
        //</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new FRM_Main().setVisible(true);
			}
		});
	}

	// <editor-fold defaultstate="collapsed" desc="Variables declaration - do not modify">
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton butRefresh;
   private javax.swing.JComboBox cmbCurrFreq;
   private javax.swing.JComboBox cmbRefresh;
   private javax.swing.JComboBox cmbScalingGovernor;
   private javax.swing.JComboBox cmbScalingMaxFreq;
   private javax.swing.JComboBox cmbScalingMinFreq;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JPanel jPanel4;
   private javax.swing.JPanel jPanel5;
   private javax.swing.JPanel jPanel6;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JLabel lblCurrFreq;
   private javax.swing.JLabel lblDriver;
   private javax.swing.JLabel lblDriverValue;
   private javax.swing.JLabel lblLeftLogo;
   private javax.swing.JLabel lblLinkedCPUs;
   private javax.swing.JLabel lblMaxCPUFreq;
   private javax.swing.JLabel lblMaxCPUFreqValue;
   private javax.swing.JLabel lblMinCPUFreq;
   private javax.swing.JLabel lblMinCPUFreqValue;
   private javax.swing.JLabel lblNumCPU;
   private javax.swing.JLabel lblNumCPUValue;
   private javax.swing.JLabel lblRightLogo;
   private javax.swing.JLabel lblScalingGovernor;
   private javax.swing.JLabel lblScalingMaxFreq;
   private javax.swing.JLabel lblScalingMinFreq;
   private javax.swing.JPanel pnlHeader;
   private javax.swing.JPanel pnlMaxCPUFreqValue;
   private javax.swing.JPanel pnlMinCPUFreqValue;
   private javax.swing.JPanel pnlTitle;
   private javax.swing.JTable tblCPUFreq;
   // End of variables declaration//GEN-END:variables
	// </editor-fold>

}
