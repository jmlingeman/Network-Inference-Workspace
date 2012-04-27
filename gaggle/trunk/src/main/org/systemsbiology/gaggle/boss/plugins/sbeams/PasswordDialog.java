// PasswordDialog.java
//-----------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins.sbeams;
//-----------------------------------------------------------------------------------------------
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.Locale;
//-----------------------------------------------------------------------------------------------
/*
 * PasswordDialog p = new PasswordDialog(null, "Test");
 * if(p.showDialog()){
 *     System.out.println("Name: " + p.getName());
 *     System.out.println("Pass: " + p.getPass());
 * } else {
 *     System.out.println("User selected cancel");
 * }
 */
public class PasswordDialog extends JDialog {
//-----------------------------------------------------------------------------------------------
  private boolean pressed_OK = false;
  protected JTextField name;
  protected JPasswordField pass;
  protected JButton okButton;
  protected JButton cancelButton;
  protected JLabel nameLabel;
  protected JLabel passLabel;
//-----------------------------------------------------------------------------------------------
  public PasswordDialog() {
	this(null, null);
  }
//-----------------------------------------------------------------------------------------------
  public PasswordDialog(String title) {
	this(null, title);
  }
//-----------------------------------------------------------------------------------------------
  public PasswordDialog(Frame parent) {
	this(parent, null);
  }
//-----------------------------------------------------------------------------------------------
  public PasswordDialog(Frame parent, String title) {
	super(parent, title, true);
  	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	if (title == null){
	  setTitle("User Login");
	}
	if (parent != null){
	  setLocationRelativeTo(parent);
	}
  }
//-----------------------------------------------------------------------------------------------
  protected void dialogInit(){

	name = new JTextField("", 20);
	pass = new JPasswordField("", 20);
	okButton = new JButton("OK");
	cancelButton = new JButton("Cancel");
	nameLabel = new JLabel("User Name ");
	passLabel = new JLabel("Password ");
	super.dialogInit();

	KeyListener keyListener = (new KeyAdapter() {
		public void keyPressed(KeyEvent e){
		  if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
			  (e.getSource() == cancelButton
			   && e.getKeyCode() == KeyEvent.VK_ENTER)){
			pressed_OK = false;
			PasswordDialog.this.setVisible(false);
		  }
		  if (e.getSource() == okButton &&
			  e.getKeyCode() == KeyEvent.VK_ENTER){
			pressed_OK = true;
			PasswordDialog.this.setVisible(false);
		  }
		}
	  });
	addKeyListener(keyListener);

      pass.addKeyListener (new KeyAdapter () {
        public void keyPressed (KeyEvent e) {
          if (e.getKeyText (e.getKeyCode()).equals ("Enter")) {
            pressed_OK = true;
            PasswordDialog.this.setVisible(false);
            }}});



	ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e){
		  Object source = e.getSource();
		  if (source == name){
			name.transferFocus();
		  } else {
			pressed_OK = (source == pass || source == okButton);
			PasswordDialog.this.setVisible(false);
			PasswordDialog.this.dispose();
		  }
		}
	  };


	// Layout
	GridBagLayout gridbag = new GridBagLayout();
	JPanel panel = new JPanel();
	JLabel label;

	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets.top = 5;
	constraints.insets.bottom = 5;

	JPanel pane = new JPanel(gridbag);
	pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

	constraints.anchor = GridBagConstraints.EAST;
	gridbag.setConstraints(nameLabel, constraints);
	pane.add(nameLabel);

	gridbag.setConstraints(name, constraints);
	name.addActionListener(actionListener);
	name.addKeyListener(keyListener);
	pane.add(name);

	constraints.gridy = 1;
	gridbag.setConstraints(passLabel, constraints);
	pane.add(passLabel);

	// Listeners
	gridbag.setConstraints(pass, constraints);
	pass.addActionListener(actionListener);
	pass.addKeyListener(keyListener);
	pane.add(pass);

	// Gridy
	constraints.gridy = 2;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	constraints.anchor = GridBagConstraints.CENTER;

	// Buttons
	okButton.addActionListener(actionListener);
	okButton.addKeyListener(keyListener);
	panel.add(okButton);
	cancelButton.addActionListener(actionListener);
	cancelButton.addKeyListener(keyListener);
	panel.add(cancelButton);
	gridbag.setConstraints(panel, constraints);
	pane.add(panel);

	GraphicsConfiguration gc = getGraphicsConfiguration ();
	int screenHeight = (int)gc.getBounds().getHeight();
	int screenWidth = (int)gc.getBounds().getWidth();
	int windowHeight = (int)getHeight();
	int windowWidth = (int)getWidth();
	int x = (int)((screenWidth-windowWidth)/2);
	int y = (int)((screenHeight-windowHeight)/2);
	setLocation(x,y);
	getContentPane().add(pane);
	pack();
  }
//-----------------------------------------------------------------------------------------------
  public void setPasswordFocus(){
	this.pass.requestFocus();
  }
//-----------------------------------------------------------------------------------------------
  public void setUsernameFocus(){
	this.name.requestFocus();
  }
//-----------------------------------------------------------------------------------------------
  public void setName(String name){
	this.name.setText(name);
  }
//-----------------------------------------------------------------------------------------------
  public void setPass(String pass){
	this.pass.setText(pass);
  }
//-----------------------------------------------------------------------------------------------
  public void setOKText(String ok){
	this.okButton.setText(ok);
	pack();
  }
//-----------------------------------------------------------------------------------------------
  public void setCancelText(String cancel){
	this.cancelButton.setText(cancel);
	pack();
  }
//-----------------------------------------------------------------------------------------------
  public void setNameLabel(String name){
	this.nameLabel.setText(name);
	pack();
  }
//-----------------------------------------------------------------------------------------------
  public void setPassLabel(String pass){
	this.passLabel.setText(pass);
	pack();
  }
//-----------------------------------------------------------------------------------------------
  public String getName(){
	return name.getText();
  }

//-----------------------------------------------------------------------------------------------
  public String getPass(){
	return new String(pass.getPassword());
  }
//-----------------------------------------------------------------------------------------------
  public boolean okPressed(){
	return pressed_OK;
  }
//-----------------------------------------------------------------------------------------------
  public boolean showDialog(){
	setVisible(true);
	return okPressed();
  }
//-----------------------------------------------------------------------------------------------
/*
  public static void main(String[] args){
	PasswordDialog p = new PasswordDialog();
	if(args.length > 0)
	  p.setName(args[0]);
	if(args.length > 1)
	  p.setPass(args[1]);
	
	if(p.showDialog()){
	  System.out.println("User Name: " + p.getName());
	  System.out.println("Password: " + p.getPass());
	} else {
	  System.out.println("User selected cancel");
	}
	p.dispose();
	p = null;
	System.exit(0);
  }
*/
}
