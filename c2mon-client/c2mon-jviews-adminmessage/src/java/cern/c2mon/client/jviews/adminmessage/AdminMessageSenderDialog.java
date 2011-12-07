package cern.c2mon.client.jviews.adminmessage;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.admin.AdminMessage;
import cern.c2mon.client.common.admin.AdminMessageImpl;
import cern.c2mon.client.common.admin.AdminMessage.AdminMessageType;

public class AdminMessageSenderDialog extends JDialog {

  /**
     * Serial Version UID for the AdminMessageSenderDialog class
     */
    private static final long serialVersionUID = 4356945784884369516L;

  private static final Logger LOG = Logger.getLogger(AdminMessageSenderDialog.class);
  
  private boolean sendMessage = false;
  
  private String messageText = null;
  
  private JButton sendButton = new JButton();
  private JButton previewButton = new JButton();
  private JButton cancelButton = new JButton();
  private JLabel labelHeader = new JLabel();
  private JTextPane messagePane = new JTextPane();
  private JScrollPane jScrollPane1 = new JScrollPane();
  
  
  public AdminMessageSenderDialog(Frame parent) {
    super(parent, "Send an administrator message to all users", true);
    try {
      jbInit();
    }
    catch (Exception e) {
      LOG.error("error occured while initialising AdminSenderDialog",e);             
    }
  }
  
  public String getMessageText() {
    return this.messageText;
  }
  
  /**
   * @return the message type
   */
  public AdminMessage.AdminMessageType getMessageType() {
    return AdminMessageType.INFO;
  }

  public AdminMessageSenderDialog() {
    try {
      jbInit();
    } catch(Exception e) {
      LOG.error("error occured while initialising AdminSenderDialog",e);         
    }
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(null);
    setSize(400, 300);
  
    //set the default location (as setting the parent frame not always work in a multiscreen environment)
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();
    this.setLocation((screenSize.width/ 2 - frameSize.width/2), 
                     (screenSize.height/ 2 - frameSize.height/2));

    sendButton.setText("Send");
    sendButton.setBounds(new Rectangle(60, 230, 87, 25));
    sendButton.setActionCommand("send");
    sendButton.setToolTipText("Send the message");
    sendButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          send(e);
        }
      });
    previewButton.setText("Preview");
    previewButton.setBounds(new Rectangle(150, 230, 87, 25));
    previewButton.setActionCommand("preview");
    previewButton.setToolTipText("Preview the message before sending it");
    previewButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          previewMessage(e);
        }
      });
    cancelButton.setText("Cancel");
    cancelButton.setBounds(new Rectangle(240, 230, 87, 25));
    cancelButton.setActionCommand("cancel");
    cancelButton.setToolTipText("Close this dialog without sending the message.");
    cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cancel(e);
        }
      });
    labelHeader.setText("<HTML><BODY><P>Please enter the message you would like to send to " + "all users.</P></BODY></HTML>");
    labelHeader.setBounds(new Rectangle(5, 5, 380, 15));
    labelHeader.setToolTipText("null");
    labelHeader.setMinimumSize(new Dimension(380, 15));
    labelHeader.setMaximumSize(new Dimension(380, 15));
    labelHeader.setPreferredSize(new Dimension(380, 15));
    jScrollPane1.setBounds(new Rectangle(5, 25, 380, 200));
    messagePane.setBounds(new Rectangle(5, 25, 380, 200));
    messagePane.setToolTipText("null");
    messagePane.setMinimumSize(new Dimension(380, 200));
    messagePane.setMaximumSize(new Dimension(380, 200));
    messagePane.setPreferredSize(new Dimension(380, 200));
    jScrollPane1.getViewport().add(messagePane,null);
    this.getContentPane().add(jScrollPane1, null);
    //this.getContentPane().add(messagePane, null);
    this.getContentPane().add(labelHeader, null);
    this.getContentPane().add(cancelButton, null);
    this.getContentPane().add(previewButton, null);
    this.getContentPane().add(sendButton, null);
    
  }

  private void previewMessage(ActionEvent e) {
    AdminMessagePopup.showAdminMessage(
        getParent(), 
        new AdminMessageImpl(
            getMessageType(), 
            "<User Name>", 
            this.messagePane.getText(), 
            new Timestamp(System.currentTimeMillis())), 
        "Admin message PREVIEW");
  }
  
  private void cancel(ActionEvent e) {
    sendMessage = false;
    messageText = null;
    dispose();
  }

  private void send(ActionEvent e) {
    sendMessage = true;
    messageText = messagePane.getText();
    dispose();
  }
  
  public boolean sendMessage() {
    return this.sendMessage;
  }

}