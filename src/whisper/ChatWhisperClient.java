package whisper;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import java.util.StringTokenizer;

public class ChatWhisperClient extends Frame implements ActionListener, KeyListener {
	
	//java swing ui를 구성하는 필드, 데이터를 주고받는 스트림 필드, 통신 소켓 필드 등 선언
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
//    private static final String PIPE_REPLACEMENT = "[PIPE]";
    private static final int REQ_LOGON = 1001;
    private static final int REQ_LOGON_FAIL = 1002;
    private static final int REQ_SENDWORDS = 1021;
    private static final int REQ_WISPERSEND = 1022;
    
    public ChatWhisperClient() {
        super("클라이언트");

        mlbl = new Label("채팅 상태를 보여줍니다.");
        add(mlbl, BorderLayout.NORTH);

        //서버와 연결된 클라이언트의 최초 로그온 화면
        display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        display.setEditable(false);
        add(display, BorderLayout.CENTER);

        Panel ptotal = new Panel(new BorderLayout());

        Panel pword = new Panel(new BorderLayout());
        wlbl = new Label("대화말");
        wtext = new TextField(30); // 전송할 데이터를 입력하는 필드
        wtext.addKeyListener(this); // 입력된 데이터를 송신하기 위한 이벤트 연결
        pword.add(wlbl, BorderLayout.WEST);
        pword.add(wtext, BorderLayout.EAST);
        ptotal.add(pword, BorderLayout.CENTER);

        //로그온 텍스트필드에서 처음 사용할 아이디를 입력한다.
        Panel plabel = new Panel(new BorderLayout());
        loglbl = new Label("로그온");
        ltext = new TextField(30); // 전송할 데이터를 입력하는 필드
        ltext.addActionListener(this); // 입력된 데이터를 송신하기 위한 이벤트 연결
        plabel.add(loglbl, BorderLayout.WEST);
        plabel.add(ltext, BorderLayout.EAST);
        ptotal.add(plabel, BorderLayout.SOUTH);

        add(ptotal, BorderLayout.SOUTH);

        addWindowListener(new WinListener());
        setSize(400, 350);
        setVisible(true);
    }
    
    //4. 클라이언트 접속 요청이 서버와 연결되면서 서로 데이터를 주고받을 수 있는 Socket과 스트림 객체가 만들어진다.
    public void runClient() {
    	
        try {
            client = new Socket(InetAddress.getLocalHost(), 5000);
            mlbl.setText("연결된 서버이름: " + client.getInetAddress().getHostName());
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            clientdata = new StringBuffer(2048);
            mlbl.setText("접속 완료 사용할 아이디를 입력하세요.");
            //아이디를 입력하고 엔터키를 누르면 생성된 소켓의 스트림을 통해 서버 데이터(아이디, 대화말 등)를 읽어들이고 클라이언트 화면창에 띄운다.
            while (true) {
                serverdata = input.readLine();
                if (serverdata != null) {
                    display.append(serverdata + "\r\n");
                    StringTokenizer st = new StringTokenizer(serverdata, SEPARATOR);
                    if (!st.hasMoreTokens()) continue;
                    int command = Integer.parseInt(st.nextToken());
                    switch (command) {
                        case REQ_LOGON:
                            ID = st.nextToken();
                            mlbl.setText(ID + "(으)로 로그인 하였습니다.");
                            ltext.setVisible(false);
                            break;
                        case REQ_LOGON_FAIL:
                            mlbl.setText("중복된 아이디입니다. 다른 아이디를 입력하세요.");
                            ID = null;
                            break;
                        default:
                            display.append(serverdata + "\r\n");
                            break;
                    }
                }
            }
            //            while (true) {
//                serverdata = input.readLine();
//                if (serverdata != null) {
//                    display.append(serverdata + "\r\n");
//                    if (serverdata.startsWith(String.valueOf(REQ_LOGON))) {
//                        mlbl.setText(ID + "(으)로 로그인 하였습니다.");
//                        ltext.setVisible(false);
//                    } else if (serverdata.startsWith(String.valueOf(REQ_LOGON_FAIL))) {
//                        mlbl.setText("중복된 아이디입니다. 다른 아이디를 입력하세요.");
//                        ID = null;
//                    }
//                }
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //4-1. 로그온 시 아이디를 입력하고 엔터키를 눌렀을 때 이벤트
    public void actionPerformed(ActionEvent ae) {
    	
            if (ID == null) {
                ID = ltext.getText();
                mlbl.setText(ID + "(으)로 로그인 하였습니다.");
                try {
                    clientdata.setLength(0);
                    clientdata.append(REQ_LOGON);
                    clientdata.append(SEPARATOR);
                    clientdata.append(ID);
                    //서버에서 전송받음
                    output.write(clientdata.toString() + "\r\n");
                    output.flush();
                    //최초 아이디 입력하고 전송 후 비활성화 처리
                    ltext.setVisible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }//actionPerformed()
    
    public static void main(String[] args) {
    	//3. 서버가 먼저 실행되어 있는 상태에서 클라이언트가 실행되면서 runClient() 실행.
        ChatWhisperClient c = new ChatWhisperClient();
        c.runClient();
    }

    //채팅창을 닫을 때 프로그램 종료
    class WinListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

	public void keyPressed(KeyEvent ke) {
		//대화말을 입력했을 때
		if(ke.getKeyChar() == KeyEvent.VK_ENTER) {
			//입력한 대화말 정보를 가져오고
			String message = wtext.getText();
//			message = message.replace(SEPARATOR, PIPE_REPLACEMENT);
			//한 칸 띄어쓰기를 구분자로 하여 st에 입력한 메시지를 저장
			StringTokenizer st = new StringTokenizer(message, " ");
			
			//아이디가 널값인경우 조건문 처리
			if (ID == null) {
				mlbl.setText("다시 로그인 하세요!!!");
				wtext.setText("");
			} else {
				try {
					//대화말에 /w가 있으면 조건문 처리
					//귓속말시 /w 상대아이디 대화말   => 이런 형식으로 전송됨
					if (st.nextToken().equals("/w")) {
						message = message.substring(3); // /w를 삭제하면, 상대아이디 대화말 => 이렇게 남게됨
						String WID = st.nextToken(); //상대아이디
						String Wmessage = st.nextToken(); //대화말
						while(st.hasMoreTokens()) { //다음에 읽어들일 token정보가 있으면 while 반복문 실행
							Wmessage = Wmessage + " " + st.nextToken();
						}
						clientdata.setLength(0);
						clientdata.append(REQ_WISPERSEND);
						clientdata.append(SEPARATOR);
						clientdata.append(ID);
						clientdata.append(SEPARATOR);
						clientdata.append(WID);
						clientdata.append(SEPARATOR);
						clientdata.append(Wmessage);
						output.write(clientdata.toString()+"\r\n");
						output.flush();
						wtext.setText("");
					} else {
						clientdata.setLength(0);
						clientdata.append(REQ_SENDWORDS);
						clientdata.append(SEPARATOR);
						clientdata.append(ID);
						clientdata.append(SEPARATOR);
						clientdata.append(message);
						output.write(clientdata.toString()+"\r\n");
						output.flush();
						wtext.setText("");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {
	}
    
	public void keyTyped(KeyEvent e) {
	}
    
    
}
