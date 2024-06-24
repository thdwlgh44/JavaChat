package ch09;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MultipleChatS extends Frame {
    TextArea display;
    Label info;
    String clientdata = "";
    String serverdata = "";
    List<ServerThread> list;

    public MultipleChatS() {
        super("서버");
        info = new Label();
        add(info, BorderLayout.NORTH);
        display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        display.setEditable(false);
        add(display, BorderLayout.CENTER);
        addWindowListener(new WinListener());
        setSize(400, 300);
        setVisible(true);
    }

    public void runServer() {
        ServerSocket server;
        Socket sock;
        ServerThread SThread;
        try {
            list = new ArrayList<>();
            server = new ServerSocket(5000, 100);
            try {
                while (true) {
                    sock = server.accept();
                    SThread = new ServerThread(this, sock, display, info, serverdata);
                    SThread.start();
                    info.setText(sock.getInetAddress().getHostName() + " 서버는 클라이언트와 연결됨");
                }
            } catch (IOException ioe) {
                server.close();
                ioe.printStackTrace();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MultipleChatS s = new MultipleChatS();
        s.runServer();
    }

    class WinListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

    class ServerThread extends Thread {
        Socket sock;
        InputStream is;
        InputStreamReader isr;
        BufferedReader input;
        OutputStream os;
        OutputStreamWriter osw;
        BufferedWriter output;
        TextArea display;
        Label info;
        MultipleChatS cs;

        public ServerThread(MultipleChatS c, Socket s, TextArea ta, Label l, String data) {
            sock = s;
            display = ta;
            info = l;
            cs = c;
            try {
                is = sock.getInputStream();
                isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                input = new BufferedReader(isr);
                os = sock.getOutputStream();
                osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                output = new BufferedWriter(osw);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public void run() {
            cs.list.add(this);
            String clientdata;
            try {
                while ((clientdata = input.readLine()) != null) {
                    display.append(clientdata + "\r\n");
                    int cnt = cs.list.size();
                    for (int i = 0; i < cnt; i++) { // 모든 클라이언트에 데이터 전송
                        ServerThread SThread = cs.list.get(i);
                        SThread.output.write(clientdata + "\r\n");
                        SThread.output.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            cs.list.remove(this); // 리스트에서 close된 클라이언트를 지운다.
            try {
                sock.close(); // 소켓닫기
            } catch (IOException ea) {
                ea.printStackTrace();
            }
        }
    }
}
