package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UnsyncChatClient {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		DatagramSocket cs = new DatagramSocket(1234);
		InetAddress ip = InetAddress.getLocalHost();
		
		System.out.println("클라이언트 실행중...");
		System.out.println("클라이언트 작동중...");
		
		//1. 서버로 전송 할 채팅 작업 스레드
		Thread csend;
		csend = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Scanner sc = new Scanner(System.in);
					while(true) {
						synchronized (this) {
							byte[] sd = new byte[1000];
							
							sd = sc.nextLine().getBytes();
							DatagramPacket sp = new DatagramPacket(sd, sd.length, ip, 1234);
							
							cs.send(sp);
							
							String msg = new String(sd);
							System.out.println("Client: " + msg);
							
							//exit condition
							if ((msg).equals("q")) {
								System.out.println("클라이언트 종료중...");
								break;
							}
							System.out.println("서버 응답 기다리는중...");
						}
					}
				} catch (Exception e) {
					System.out.println("예외 발생!!!");
				}
			}
		});
		
		//2. 서버의 채팅을 받을 작업 스레드
		Thread creceive;
		creceive = new Thread(new Runnable() {			
			@Override
			public void run() {
				try {
					while (true) {
						synchronized (this) {
							byte[] rd = new byte[1000];
							
							DatagramPacket sp1 = new DatagramPacket(rd, rd.length);
							cs.receive(sp1);
							
							String msg = (new String(rd)).trim();
							System.out.println("Server (" + sp1.getPort() + "):" + " " + msg);
							
							//exit condition
							if ((msg).equals("q")) {
								System.out.println("서버 연결 종료...");
								break;
							}
						}
					}
				} catch (Exception e) {
					System.out.println("예외 발생!!!");
				}
			}
		});
		
		csend.start();
		creceive.start();
		
		//실행중인 쓰레드를 강제로 실행 대기(lock) 상태로 변하게 한 뒤 특정 쓰레드가 실행되고 종료 될 때까지 기다리게 할 수 있다.
		csend.join();
		creceive.join();
	}
}
