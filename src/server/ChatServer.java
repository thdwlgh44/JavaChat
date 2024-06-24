package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    public static final int cs_port = 2777; //서버 포트번호
    public static final int cs_maxclient = 50;  //클라이언트 최대 가입자수

    public static void main(String[] args) {
        try {
        	//1. ServerSocket 생성하고 Client의 접속을 기다린다.
            ServerSocket ss_socket = new ServerSocket(cs_port);
            System.out.println("서버소켓 실행: 클라이언트의 접속을 기다립니다.");
            while (true) {
                Socket sock = null;
                ServerThread client = null;
                try {
                    sock = ss_socket.accept(); //접속요청을 기다린다.
                    client = new ServerThread(sock);
                    client.start(); //스레드를 실행시킨다.
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
            }
        } catch (IOException e) {}
    }
}
