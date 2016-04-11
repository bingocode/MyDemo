package com.example.zb.mydemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
	private static final String HOSTNMAE_LAB="192.168.1.128";
    private static final String HOSTNMAE_DOM="10.131.245.201";
    private static final int PORT=10900;
    private OutputStream os;
    private Handler handler;
    private String idd="1";
	private ListView msgListView;

	private EditText inputText;

	private Button send;
	
	private MsgAdapter adapter;

	private List<Msg> msgList = new ArrayList<Msg>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		Intent intent=getIntent();
		idd=intent.getStringExtra("id");
		initMsgs();
		adapter = new MsgAdapter(MainActivity.this, R.layout.msg_item, msgList);
		inputText = (EditText) findViewById(R.id.input_text);
		send = (Button) findViewById(R.id.send);
		msgListView = (ListView) findViewById(R.id.msg_list_view);
		msgListView.setAdapter(adapter);
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = inputText.getText().toString();
				if (!("".equals(content)||content==null)) {
					try {
						os.write((idd+":"+content + "\r\n").getBytes());
						inputText.setText("");
					} catch (Exception e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}

                    // ���input�ı�������
					
				}
			}
		});
		handler = new Handler(){
            public void handleMessage(Message msg) {
                // �����Ϣ�������߳�
                if (msg.what == 0x234) {
                    // ����ȡ������׷����ʾ���ı�����
                    //�����Լ��ͱ��˷�����Ϣ�ж���ȷ����Ϣ��
                String c=msg.obj.toString();
                	 String[] nc = c.split(":");
                	 Msg mssg;
                	 String c2="";
                	 for(int i=1;i<nc.length;i++)
                		 if(i!=nc.length-1)
                		 c2+=nc[i]+":";
                		 else
                			 c2+=nc[i];
                	 if(nc[0].equals(idd))
                    mssg = new Msg(nc[0],c2, Msg.TYPE_SENT);
                	 else
                	 mssg = new Msg(nc[0],c2, Msg.TYPE_RECEIVED);
					msgList.add(mssg);
					adapter.notifyDataSetChanged();
					msgListView.setSelection(msgList.size());
                }
            }
        };
        new Thread(networkTask).start();
	}
	 Runnable networkTask = new Runnable() {
	        public void run() {
	            try {
	                    Socket socket;
	                socket = new Socket(HOSTNMAE_DOM, PORT);
	                // �ͻ�������ClientThread�̲߳��϶�ȡ���Է�����������
	                new Thread(new ClientThread(socket, handler)).start();
	                os = socket.getOutputStream();

	            } catch (Exception e) {
	                e.printStackTrace();

	            }
	        }
	    };
	private void initMsgs() {
		Msg msg1 = new Msg(idd,"Hello guy.", Msg.TYPE_RECEIVED);
		msgList.add(msg1);
		Msg msg2 = new Msg(idd,"Hello. Who is that?", Msg.TYPE_SENT);
		msgList.add(msg2);
		Msg msg3 = new Msg(idd,"This is Tom. Nice talking to you. ", Msg.TYPE_RECEIVED);
		msgList.add(msg3);
	}

		class ClientThread implements Runnable {
	        private Handler handler1;
	        // ���߳��������Socket����Ӧ��������
	        private BufferedReader br = null;

	        public ClientThread(Socket socket, Handler handler) throws IOException {
	            this.handler1 = handler;
	            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        }

	        @Override
	        public void run() {
	            try {
	                String content = null;
	                // ���϶�ȡSocket������������
	                while ((content = br.readLine()) != null) {
	                    // ÿ���������Է�����������֮�󣬷�����Ϣ֪ͨ���������ʾ������
	                    Message msg = new Message();
	                    msg.what = 0x234;
	                    msg.obj = content;
	                    handler1.sendMessage(msg);
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }

	    }
}
