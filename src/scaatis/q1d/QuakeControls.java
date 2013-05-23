package scaatis.q1d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;

public class QuakeControls {

    private JFrame frame;
    private DefaultListModel<Connection> model;
    private JList<Connection> list;
    private JButton btnKick;
    private JButton btnRestartRound;
    private JTextField textField;
    private JLabel lblCurrentTurn;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    QuakeControls window = new QuakeControls();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public QuakeControls() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame("Quake 1D Server");
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        
        model = new DefaultListModel<>();
        
        list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new PlayerRenderer());
        list.setModel(model);
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane);
        
        btnKick = new JButton("Kick");
        btnKick.setPreferredSize(new Dimension(52, 100));
        btnKick.setMnemonic('k');
        panel.add(btnKick);
        
        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(null, "Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frame.getContentPane().add(panel_1, BorderLayout.SOUTH);
        
        btnRestartRound = new JButton("Restart Round");
        btnRestartRound.setMnemonic('r');
        panel_1.add(btnRestartRound);
        
        lblCurrentTurn = new JLabel("Current Turn:");
        panel_1.add(lblCurrentTurn);
        
        textField = new JTextField();
        textField.setEditable(false);
        panel_1.add(textField);
        textField.setColumns(10);
        
    }

    private class PlayerRenderer extends JLabel implements ListCellRenderer<Connection> {
        
        private static final long serialVersionUID = 1L;

        public PlayerRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Connection> list,
                Connection connection, int index, boolean isSelected, boolean cellHasFocus) {
            
            if (isSelected) {
                setText("> " + connection.getAddress());
            } else {
                setText(connection.getAddress());
            }
            
            setForeground(Color.black);
            setBackground(new Color(connection.getPlayer().getColor()));
            setFont(list.getFont());
            return this;
        }
    }
    
    public void setVisible() {
        frame.setVisible(true);
    }
    
    public void addElement(Connection elem) {
        model.addElement(elem);
    }
    
    public void removeElement(Connection elem) {
        model.removeElement(elem);
    }
    
    public void dispose() {
        frame.dispose();
    }
    
    public JButton getBtnKick() {
        return btnKick;
    }
    
    public JButton getBtnRestartRound() {
        return btnRestartRound;
    }
    
    public Connection getSelected() {
        return list.getSelectedValue();
    }
    public JTextField getTextField() {
        return textField;
    }
}
