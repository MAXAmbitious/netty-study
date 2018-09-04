package com.beidao.netty.io;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.oio.OioSctpServerChannel;
import io.netty.util.CharsetUtil;

public class NettyOioServer {

	public void server(int port) throws Exception {
		final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n",CharsetUtil.UTF_8));
		//事件循环组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			//用来引导服务器配置
			ServerBootstrap bootstrap = new ServerBootstrap();
			//使用OIO阻塞模式
			bootstrap.group(group).channel(OioSctpServerChannel.class).localAddress(new InetSocketAddress(port))
			//指定ChannelInitialer初始化handlers
				.childHandler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel ch) throws Exception {
						//添加一个“入站”handle到ChannelPipline
						ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
							public void channelActive(ChannelHandlerContext ctx) throws Exception{
								//连接后，写消息到客户端，写完后关闭连接
								ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
							}
						});
					}
					
				});
			//绑定服务器接受连接
			ChannelFuture future = bootstrap.bind().sync();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			//释放所有资源
			group.shutdownGracefully();
		}
	}
}
