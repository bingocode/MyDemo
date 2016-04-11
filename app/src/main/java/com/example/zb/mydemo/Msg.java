package com.example.zb.mydemo;

public class Msg {

	public static final int TYPE_RECEIVED = 0;

	public static final int TYPE_SENT = 1;

	private String content;
	private String id;

	private int type;

	public Msg(String id,String content, int type) {
		this.content = content;
		this.type = type;
		this.id=id;
	}
    public String getId(){
    return id;
    }
	public String getContent() {
		return content;
	}

	public int getType() {
		return type;
	}

}
