package cern.c2mon.client.jviews.adminmessage;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import java.awt.Dimension;
import javax.swing.text.PlainDocument;

public class AdminMessageSenderPanel extends JPanel{
  /**
     * Serial Version UID for the AdminMessageSenderPanel class  
     */
  private static final long serialVersionUID = -4170506973219576849L;
  private JLabel jLabel1 = new JLabel();
  private JTextArea jTextPane1 = new JTextArea();

  public AdminMessageSenderPanel() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public String getMessage() {
    return this.jTextPane1.getDocument().toString();
  }

  private void jbInit() throws Exception {
    this.setLayout(null);
    Dimension d = new Dimension(400, 230);
    this.setSize(d);
    this.setMaximumSize(d);
    this.setMinimumSize(d);
    this.setPreferredSize(d);
    this.setBounds(new Rectangle(10, 10, 400, 230));
    jLabel1.setText("<HTML><BODY><P>Please enter the message text:</P></BODY></HTML>");
    jLabel1.setBounds(new Rectangle(5, 5, 340, 15));
    jLabel1.setToolTipText("null");
    jLabel1.setSize(new Dimension(390, 20));
    jTextPane1.setText("jTextPane1");
    jTextPane1.setBounds(new Rectangle(5, 25, 390, 180));
    jTextPane1.setSize(new Dimension(390, 200));
    jTextPane1.setDocument(new PlainDocument());
    jTextPane1.setRows(25);
    this.add(jTextPane1, null);
    this.add(jLabel1, null);
  }
  
}