package cern.c2mon.client.jviews.adminmessage;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.admin.AdminMessage;
import cern.c2mon.client.common.admin.AdminMessageImpl;
import cern.c2mon.client.common.admin.AdminMessage.AdminMessageType;

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

  private static final int TIMESTAMP_COLUMN = 0;
  private static final int MESSAGE_CONTENT_COLUMN = 1;
  private static final int AUTHOR_COLUMN = 2;

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
    tableMessages.setModel(new MessageTableModel(messages));
    this.add(tableMessages, null);
    this.add(labelHeader, null);

    tableMessages.addMouseListener(new MouseAdapter() {
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
          int row = tableMessages.getSelectedRow();
          final String message = (String) tableMessages.getModel().getValueAt(row, 1);
          final String author = (String) tableMessages.getModel().getValueAt(row, 2);
          showMessage(message, author);
        }
      }
    });
    //addTestMessages(); // only for debbuging
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
      if (messages.length == 0) {
        return "No messages have been received yet.";
      }
      AdminMessage msg = messages[rowIndex];
      switch(columnIndex) {
        case TIMESTAMP_COLUMN: 
          return format.format(msg.getTimestamp());
        case MESSAGE_CONTENT_COLUMN: return msg.getMessage();
        case AUTHOR_COLUMN:
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
  
  /**
   * Displays a message in its own dialog box.
   * This is useful as some messages are very long
   * and cannot fit in the History Panel.
   * @param message The message to be displayed
   * @param author The author of the message
   */
  private void showMessage(final String message, final String author) {
    
    JOptionPane.showMessageDialog(null,
        message.replace(". ", ".\n"),
        "[" + author + "]",
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Used only for testing.
   * Adds a test message in the list of messages.
   */
  private void addTestMessages() {

    AdminMessage m1 = new AdminMessageImpl(AdminMessageType.WARN
        , "Anonymous Hacker"
        , "Hi Guys!"
        , new Timestamp(System.currentTimeMillis()));
    
    AdminMessage m2 = new AdminMessageImpl(AdminMessageType.WARN
        , "Anonymous Hacker who likes writing long messages"
        , "This is a super long message... It's just here to test " 
        + "that super long messages can be seen properly.. \n" 
        + "Otherwise it's of no use and noone really cares about it."
        , new Timestamp(System.currentTimeMillis()));
    
    Collection<AdminMessage> testMessages = new ArrayList<AdminMessage>();
    testMessages.add(m1);
    testMessages.add(m2);

    setMessages(testMessages);
  }
}