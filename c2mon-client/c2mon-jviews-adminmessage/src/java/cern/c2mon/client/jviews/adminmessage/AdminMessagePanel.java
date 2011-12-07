package cern.c2mon.client.jviews.adminmessage;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import cern.c2mon.client.common.admin.AdminMessage;

public class AdminMessagePanel extends JPanel {
  /**
     * Serial Version UID for the AdminMessagePanel class 
     */
  private static final long serialVersionUID = 7289995229599870483L;
  private JTextPane jTextPane1 = new JTextPane();
  private JLabel labelHeader = new JLabel();
  private JLabel labelSendBy1 = new JLabel();
  private AdminMessage msg = null;
  private JScrollPane jScrollPane1 = new JScrollPane();

  public AdminMessagePanel(AdminMessage pMsg) {
    msg=pMsg; 
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);
    this.setSize(new Dimension(300, 200));
    this.setMaximumSize(new Dimension(300, 220));
    this.setPreferredSize(new Dimension(300, 220));
    this.setBounds(new Rectangle(10, 10, 300, 220));
    jScrollPane1.setBounds(new Rectangle(5, 30, 290, 150));
    jTextPane1.setBounds(new Rectangle(5, 30, 290, 150));
    jTextPane1.setEditable(false);
    
    //jTextPane1.setContentType("text/html");
    jTextPane1.setText(msg.getMessage());
    labelHeader.setText("<HTML><BODY><P>The following message has been received:</P></BODY></HTML>");
    labelHeader.setBounds(new Rectangle(5, 5, 290, 20));
    labelHeader.setFont(new Font("Tahoma", 1, 12));
    labelSendBy1.setText("<HTML><BODY><P>Sent by user <B>"+ msg.getSender() + "</B> at <B>" + msg.getTimestamp() + "</B>.</P></BODY></HTML>");
    labelSendBy1.setBounds(new Rectangle(5, 180, 295, 25));
    labelSendBy1.setToolTipText("null");
    labelSendBy1.setFont(new Font("Tahoma", 0, 12));
    jScrollPane1.getViewport().add(jTextPane1,null);
    this.add(jScrollPane1, null);
    this.add(labelSendBy1, null);
    //this.add(jTextPane1, null);
    this.add(labelHeader, null);
  }
}