package telkom.eai.env.tools;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JTextField;
import javax.swing.JProgressBar;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.swing.JFileChooser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.JCheckBox;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

public class SearchLogsFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField txtSearchKey;
	
	private LogFinder logfinder;
	private JTextArea txtOutput;
	private JPanel pnlOutput;
	private JComboBox<Object> cmbEnvironment;
	private JComboBox cmbSearchBy;
	private JList<String> fileList;
	private JSpinner spnDate;
	private JCheckBox chkAddDate;
	private HashMap<String, String> fileMap;
	private HashMap<String, String> loadedLogFiles;
	private JScrollPane jsp;
	private String vSearchKey;
	SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	public SearchLogsFrame(Properties props) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			MiscUtils.printEx(e);
		}
		
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setVgap(4);
		borderLayout.setHgap(2);
		fileMap = new HashMap<String, String>();
		loadedLogFiles = new HashMap<String,String>();
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Search Logs");
		
		logfinder = LogFinder.getInstance(props);
		
		JPanel pnlEnvironment = new JPanel();
		getContentPane().add(pnlEnvironment, BorderLayout.NORTH);
		GridBagLayout gbl_pnlEnvironment = new GridBagLayout();
		gbl_pnlEnvironment.columnWidths = new int[] {107, 250, 89, 149, 0};
		gbl_pnlEnvironment.rowHeights = new int[]{22, 20, 23, 0};
		gbl_pnlEnvironment.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlEnvironment.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlEnvironment.setLayout(gbl_pnlEnvironment);
		
		String keys [] = new String[logfinder.getENVs().keySet().size()];
		logfinder.getENVs().keySet().toArray(keys);	
		
		java.util.List<String> envlist =Arrays.asList(keys);
		Collections.sort(envlist);
		DefaultComboBoxModel<Object> envModel = new DefaultComboBoxModel<Object>(envlist.toArray());
		
		JLabel lblNewLabel = new JLabel("Environment:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		pnlEnvironment.add(lblNewLabel, gbc_lblNewLabel);
		
		final JButton btnSearch = new JButton("Search");
		btnSearch.addActionListener(new SearchEvent());
		
		chkAddDate = new JCheckBox("Transaction Date");
		chkAddDate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chkAddDate.isSelected()) {
					spnDate.setEnabled(true);
				}
				else {
					spnDate.setEnabled(false);
				}
			}
		});
		
		txtSearchKey = new JTextField();
		txtSearchKey.addActionListener(new SearchEvent());
		
		
		cmbEnvironment = new JComboBox<Object>();	
		cmbEnvironment.setModel(envModel);
		GridBagConstraints gbc_cmbEnvironment = new GridBagConstraints();
		gbc_cmbEnvironment.fill = GridBagConstraints.BOTH;
		gbc_cmbEnvironment.insets = new Insets(0, 0, 5, 5);
		gbc_cmbEnvironment.gridx = 1;
		gbc_cmbEnvironment.gridy = 0;
		pnlEnvironment.add(cmbEnvironment, gbc_cmbEnvironment);
		
		JLabel lblNewLabel_4 = new JLabel("Search key");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 1;
		pnlEnvironment.add(lblNewLabel_4, gbc_lblNewLabel_4);
		GridBagConstraints gbc_txtSearchKey = new GridBagConstraints();
		gbc_txtSearchKey.fill = GridBagConstraints.BOTH;
		gbc_txtSearchKey.insets = new Insets(0, 0, 5, 5);
		gbc_txtSearchKey.gridx = 1;
		gbc_txtSearchKey.gridy = 1;
		pnlEnvironment.add(txtSearchKey, gbc_txtSearchKey);
		txtSearchKey.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Search by");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 1;
		pnlEnvironment.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		cmbSearchBy = new JComboBox();
		cmbSearchBy.setModel(new DefaultComboBoxModel(new String[] {"MessageId", "CorrelationId", "InternalMessageId", "OrderActionID"}));
		GridBagConstraints gbc_cmbSearchBy = new GridBagConstraints();
		gbc_cmbSearchBy.fill = GridBagConstraints.BOTH;
		gbc_cmbSearchBy.insets = new Insets(0, 0, 5, 0);
		gbc_cmbSearchBy.gridx = 3;
		gbc_cmbSearchBy.gridy = 1;
		pnlEnvironment.add(cmbSearchBy, gbc_cmbSearchBy);
		GridBagConstraints gbc_chkAddDate = new GridBagConstraints();
		gbc_chkAddDate.anchor = GridBagConstraints.WEST;
		gbc_chkAddDate.insets = new Insets(0, 0, 0, 5);
		gbc_chkAddDate.gridx = 0;
		gbc_chkAddDate.gridy = 2;
		pnlEnvironment.add(chkAddDate, gbc_chkAddDate);
		
				spnDate = new JSpinner();
				spnDate.setModel(new SpinnerDateModel());
								
				spnDate.setEditor(new JSpinner.DateEditor(spnDate, "yyyy-MM-dd"));
				spnDate.setEnabled(false);
				GridBagConstraints gbc_spnDate = new GridBagConstraints();
				gbc_spnDate.fill = GridBagConstraints.BOTH;
				gbc_spnDate.insets = new Insets(0, 0, 0, 5);
				gbc_spnDate.gridx = 1;
				gbc_spnDate.gridy = 2;
				pnlEnvironment.add(spnDate, gbc_spnDate);
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.fill = GridBagConstraints.BOTH;
		gbc_btnSearch.insets = new Insets(0, 0, 0, 5);
		gbc_btnSearch.gridx = 2;
		gbc_btnSearch.gridy = 2;
		pnlEnvironment.add(btnSearch, gbc_btnSearch);
		
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				if (fileChooser.showSaveDialog(SearchLogsFrame.this) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					FileWriter writer = null;
					try {
						writer = new FileWriter(file);
						writer.write(txtOutput.getText());
						writer.close();
					} catch (Exception e) {
						MiscUtils.printEx(e);
						JOptionPane.showMessageDialog(null, "Save failed!\n"+e.getMessage(), "Error: ", JOptionPane.INFORMATION_MESSAGE);
					}
					finally {
						if(writer != null) {
							try {
								writer.close();
							} catch (IOException e1) {
								MiscUtils.printEx(e1);
							}
						}
					}
				}
			}
		});
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.BOTH;
		gbc_btnSave.gridx = 3;
		gbc_btnSave.gridy = 2;
		pnlEnvironment.add(btnSave, gbc_btnSave);
		SpinnerDateModel spnDateModel = new SpinnerDateModel();
		
		pnlOutput = new JPanel();
		getContentPane().add(pnlOutput, BorderLayout.CENTER);
		pnlOutput.setLayout(new BorderLayout(0, 0));
		
		txtOutput = new JTextArea();
		txtOutput.setForeground(Color.WHITE);
		txtOutput.setBackground(Color.DARK_GRAY);
		
		jsp = new JScrollPane(txtOutput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ); 
		
		pnlOutput.add(jsp, BorderLayout.CENTER);
		
		
		fileList = new JList();
		fileList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {	
				
				SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){

					@Override
			         protected Void doInBackground() throws Exception {
			        	 	
							String filename = fileList.getSelectedValue();	
							String absFilePath = fileMap.get(filename);
							String env = cmbEnvironment.getSelectedItem().toString();	
							String output = "";
							long start=0;
							long end=0;
							
							if(absFilePath == null)
								return null;
							
							start = System.currentTimeMillis();
							if(loadedLogFiles.containsKey(filename)) {
								output = loadedLogFiles.get(filename)+"";
							}
							else {
								//output = logfinder.fetchLogsInFilesBySearchKey(env,absFilePath, txtSearchKey.getText());
								
								output = logfinder.extractLogs(env, absFilePath, vSearchKey);						
						        
								loadedLogFiles.put(filename, output);
							}
							end = System.currentTimeMillis();
							String strExecTime = "**Execution time: "+(end-start)/1000+" seconds**\n\n";
							
							txtOutput.setText(strExecTime+output);	
							jsp.getVerticalScrollBar().setValue(0);
			            return null;
			         }
			      };

			      Window win = SwingUtilities.getWindowAncestor((JList)arg0.getSource());
			      final JDialog dialog = new JDialog(win, "Dialog", ModalityType.APPLICATION_MODAL);

			      mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {


			         public void propertyChange(PropertyChangeEvent evt) {
			            if (evt.getPropertyName().equals("state")) {
			               if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
			                  dialog.dispose();
			               }
			            }
			         }
			      });
			      mySwingWorker.execute();

			      JProgressBar progressBar = new JProgressBar();
			      progressBar.setIndeterminate(true);
			      JPanel panel = new JPanel(new BorderLayout());
			      panel.add(progressBar, BorderLayout.CENTER);
			      panel.add(new JLabel("Please wait......."), BorderLayout.PAGE_START);
			      dialog.getContentPane().add(panel);
			      dialog.pack();
			      dialog.setLocationRelativeTo(win);
			      dialog.setVisible(true);
				
			}
		});
		fileList.setSize(new Dimension(30, 10));
		
		fileList.setModel(new DefaultListModel());
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pnlOutput.add(fileList, BorderLayout.WEST);
	}
	
	public static boolean isSameDay(Date date1, Date date2) {
	    LocalDate localDate1 = date1.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDate();
	    LocalDate localDate2 = date2.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDate();
	    return localDate1.isEqual(localDate2);
	}
	
	public void clearResults() {
		this.txtOutput.setText("");
	}
	
	public static boolean isValidDate(String strDate)
	   {
		/* Check if date is 'null' */
		if (strDate.trim().equals(""))
		{
		    return false;
		}
		/* Date is not 'null' */
		else
		{
		    /*
		     * Set preferred date format,
		     * For example MM-dd-yyyy, MM.dd.yyyy,dd.MM.yyyy etc.*/
		    SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
		    sdfrmt.setLenient(false);
		    /* Create Date object
		     * parse the string into date 
	             */
		    try
		    {
		        Date javaDate = sdfrmt.parse(strDate); 
		        System.out.println(strDate+" is valid date format");
		    }
		    /* Date format is invalid */
		    catch (ParseException e)
		    {
		        System.out.println(strDate+" is Invalid Date format");
		        MiscUtils.printEx(e);
		        return false;
		    }
		    /* Return true if date format is valid */
		    return true;
		}
	   }
	
	class SearchEvent implements ActionListener{

	   public void actionPerformed(ActionEvent evt) {
	      SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
	         @Override
	         protected Void doInBackground() throws Exception {

	        	 	DefaultListModel<String> lmodel = new DefaultListModel<String>();							

					String filePattern = "\\*.log"; 
					String [] filePatternList=null;
					
					String selectedDate = dateFormatter.format(spnDate.getValue());
					Date today = new Date();	
					
					vSearchKey = txtSearchKey.getText();
					String env = cmbEnvironment.getSelectedItem().toString();
					String searchBy = cmbSearchBy.getSelectedItem().toString();
					
					if(chkAddDate.isSelected() && !isValidDate(selectedDate)) {
						JOptionPane.showMessageDialog(null, "Invalid date. Required format is: yyyy-MM-dd", "Error: ", JOptionPane.INFORMATION_MESSAGE);
						return null;
					}
					if(env.startsWith("EIESB_")) {
						filePatternList = new String[] {filePattern,"\\*.log.\\*"};
					}
					else if(chkAddDate.isSelected() && !isSameDay(today, (Date)spnDate.getValue())) {
						filePatternList = new String[] {filePattern+"."+selectedDate+"*"};
						
					}
					else {
						filePatternList = new String[] {filePattern,"\\*.log."+dateFormatter.format(new Date())+"\\*"};
					}
					
					
					
					if(!(vSearchKey != null && !"".equals(vSearchKey.trim()))) {
					
						JOptionPane.showMessageDialog(null, "Search key is empty!", "Error: ", JOptionPane.INFORMATION_MESSAGE);
						return null;
					}
					
					long start = System.currentTimeMillis();
					String filieList = logfinder.getMatchinFileList(vSearchKey, searchBy, env, filePatternList);
					long end = System.currentTimeMillis();
					String strExecTime = "**Execution time: "+(end-start)/1000+" seconds**\n\n";
					
					String lines[] = filieList.trim().split("\\r?\\n");	
					
					HashMap<String,String> clientList = new HashMap<String,String>();
					HashMap<String,String> compList = new HashMap<String,String>();
					HashMap<String,String> serviceList = new HashMap<String,String>();
					HashMap<String,String> esbList = new HashMap<String, String>();
					
					fileMap.clear();
					loadedLogFiles.clear();
					for(String line : lines) {
						
						int numTok = line.split("/").length;
						
						if(numTok<=1)
							continue;					
						String fileKey = line.split("/")[numTok-1];
						
						if(fileKey.indexOf("Client_") >= 0 && !clientList.containsKey(fileKey))
							clientList.put(fileKey,line);
						else if(fileKey.indexOf("Comp_") >= 0 && !compList.containsKey(fileKey))
							compList.put(fileKey,line);
						else if(fileKey.indexOf("Service_") >= 0 && !serviceList.containsKey(fileKey))
							serviceList.put(fileKey,line);
						else if(env.startsWith("EIESB_"))
							esbList.put(fileKey, line);
					}
					
					for (Object key: clientList.keySet()) {
						lmodel.addElement(key.toString());
						fileMap.put(key.toString(),clientList.get(key));
					}
					
					for (Object key: compList.keySet()) {
						lmodel.addElement(key.toString());
						fileMap.put(key.toString(),compList.get(key));
					}
					
					for (Object key: serviceList.keySet()) {
						lmodel.addElement(key.toString());
						fileMap.put(key.toString(),serviceList.get(key));
					}
					
					for (Object key: esbList.keySet()) {
						lmodel.addElement(key.toString());
						fileMap.put(key.toString(),esbList.get(key));
					}
					
					fileList.setModel(lmodel);
					/*String connResult = logfinder.testAccess(cmbEnvironment.getSelectedItem().toString());
					
					if("".equals(connResult) || !"success".equals(connResult.trim())) {
						txtOutput.setText("Connection failed:"+cmbEnvironment.getSelectedItem()+"\n"+connResult);	
						return null;
					}*/
					
					if(lmodel.getSize()<1 || lmodel.getSize()==0)
						txtOutput.setText(strExecTime+"No match found for file patterns:   "+Arrays.asList(filePatternList));
					else
						txtOutput.setText(strExecTime+lmodel.getSize()+" file(s) matched");
	            return null;
	         }
	      };

	      Window win = SwingUtilities.getWindowAncestor((Component) evt.getSource());
	      final JDialog dialog = new JDialog(win, "Dialog", ModalityType.APPLICATION_MODAL);

	      mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("state")) {
		               if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
		                  dialog.dispose();
		               }
		            }
				
			}
	      });
	      mySwingWorker.execute();

	      JProgressBar progressBar = new JProgressBar();
	      progressBar.setIndeterminate(true);
	      JPanel panel = new JPanel(new BorderLayout());
	      panel.add(progressBar, BorderLayout.CENTER);
	      panel.add(new JLabel("Please wait......."), BorderLayout.PAGE_START);
	      dialog.getContentPane().add(panel);
	      dialog.pack();
	      dialog.setLocationRelativeTo(win);
	      dialog.setVisible(true);
	   }	
	}	
}