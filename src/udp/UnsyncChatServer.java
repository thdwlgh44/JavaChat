package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

//TCP방식보다 빠르지만 안정성이 떨어진다.
public class UnsyncChatServer {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		DatagramSocket ss = new DatagramSocket(5334);
		InetAddress ip = InetAddress.getLocalHost();
		
		System.out.println("서버 실행중...");
		System.out.println("서버 작동중...");
		
		//1. 서버에서 클라이언트로 전송하는 채팅
		Thread ssend;
		ssend = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Scanner sc = new Scanner(System.in);
					while(true) {
						//자원에 대한 접근을 순차적으로 할 수 있도록 한다.
						synchronized (this) {
							byte[] sd = new byte[1000];
							
							sd = sc.nextLine().getBytes();
							DatagramPacket sp = new DatagramPacket(sd, sd.length, ip, 5334);
							
							//DatagramSocket에 packet을 담아 보낸다.
							ss.send(sp);
							
							String msg = new String(sd);
							System.out.println("Server: " + msg);
							
							//exit condition
							if ((msg).equals("q")) {
								System.out.println("서버 종료중...");
								break;
							}
							System.out.println("클라이언트 응답 기다리는중...");
						}
					}
				} catch (Exception e) {
					System.out.println("예외 발생!!!");
				}
			}
		});
		
		//2. 클라이언트에서 전송한 채팅 받는 작업 스레드
		Thread sreceive;
		sreceive = new Thread(new Runnable() {			
			@Override
			public void run() {
				try {
					while (true) {
						//자원에 대한 접근을 순차적으로 할 수 있도록 한다.
						synchronized (this) {
							byte[] rd = new byte[1000];
							
							DatagramPacket sp1 = new DatagramPacket(rd, rd.length);
							ss.receive(sp1);
							
							String msg = (new String(rd)).trim();
							System.out.println("Client (" + sp1.getPort() + "):" + " " + msg);
							
							//exit condition
							if ((msg).equals("q")) {
								System.out.println("클라이언트 연결 종료...");
								break;
							}
						}
					}
				} catch (Exception e) {
					System.out.println("예외 발생!!!");
				}
			}
		});
		
		ssend.start();
		sreceive.start();
		
		//실행중인 쓰레드를 강제로 실행 대기(lock) 상태로 변하게 한 뒤 특정 쓰레드가 실행되고 종료 될 때까지 기다리게 할 수 있다.
		ssend.join();
		sreceive.join();
	}
}
