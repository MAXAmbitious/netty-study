package com.beidao.netty.demo.client;

import java.io.IOException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EchoClient {

	private final String host;
	private final int port;
	
	public EchoClient(String host, int port) {
		this.port = port;
		this.host = host;
	}
	
	public void start() throws InterruptedException, IOException {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(new EchoClientFilter());
        // 连接服务端
        Channel ch = bootstrap.connect(host, port).sync().channel();
        sendData(ch);
	}
	
	public static void sendData(Channel ch) throws IOException{
        String str="Hello Netty";
        ch.writeAndFlush(str+ "\r\n");
        System.out.println("sending data:"+str);
   }
	
	public static void main(String[] args) throws InterruptedException, IOException {
		new EchoClient("localhost", 5555).start();
	}

}
