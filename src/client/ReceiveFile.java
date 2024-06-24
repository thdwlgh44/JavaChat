package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveFile extends Frame implements ActionListener {

    public static final int port = 3777;
    public Label lbl;
    public TextArea txt;
    public Button btn;

    public ReceiveFile() {
        super("파일전송");
        setLayout(null);
        lbl = new Label("파일 전송을 기다립니다.");
        lbl.setBounds(10, 30, 230, 20);
        lbl.setBackground(Color.gray);
        lbl.setForeground(Color.white);
        add(lbl);
        txt = new TextArea("", 0,0,TextArea.SCROLLBARS_BOTH);
        txt.setBounds(10, 60, 230, 100);
        txt.setEditable(false);
        add(txt);
        btn = new Button("닫기");
        btn.setBounds(105, 170, 40, 20);
        btn.setVisible(false);
        btn.addActionListener(this);
        add(btn);
        addWindowListener(new WinListener());
        setSize(250, 200);
        show();

        try {
            ServerSocket socket = new ServerSocket(port);
            Socket sock = null;
            FileThread client = null;
            try {
                sock = socket.accept();
                client = new FileThread(this, sock);
                client.start();
            } catch (IOException e) {
                System.out.println(e);
                try {
                    if (sock != null) sock.close();
                } catch (IOException e1) {
                    System.out.println(e1);
                } finally {
                    sock = null;
                }
            }
        } catch (IOException e) {}
    }

    public void actionPerformed(ActionEvent e) {
        dispose();
    }

    class WinListener extends WindowAdapter {
        public void windowClosing(WindowEvent we) {
           dispose();
        }
    }









}
