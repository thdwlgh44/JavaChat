package ch09;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatMessageC extends Frame implements ActionListener {

    TextArea display;
    TextField wtext, ltext;
    Label mlbl, wlbl, loglbl;
    BufferedWriter output;
    BufferedReader input;
    Socket client;
    StringBuffer clientdata;
    String serverdata;
    String ID;

    private static final String SEPARATOR = "|";
    private static final int REQ_LOGON = 1001;
    private static final int REQ_SENDWORDS = 1021;

    public ChatMessageC() {
        super("클라이언트");

        mlbl = new Label("채팅 상태를 보여줍니다.");
        add(mlbl, BorderLayout.NORTH);

        display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        display.setEditable(false);
        add(display, BorderLayout.CENTER);

        Panel ptotal = new Panel(new BorderLayout());

        Panel pword = new Panel(new BorderLayout());
        wlbl = new Label("대화말");
        wtext = new TextField(30); // 전송할 데이터를 입력하는 필드
        wtext.addActionListener(this); // 입력된 데이터를 송신하기 위한 이벤트 연결
        pword.add(wlbl, BorderLayout.WEST);
        pword.add(wtext, BorderLayout.CENTER);
        ptotal.add(pword, BorderLayout.CENTER);

        Panel plabel = new Panel(new BorderLayout());
        loglbl = new Label("로그온");
        ltext = new TextField(30); // 전송할 데이터를 입력하는 필드
        ltext.addActionListener(this); // 입력된 데이터를 송신하기 위한 이벤트 연결
        plabel.add(loglbl, BorderLayout.WEST);
        plabel.add(ltext, BorderLayout.CENTER);
        ptotal.add(plabel, BorderLayout.SOUTH);

        add(ptotal, BorderLayout.SOUTH);

        addWindowListener(new WinListener());
        setSize(400, 300);
        setVisible(true);
    }

    public void runClient() {
        try {
            client = new Socket(InetAddress.getLocalHost(), 5000);
            mlbl.setText("연결된 서버이름: " + client.getInetAddress().getHostName());
            input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
            clientdata = new StringBuffer(2048);
            mlbl.setText("접속 완료 사용할 아이디를 입력하세요.");
            while (true) {
                serverdata = input.readLine();
                display.append(serverdata + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == ltext) {
            if (ID == null) {
                ID = ltext.getText();
                mlbl.setText(ID + "(으)로 로그인 하였습니다.");
                try {
                    clientdata.setLength(0);
                    clientdata.append(REQ_LOGON);
                    clientdata.append(SEPARATOR);
                    clientdata.append(ID);
                    output.write(clientdata.toString() + "\r\n");
                    output.flush();
                    ltext.setVisible(false);
                    loglbl.setVisible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (ae.getSource() == wtext) {
            String message = wtext.getText();
            if (ID == null) {
                mlbl.setText("다시 로그인 하세요!!!");
                wtext.setText("");
            } else {
                try {
                    clientdata.setLength(0);
                    clientdata.append(REQ_SENDWORDS);
                    clientdata.append(SEPARATOR);
                    clientdata.append(ID);
                    clientdata.append(SEPARATOR);
                    clientdata.append(message);
                    output.write(clientdata.toString() + "\r\n");
                    output.flush();
                    wtext.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }//actionPerformed()

    public static void main(String[] args) {
        ChatMessageC c = new ChatMessageC();
        c.runClient();
    }

    class WinListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }
}
