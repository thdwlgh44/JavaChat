package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class SendFile extends Frame implements ActionListener {

    private TextField tf_filename;
    private Button bt_dialog, bt_send, bt_close;
    private Label lb_status;

    private static final String SEPARATOR = "|";
    private String address;

    public SendFile(String address) {
        super("파일전송");
        this.address = address;

        setLayout(null);

        Label lbl = new Label("파일이름");
        lbl.setBounds(10, 30, 60 ,20);
        add(lbl);

        tf_filename = new TextField();
        tf_filename.setBounds(80, 30, 160, 20);
        add(tf_filename);

        bt_dialog = new Button("찾아보기");
        bt_dialog.setBounds(45, 60, 60, 20);
        bt_dialog.addActionListener(this);
        add(bt_dialog);

        bt_send = new Button("전송");
        bt_send.setBounds(115, 60, 40, 20);
        bt_send.addActionListener(this);
        add(bt_send);

        bt_close = new Button("종료");
        bt_close.setBounds(165, 60, 40, 20);
        bt_close.addActionListener(this);
        add(bt_close);

        lb_status = new Label("파일전송 대기중...");
        lb_status.setBounds(10, 90, 230, 20);
        lb_status.setBackground(Color.gray);
        lb_status.setForeground(Color.white);

        add(lb_status);

        addWindowListener(new WinListener() {
        });
        setSize(250, 130);
        show();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bt_dialog) {
            FileDialog fd = new FileDialog(this, "파일 찾기", FileDialog.LOAD);
            fd.show();
            tf_filename.setText(fd.getDirectory() + fd.getFile());
            if (tf_filename.getText().startsWith("null"))
                tf_filename.setText("");
        } else if (e.getSource() == bt_send) {
            String filename = tf_filename.getText();

            if (filename.equals("")) {
                lb_status.setText("파일이름을 입력하세요.");
                return;
            }

            lb_status.setText("파일검색중..");

            File file = new File(filename);

            if (!file.exists()) {
                lb_status.setText("해당파일을 찾을 수 없습니다.");
                return;
            }

            StringBuffer buffer = new StringBuffer();
            int fileLength = (int) file.length();

            buffer.append(file.getName());
            buffer.append(SEPARATOR);
            buffer.append(fileLength);

            lb_status.setText("연결설정중.....");

            try {
                Socket sock = new Socket(address, 3777);
                FileInputStream fin = new FileInputStream(file);
                BufferedInputStream bin = new BufferedInputStream(fin, fileLength);
                byte data[] = new byte[fileLength];
                try {
                    lb_status.setText("전송할 파일 로드중.....");
                    bin.read(data, 0, fileLength);
                    bin.close();
                } catch (IOException err) {
                    lb_status.setText("파일읽기 오류.");
                    return;
                }

                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                out.writeUTF(buffer.toString());

                tf_filename.setText("");
                lb_status.setText("파일전송중......( 0 Byte)");
                BufferedOutputStream bout = new BufferedOutputStream(out, 2048);
                DataInputStream din = new DataInputStream(sock.getInputStream());
                sendFile(bout, din, data, fileLength);
                bout.close();
                din.close();

                lb_status.setText(file.getName() + " 파일전송이 완료되었습니다.");
                sock.close();
            } catch (IOException e1) {
                System.out.println(e1);
                lb_status.setText(address + "로의 연결에 실패하였습니다.");
            }
        } else if (e.getSource() == bt_close) {
            dispose();
        }
    }

    private void sendFile(BufferedOutputStream bout, DataInputStream din, byte[] data, int fileLength) throws IOException {
        int size = 2048;
        int count = fileLength/size;
        int rest = fileLength%size;
        int flag = 1;

        if (count == 0) flag = 0;

        for (int i=0; i<=count; i++) {
            if (i == count && flag == 0) {
                bout.write(data, 0, rest);
                bout.flush();
                return;
            } else if (i == count) {
                bout.write(data, i*size, rest);
                bout.flush();
                return;
            } else {
                bout.write(data, i*size, size);
                bout.flush();
                lb_status.setText("파일전송중......(" + ((i+1)*size) + "/" + fileLength + " Byte)");
                din.readUTF();
            }
        }
    }

    class WinListener extends WindowAdapter {
        public void windowClosing(WindowEvent we) {
            System.exit(0);
        }
    }

}
