package ch09;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ch09.MultipleChatS.WinListener;

//로그온 메시지와 대화말 메시지를 전송함
//클라이언트 프로그램은 반드시 먼저 아이디를 전송하고 대화말을 전송한다.
public class ChatMessageS extends Frame {
	TextArea display;
	Label info;
	List<ServerThread> list;
	
	public ServerThread SThread;
	
	public ChatMessageS() {
		super("서버");
		info = new Label();
		add(info, BorderLayout.CENTER);
		display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		display.setEditable(false);
        add(display, BorderLayout.SOUTH);
        addWindowListener(new WinListener());
        setSize(300, 250);
        setVisible(true);
	}
	
	public void runServer() {
		ServerSocket server;
		Socket sock;
		ServerThread SThread;
		try {
			list = new ArrayList<ServerThread>();
			server = new ServerSocket(5000, 100);
			try {
				while(true) {
					sock = server.accept();
					SThread = new ServerThread(this, sock, display, info);
					SThread.start();
					info.setText(sock.getInetAddress() + " 서버는 클라이언트와 연결됨");
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
		ChatMessageS s = new ChatMessageS();
		s.runServer();
	}
	
	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
}
	
	class ServerThread extends Thread {
		Socket sock;
		BufferedWriter output;
		BufferedReader input;
		TextArea display;
		Label info;
		TextField text;
		String clientdata;
		String serverdata = "";
		ChatMessageS cs;
		
		private static final String SEPARATOR = "|";
		private static final int REQ_LOGON = 1001;
		private static final int REQ_SENDWORDS = 1021;
		
		public ServerThread(ChatMessageS c, Socket s, TextArea ta, Label l) {
			sock = s;
			display = ta;
			info = l;
			cs = c;
			try {
				input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		public void run() {
			try {
				cs.list.add(this);
				while((clientdata = input.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(clientdata, SEPARATOR);
					int command = Integer.parseInt(st.nextToken());
					int cnt = cs.list.size();
					switch(command) {
						case REQ_LOGON: {
							String ID = st.nextToken();
							display.append("클라이언트가 " + ID + "(으)로 로그인 하였습니다.\r\n");
							break;
						}
						case REQ_SENDWORDS: {
							String ID = st.nextToken();
							String message = st.nextToken();
							display.append(ID + " : " + message + "\r\n");
							for (int i=0; i<cnt; i++) { //모든 클라이언트에 전송
								ServerThread SThread = (ServerThread)cs.list.get(i);
								SThread.output.write(ID + " : " + message + "\r\n");
								SThread.output.flush();
							}
							break;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			cs.list.remove(this);
			try {
				sock.close();
			} catch (IOException ea) {
				ea.printStackTrace();
			}
		}
	}