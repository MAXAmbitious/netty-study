package com.beidao.netty.dubbo.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.beidao.netty.dubbo.facade.api.DubboRequest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * dubbo消费者拦截器
 * @author 0200759
 *
 */
public class DubboConsumerHandler implements InvocationHandler{

	private Object res;
	
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
				
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
					ch.pipeline().addLast(new ObjectEncoder());
					ch.pipeline().addLast(new ConsumerHandler(proxy, method, args));
				}
			});
			//从注册中心获取服务端ip和端口
			ChannelFuture f = bootstrap.connect("127.0.0.1", 8080).sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
		return res;
	}
	
	/**
	 * 
	 * netty-dubbo消费者拦截器
	 * @author 0200759
	 *
	 */
	private class ConsumerHandler extends ChannelInboundHandlerAdapter{
		private Object proxy;
		private Method method;
		private Object[] args;
		
		public ConsumerHandler(Object proxy, Method method, Object[] args) {
			this.proxy = proxy;
			this.args = args;
			this.method = method;
		}
		
		public void channelActive(ChannelHandlerContext ctx) {
			 // 传输的对象必须实现序列化接口 包括其中的属性
			DubboRequest req = new DubboRequest(proxy.getClass().getInterfaces()[0], method.getName(), method.getParameterTypes(), args);
            ctx.write(req);
            ctx.flush();
		}
		
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("调用成功");
            res = msg;
            ctx.flush();
            //收到响应后断开连接
            ctx.close();
        }
		
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
	}

}
