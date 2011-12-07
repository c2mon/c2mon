package cern.c2mon.client.jviews.adminmessage;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.admin.AdminMessage;

/**
 * A panel for displaying all AdminMessages that were received since the TIM
 * Viewer was started.
 * @author J. Stowisek
 */

public class AdminMessageHistoryPanel extends JPanel {
  /**
     * Serial Version UID for the AdminMessageHistoryPanel class
     */
  private static final long serialVersionUID = -3322922665656938973L;
  private JLabel labelHeader = new JLabel();
  private JTable tableMessages = new JTable();
  private JTableHeader jTableHeader1 = new JTableHeader();
  private static final Logger LOG = Logger.getLogger(AdminMessageHistoryPanel.class);

  public AdminMessageHistoryPanel(Collection<AdminMessage> messages) {
    this.setLayout(null);
    Dimension d = new Dimension(400, 200);
    
    this.setSize(d);
    this.setPreferredSize(d);
    this.setMaximumSize(d);
    this.setMaximumSize(d);
    labelHeader.setText("<HTML><BODY><P>The following administrator messages were received:</P></BODY></HTML>");
    labelHeader.setBounds(new Rectangle(5, 5, 390, 20));
    tableMessages.setBounds(new Rectangle(5, 25, 390, 170));
    tableMessages.setModel(new MessageTableModel(messages) );
    this.add(tableMessages, null);
    this.add(labelHeader, null);
  }
  
  public void setMessages(final Collection<AdminMessage> pMessages) {
    if (pMessages == null) {
      return;
    }
    tableMessages.setModel(new MessageTableModel(pMessages));    
  }
  
  class MessageTableModel extends AbstractTableModel {
    /**
     * Serial Version UID for the MessageTableModel class
     */
    private static final long serialVersionUID = -5507412590528170978L;
    private AdminMessage[] messages = {};
    private DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    
    private String[] column_names = {"Received at", "Message text", "Sent by"};

    public MessageTableModel(final Collection<AdminMessage> pMessages) {
      this.messages = pMessages.toArray(new AdminMessage[0]);
    }
    
    public  int getColumnCount() {
      if (messages.length == 0) {
        return 1;
      }
      else {
        return column_names.length;
      }
    }
    
    public int getRowCount() {
      if (messages.length == 0) {
        return 1;
      }
      else {
        return this.messages.length;
      }
    }
    
    public String getColumnName(int columnIndex) { 
      if (messages.length == 0) {
        return "No messages were received yet";
      }
      else {
        return this.column_names[columnIndex];
      }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (messages.length==0) {
        return "No messages have been received yet.";
      }
      AdminMessage msg = messages[rowIndex];
      switch(columnIndex) {
        case 0: 
          return format.format(msg.getTimestamp());
        case 1: return msg.getMessage();
        case 2:
          if (msg.getSender() != null) {
            return msg.getSender();
          }
          else {
            return "An anonymous hacker";
          }
        default: return "";
      }
    }
    
  }

}