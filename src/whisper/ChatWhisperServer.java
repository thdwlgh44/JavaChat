package whisper;

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
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

//클라이언트 간의 채팅에서 특정 클라이언트와의 귓속말 구현
public class ChatWhisperServer extends Frame {
	
	TextArea display;
	Label info;
	List<ServerThread> list;
	Hashtable hash;
	public ServerThread SThread;
	
	public ChatWhisperServer() {
		
		super("서버");
		info = new Label();
		add(info, BorderLayout.CENTER);
		display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		display.setEditable(false);
		add(display, BorderLayout.SOUTH);
		addWindowListener(new WinListener());
		setSize(300,250);
		setVisible(true);
	}
	
	public void runServer() {
		
		ServerSocket server;
		Socket sock;
		ServerThread SThread;
		
		try {
			//1. port를 바인딩한 ServerSocket 생성한다.
			server = new ServerSocket(5000, 100);
			hash = new Hashtable();
			list = new ArrayList<ServerThread>();
			try {
				while(true) {
					
					//2. ServerSocket 생성 후 클라이언트 요청 대기 상태... 
					//클라이언트 요청이 들어오면 서로 통신 가능한 출입구 Socket이 만들어진다.
					//최초 요청 이후 연속되는 요청에 대해서 다시 새로운 스레드를 만들어 제공함으로 다중 채팅이 가능하다.
					sock = server.accept();
					
					//4. 클라이언트 요청에 따라 소켓과 데이터 흐름을 처리할 수 있는 작업 스레드가 만들어지고, 
					//서버가 어떤 컴퓨터 ip주소와 연결됐는지 보여준다.
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
		ChatWhisperServer s = new ChatWhisperServer();
		s.runServer();
	}
	
	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	
}//class

//클라이언트 요청에 따라 작업스레드가 생성되었고 어떤 요청인지에 따라 작업을 다르게 처리할 수 있다.
class ServerThread extends Thread {
	Socket sock;
	BufferedWriter output;
	BufferedReader input;
	TextArea display;
	Label info;
	TextField text;
	String clientdata;
	String serverdata = "";
	ChatWhisperServer cs;
	
	private static final String SEPARATOR = "|";
//	private static final String PIPE_REPLACEMENT = "[PIPE]";
	private static final int REQ_LOGON = 1001;
	private static final int REQ_SENDWORDS = 1021;
	private static final int REQ_WISPERSEND = 1022;
	private static final int REQ_LOGON_FAIL = 1002;
	
	public ServerThread(ChatWhisperServer c, Socket s, TextArea ta, Label l) {
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
//				clientdata = clientdata.replace(PIPE_REPLACEMENT, SEPARATOR); // 수신 시 복원
				//구분자를 기준으로 문자열을 분리할 때 구분자도 토큰으로 넣는다.
				StringTokenizer st = new StringTokenizer(clientdata, SEPARATOR);
//				StringTokenizer st = new StringTokenizer(clientdata, SEPARATOR, true);
				if (!st.hasMoreTokens()) continue; // 토큰이 없으면 다음 루프로 넘어감
				int command = Integer.parseInt(st.nextToken());
				int Lcnt = cs.list.size();
				switch(command) {
				
				//최초 서버와 클라이언트가 연결되고나서 로그인했을 때 안내문구를 띄운다.
					case REQ_LOGON: {
						if (!st.hasMoreTokens()) continue;
						String ID = st.nextToken();
						if (cs.hash.containsKey(ID)) {
							output.write(REQ_LOGON_FAIL + SEPARATOR + "이미 사용 중인 아이디입니다.\r\n");
							output.flush();
						} else {
							display.append("클라이언트가 " + ID + "(으)로 로그인 하였습니다.\r\n");
							cs.hash.put(ID, this);
							output.write(REQ_LOGON + SEPARATOR + ID + "\r\n");
                            output.flush();
						}
//						display.append("클라이언트가 " + ID + "(으)로 로그인 하였습니다.\r\n");
//						cs.hash.put(ID, this); //해쉬테이블에 아이디와 스레드를 저장한다. => 귓속말 기능에서 사용할 정보.
						break;
					}
					case REQ_SENDWORDS: {
						if (!st.hasMoreTokens()) continue;
						String ID = st.nextToken();
						if (!st.hasMoreTokens()) continue;
						String message = st.nextToken();
						
//						message = message.replace(SEPARATOR, PIPE_REPLACEMENT); // 전송 시 변환
						
						//대화말 전송 시 보여줌
						display.append(ID + " : " + message + "\r\n");
						for (int i=0; i<Lcnt; i++) { //모든 클라이언트에 대화말 정보 전송
							ServerThread SThread = (ServerThread)cs.list.get(i);
							SThread.output.write(ID + " : " + message + "\r\n");
							SThread.output.flush();
						}
						break;
					}
					case REQ_WISPERSEND: {
						if (!st.hasMoreTokens()) continue;
						String ID = st.nextToken();
						if (!st.hasMoreTokens()) continue;
						String WID = st.nextToken();
						if (!st.hasMoreTokens()) continue;
						String message = st.nextToken();
//						message = message.replace(SEPARATOR, PIPE_REPLACEMENT); // 전송 시 변환
						
						//다음과 같은 형식으로 귓속말
						display.append("[귓속말] " + ID + " -> " + WID + " : " + message + "\r\n");
						ServerThread SThread = (ServerThread)cs.hash.get(ID);
						
						//해시테이블에서 귓속말 메시지를 전송한 클라이언트의 스레드를 가져옴
						SThread.output.write("[귓속말] " + ID + " -> " + WID + " : " + message + "\r\n");
						
						//귓속말 메시지를 전송한 클라이언트에 전송함
						SThread.output.flush();
						SThread = (ServerThread)cs.hash.get(WID);
						
						//해시테이블에서 귓속말 메시지를 수신할 클라이언트의 스레드를 구함
						SThread.output.write("[From " + ID + "] " + message + "\r\n");
						
						//귓속말 메시지를 수신할 클라이언트에 전송함
						SThread.output.flush();
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//소켓 닫기 전 정보 지우기
		cs.list.remove(this);
		try {
			sock.close();
		} catch (IOException ea) {
			ea.printStackTrace();
		}
	}
	
}
