package client;

import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MessageBox extends JDialog implements ActionListener {

    private Container c;
    private JButton bt;

    public MessageBox(JFrame parent, String title, String message) {
        super(parent, false);
        setTitle(title);
        c = getContentPane();
        c.setLayout(null);
        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("SanSerif", Font.PLAIN, 12));
        lbl.setBounds(20, 10, 190, 20);
        c.add(lbl);

        bt = new JButton("확 인");
        bt.setBounds(60, 40, 70, 25);
        bt.setFont(new Font("SanSerif", Font.PLAIN, 12));
        lbl.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        bt.addActionListener(this);
        c.add(bt);

        Dimension dim = getToolkit().getScreenSize();
        setSize(200, 100);
        setLocation(dim.width/2 - getWidth()/2, dim.height/2 - getHeight()/2);
        show();

        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        dispose();
                    }
                }
        );
    }

    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == bt) {
            dispose();
        }
    }


}
