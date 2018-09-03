package com.beidao.netty.demo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {
	
	private int port;
	
	public EchoServer(int port) {
		this.port = port;
	}
	
	public void start() throws Exception{
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			
			bootstrap.group(group);
			bootstrap.channel(NioServerSocketChannel.class);
			//设置过滤器
			bootstrap.childHandler(new EchoServerFilter()); 
            // 服务器绑定端口监听
            ChannelFuture f = bootstrap.bind(port).sync();
            System.out.println(EchoServer.class.getName() + "started and listen on" + f.channel().localAddress());
            // 监听服务器关闭监听
            f.channel().closeFuture().sync();
			
		} finally {
			group.shutdownGracefully().sync();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new EchoServer(5555).start();

	}

}
