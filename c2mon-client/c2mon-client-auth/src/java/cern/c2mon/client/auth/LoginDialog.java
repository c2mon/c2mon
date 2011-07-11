package cern.c2mon.client.auth;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginDialog extends JDialog implements PropertyChangeListener {

  /**
   * Serial Version UID identifier that allows to detect version changes
   */
  private static final long serialVersionUID = -324224070483783866L;
    
  private JTextField userField;
  private JTextField pwdField;
  private JLabel messageLabel;
  private JOptionPane optionPane;
  private String btnString1 = "OK";
  private String btnString2 = "Cancel";
  
  public LoginDialog(String loginMessage, Frame owner) {
    super(owner);

    setTitle("TimViewer Login Dialog");

    messageLabel = new JLabel(loginMessage);
    messageLabel.setForeground(Color.BLACK);
    messageLabel.setHorizontalAlignment(JLabel.CENTER);
    
    JPanel userPanel = new JPanel();
    userPanel.setLayout(new GridLayout(1, 2));
    userField = new JTextField(15);
    userPanel.add(new JLabel("User name: "));
    userPanel.add(userField);
    
    JPanel pwdPanel = new JPanel();
    pwdPanel.setLayout(new GridLayout(1, 2));    
    pwdField = new JPasswordField(15);
    pwdPanel.add(new JLabel("Password: "));
    pwdPanel.add(pwdField);

    //Create an array of the text and components to be displayed.
    Object[] array = {messageLabel, userPanel, pwdPanel};
    //Create an array specifying the number of dialog buttons and their text.
    Object[] options = {btnString1, btnString2};    

    //Create the JOptionPane.
    optionPane = new JOptionPane(array,
                                 JOptionPane.PLAIN_MESSAGE,
                                 JOptionPane.YES_NO_OPTION,
                                 null,
                                 options,
                                 options[0]);

    //Make this dialog display it.
    setContentPane(optionPane);
    //Register an event handler that reacts to option pane state changes.
    optionPane.addPropertyChangeListener(this);
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension dialogSize = new Dimension(250, 160);
    setSize(dialogSize);
    setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
    setResizable(false);
    setVisible(true);
  }

  public LoginDialog(Frame owner)
  {
    this("Please enter your NICE login", owner);
  }

/** This method reacts to state changes in the option pane. */
  public void propertyChange(PropertyChangeEvent e)
  {

    String prop = e.getPropertyName();
    if (isVisible() && (e.getSource() == optionPane)
        && (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)))
    {
      Object value = optionPane.getValue();
      if (value == JOptionPane.UNINITIALIZED_VALUE)
      {
        //ignore reset
        return;
      }
      // Reset the JOptionPane's value (without this reset, if the user
      // presses the same button next time, no property change event is fired)
      optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
      if (btnString1.equals(value))
      {
        String username = userField.getText();
        String password = pwdField.getText();
        if (username == null || username.equals("")) {
          messageLabel.setText("Please enter a user name");
          messageLabel.setForeground(Color.RED);
          userField.requestFocus();
        }
        else if (password == null || password.equals("")) {
          messageLabel.setText("Please enter a valid password");
          messageLabel.setForeground(Color.RED);
          pwdField.requestFocus();
        }
        else if (SessionManager.getInstance().login(username, password) == null) 
        {
          clear();
          messageLabel.setText("The login is incorrect");
          messageLabel.setForeground(Color.RED);
          userField.requestFocus();
        }  
        else
        {
          clear();
          setVisible(false);
        }
      }
      else
      { // user clicked cancel
         clear();
         setVisible(false);
      }
    }
  }

  /** This method clears the dialog and hides it. */
  public void clear()
  {
    userField.setText(null);
    pwdField.setText(null);  
  }  
}