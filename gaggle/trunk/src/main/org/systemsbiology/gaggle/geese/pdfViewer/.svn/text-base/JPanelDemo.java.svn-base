/**
 * Created on 04-Mar-2005
 *
 * Simple demo to show JPedal being used as a simple GUI viewer.
 * 
 * represents the minimum needed to display a PDF page.
 * 
 */
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.pdfViewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.utils.FileFilterer;

/**
 * Scope:<b>(All)</b>
 * <p>Description:A 'barebones' example of implementing a PDF display panel.
 * The SimpleViewer example shows all the bells and whistles
 * <P>
 */
public class JPanelDemo extends JFrame {
	
	private String viewerTitle="Jpanel Demo";
	
	/**the actual JPanel/decoder object*/
	private PdfDecoder pdfDecoder;
	
	/**name of current PDF file*/
	private String currentFile=null;
	
	/**current page number (first page is 1)*/
	private int currentPage=1;
	
	private final JLabel pageCounter1=new JLabel("Page ");
	private JTextField pageCounter2=new JTextField(4);//000 used to set prefered size
	private JLabel pageCounter3=new JLabel("of");//000 used to set prefered size
	
	/**
	 * construct a pdf viewer, passing in the full file name
	 */
	public JPanelDemo(String name){
		
		pdfDecoder = new PdfDecoder();
		
		currentFile = name;//store file name for use in page changer
		
		try{
			//this opens the PDF and reads its internal details
			pdfDecoder.openPdfFile(currentFile);
			
			//these 2 lines opens page 1 at 100% scaling
			pdfDecoder.decodePage(currentPage);
			pdfDecoder.setPageParameters(1,1); //values scaling (1=100%). page number
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//setup our GUI display
		initializeViewer();
		
		//set page number display
		pageCounter2.setText(currentPage+"");
		pageCounter3.setText("of "+pdfDecoder.getPageCount());
	}
	
	/**
	 * construct an empty pdf viewer and pop up the open window
	 */
	public JPanelDemo(){
		
		setTitle(viewerTitle);
		
		pdfDecoder = new PdfDecoder();
		
		initializeViewer();
		
//		selectFile();
	}

	/**
	 * opens a chooser and allows user to select a pdf file and opens it
	 */
	private void selectFile() {
		
		JFileChooser open = new JFileChooser(".");
		open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		String[] pdf = new String[] { "pdf" };
		open.addChoosableFileFilter(new FileFilterer(pdf,"Pdf (*.pdf)"));
		
		int resultOfFileSelect = JFileChooser.ERROR_OPTION;
		while(resultOfFileSelect==JFileChooser.ERROR_OPTION){
			
			resultOfFileSelect = open.showOpenDialog(this);
			
			if(resultOfFileSelect==JFileChooser.ERROR_OPTION)
				System.err.println("JFileChooser error");
			
			if(resultOfFileSelect==JFileChooser.APPROVE_OPTION){
				currentFile = open.getSelectedFile().getAbsolutePath();
				
				currentPage = 1;
				try{
					//close the current pdf before opening another
					pdfDecoder.closePdfFile();
					
//					this opens the PDF and reads its internal details
					pdfDecoder.openPdfFile(currentFile);
					
					//check for password encription and acertain
					if(!checkEncryption()){
						//if file content is not accessable make user select a different file
						resultOfFileSelect = JFileChooser.CANCEL_OPTION;
					}
					
//					these 2 lines opens page 1 at 100% scaling
					pdfDecoder.decodePage(currentPage);
					pdfDecoder.setPageParameters(1,1); //values scaling (1=100%). page number
					pdfDecoder.invalidate();
					
				}catch(Exception e){
					e.printStackTrace();
				}
				
				//set page number display
				pageCounter2.setText(currentPage+"");
				pageCounter3.setText("of "+pdfDecoder.getPageCount());
				
				setTitle(viewerTitle+" - "+currentFile);
				
				repaint();
			}
		}
	}

	/**
	 * check if encryption present and acertain password, return true if content accessable
	 */
	private boolean checkEncryption() {
		
//		check if file is encrypted
		if(pdfDecoder.isEncrypted()){
			
			//if file has a null password it will have been decoded and isFileViewable will return true
			while(!pdfDecoder.isFileViewable()) {
	
				/**<start-13>
				//<end-13>
				JOptionPane.showMessageDialog(this,"Please use Java 1.4 to display encrypted files");
				return false;//no other content can be displayed from this file
				//<start-13>
				 /**/
	
				/** popup window if password needed */
				String password = JOptionPane.showInputDialog(this,"Please enter password");
	
				/** try and reopen with new password */
				if (password != null) {
					pdfDecoder.setEncryptionPassword(password);
					pdfDecoder.verifyAccess();
	
				}
				//<end-13>
			}
			return true;
		}
		//if not encrypted return true
		return true;
	}

	/**
	 * setup the viewer and its components
	 */
	private void initializeViewer() {
		//<start-13>
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		//<end-13>
		
		Container cPane = getContentPane();
		cPane.setLayout(new BorderLayout());
		
		JButton open = initOpenBut();//setup open button
		JPanel pageChanger = initChangerPanel();//setup page display and changer
		
		JToolBar topBar = new JToolBar();
		topBar.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
		topBar.add(open);
		topBar.add(pageChanger);
		
		cPane.add(topBar,BorderLayout.NORTH);
		
		JScrollPane display = initPDFDisplay();//setup scrollpane with pdf display inside
		cPane.add(display,BorderLayout.CENTER);
		
		pack();
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screen.width/2,screen.height/2);
		
		//<start-13>
		setLocationRelativeTo(null);//centre on screen
		//<end-13>
		setVisible(true);
	}
	
	/**
	 * returns the open button with listener
	 */
	private JButton initOpenBut() {
		
		JButton open = new JButton();
		open.setIcon(new ImageIcon(ClassLoader.getSystemResource("org/jpedal/examples/simpleviewer/open.gif"))); //$NON-NLS-1$
		open.setText("Open");
		open.setToolTipText("Open a file"); 
		open.setBorderPainted(false);
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				    selectFile();
			}
		});
		
		return open;
	}

	/**
	 * returns the scrollpane with pdfDecoder set as the viewport
	 */
	private JScrollPane initPDFDisplay() {
		
		JScrollPane currentScroll = new JScrollPane();
		currentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		currentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		currentScroll.setViewportView(pdfDecoder);
		
		return currentScroll;
	}

	/**
	 * setup the page display and changer panel and return it 
	 */
	private JPanel initChangerPanel(){
		
		JPanel currentBar1 = new JPanel();
		currentBar1.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
		
		/**back to page 1*/
		JButton start = new JButton();
		start.setBorderPainted(false);
		URL startImage =ClassLoader.getSystemResource("org/jpedal/examples/simpleviewer/start.gif"); 
		start.setIcon(new ImageIcon(startImage));
		start.setToolTipText("Rewind to page 1");
		currentBar1.add(start);
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    if(currentFile!=null && currentPage!=1){
			    	currentPage = 1;
			    	try {
						pdfDecoder.decodePage(currentPage);
						pdfDecoder.invalidate();
						repaint();
					} catch (Exception e1) {
						System.err.println("back to page 1");
						e1.printStackTrace();
					}
			    	
			    	//set page number display
					pageCounter2.setText(currentPage+"");
			    }
			}
		});
		
		/**back 10 icon*/
		JButton fback = new JButton();
		fback.setBorderPainted(false);
		URL fbackImage =ClassLoader.getSystemResource("org/jpedal/examples/simpleviewer/fback.gif");
		fback.setIcon(new ImageIcon(fbackImage));
		fback.setToolTipText("Rewind 10 pages");
		currentBar1.add(fback);
		fback.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(currentFile!=null && currentPage>10){
					currentPage -= 10;
			    	try {
						pdfDecoder.decodePage(currentPage);
						pdfDecoder.invalidate();
						repaint();
					} catch (Exception e1) {
						System.err.println("back 10 pages");
						e1.printStackTrace();
					}
			    	
//			    	set page number display
					pageCounter2.setText(currentPage+"");
				}
			}
		});
		
		/**back icon*/
		JButton back = new JButton();
		back.setBorderPainted(false);
		URL backImage =ClassLoader.getSystemResource("org/jpedal/examples/simpleviewer/back.gif"); 
		back.setIcon(new ImageIcon(backImage));
		back.setToolTipText("Rewind one page"); 
		currentBar1.add(back);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			if(currentFile!=null && currentPage>1){
				currentPage -= 1;
		    	try {
					pdfDecoder.decodePage(currentPage);
					pdfDecoder.invalidate();
					repaint();
				} catch (Exception e1) {
					System.err.println("back 1 page");
					e1.printStackTrace();
				}
		    	
//		    	set page number display
				pageCounter2.setText(currentPage+"");
			}
			}
		});
		
		pageCounter2.setEditable(true);
		pageCounter2.addActionListener(new ActionListener(){
		    
		    public void actionPerformed(ActionEvent a) {
		        
		        String value=(String) pageCounter2.getText().trim();
		        int newPage;
		        
		        //allow for bum values
		        try{
		            newPage=Integer.parseInt(value);
		            
		            if((newPage>pdfDecoder.getPageCount())|(newPage<1)){
		            	return;
		            }
		            
		            currentPage=newPage;
		            try{
		            	pdfDecoder.decodePage(currentPage);
		            	pdfDecoder.invalidate();
						repaint();
		            }catch(Exception e){
		            	System.err.println("page number entered");
		            	e.printStackTrace();
		            }
		            
		        }catch(Exception e){
		            JOptionPane.showMessageDialog(null,">"+value+ "< is Not a valid Value.\nPlease enter a number between 1 and "+pdfDecoder.getPageCount()); 
		            return;
		        }
		        
		    }
		    
		});
		
		/**put page count in middle of forward and back*/
		currentBar1.add(pageCounter1);
		currentBar1.add(new JPanel());//add gap
		currentBar1.add(pageCounter2);
		currentBar1.add(new JPanel());//add gap
		currentBar1.add(pageCounter3);

		/**forward icon*/
		JButton forward = new JButton();
		forward.setBorderPainted(false);
		URL fowardImage =ClassLoader.getSystemResource("org/jpedal/examples/simpleviewer/forward.gif");
		forward.setIcon(new ImageIcon(fowardImage));
		forward.setToolTipText("forward 1 page"); 
		currentBar1.add(forward);
		forward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			if(currentFile!=null && currentPage<pdfDecoder.getPageCount()){
				currentPage += 1;
				try {
					pdfDecoder.decodePage(currentPage);
					pdfDecoder.invalidate();
					repaint();
				} catch (Exception e1) {
					System.err.println("forward 1 page");
					e1.printStackTrace();
				}
				
//				set page number display
				pageCounter2.setText(currentPage+"");
			}
			}
		});
		
		/**fast forward icon*/
		JButton fforward = new JButton();
		fforward.setBorderPainted(false);
		URL ffowardImage =ClassLoader.getSystemResource("org/jpedal/examples/simpleviewer/fforward.gif");
		fforward.setIcon(new ImageIcon(ffowardImage));
		fforward.setToolTipText("Fast forward 10 pages"); 
		currentBar1.add(fforward);
		fforward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			if(currentFile!=null && currentPage<pdfDecoder.getPageCount()-9){
				currentPage += 10;
				try {
					pdfDecoder.decodePage(currentPage);
					pdfDecoder.invalidate();
					repaint();
				} catch (Exception e1) {
					System.err.println("forward 10 pages");
					e1.printStackTrace();
				}
				
//				set page number display
				pageCounter2.setText(currentPage+"");
			}
			}
		});

		/**goto last page*/
		JButton end = new JButton();
		end.setBorderPainted(false);
		URL endImage =ClassLoader.getSystemResource("org/jpedal/examples/simpleviewer/end.gif");
		end.setIcon(new ImageIcon(endImage));
		end.setToolTipText("Fast forward to last page");
		currentBar1.add(end);
		end.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			if(currentFile!=null && currentPage<pdfDecoder.getPageCount()){
				currentPage = pdfDecoder.getPageCount();
				try {
					pdfDecoder.decodePage(currentPage);
					pdfDecoder.invalidate();
					repaint();
				} catch (Exception e1) {
					System.err.println("forward to last page");
					e1.printStackTrace();
				}
				
//				set page number display
				pageCounter2.setText(currentPage+"");
			}
			}
		});
		
		return currentBar1;
	}

	/**create a standalone program. User may pass in name of file as option*/
	public static void main(String[] args) {
		
		JPanelDemo current;
        
        /** Run the software */
        if (args.length > 0) {
            current = new JPanelDemo(args[0]);
        } else {
            current = new JPanelDemo();
        }
	}
}
