# 前言
Dubbo是基于Netty搭建的RPC框架，为了更好地理解Netty在Dubbo中的应用，仿照Dubbo搭建了一个简易版的RPC框架。

# 概述
**整个调用逻辑如下：**
1、生产者服务端启动Netty服务端。
2、消费者客户端通过JDK动态代理启动Netty客户端，通过注册中心地址连接生产者服务端，同时将接口调用信息（接口、方法、参数等）先序列化再发送给生产者服务端。
3、生产者服务端接收消息并通过反射调用相应方法，然后返回调用结果给消费者。
4、消费者接收生产者传来的调用结果。


# 实现
**新建DubboRequest类(相当于POJO)，作为消息载体**

```
package com.beidao.netty.dubbo.facade.api;

import java.io.Serializable;
import java.util.Arrays;

/**
 * dubbo请求类
 * @author 0200759
 *
 */
public class DubboRequest implements Serializable{

	private static final long serialVersionUID = 422805234202183587L;
    private Class<?> interfaceClass;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] args;
    
    public DubboRequest(Class<?> interfaceClass, String methodName, Class<?>[] paramTypes, Object[] args) {

        this.interfaceClass = interfaceClass;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.args = args;
    }

    public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(Class<?>[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	@Override
    public String toString() {
        return "DubboRequest{" +
                "interfaceClass=" + interfaceClass +
                ", methodName='" + methodName + '\'' +
                ", paramTypes=" + Arrays.toString(paramTypes) +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}

```
**新建Dubbo消费者调用接口IUserFacade**

```
package com.beidao.netty.dubbo.facade.api;

/**
 * dubbo api接口
 * @author 0200759
 *
 */
public interface IUserFacade {

	/**
	 * 返回用户名接口
	 * @param string 
	 * @return
	 */
	public String getUserName(Long id);

}

```
**新建Dubbo消费者拦截器**

```
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

```
**新建Dubbo服务代理类**

```
package com.beidao.netty.dubbo.client;

import java.lang.reflect.Proxy;

/**
 * Dubbo代理类
 * @author 0200759
 *
 */
public class DubboProxy {

	public static Object getProxyInstance(Class<?> clazz) {
		return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new DubboConsumerHandler());
	}
}

```
**新建Dubbo消费者**

```
package com.beidao.netty.dubbo.client;

import com.beidao.netty.dubbo.facade.api.IUserFacade;

/**
 * dubbo客户端(消费者)
 * @author 0200759
 *
 */
public class DubboClient {
 
	public static void main(String[] args){
		IUserFacade userFacade = (IUserFacade) DubboProxy.getProxyInstance(IUserFacade.class);
		
		System.out.println(userFacade.getUserName(520L));
		System.out.println(userFacade.getUserName(1314L));
		System.out.println(userFacade.getUserName(1314520L));
	}
}

```
**新建 dubbo服务端实现类**

```
package com.beidao.netty.dubbo.facade.impl;

import com.beidao.netty.dubbo.facade.api.IUserFacade;

/**
 * dubbo服务端实现类
 * @author 0200759
 *
 */
public class UserFacade implements IUserFacade {

	public String getUserName(Long id) {
		
		return "I love you, "+id;
	}

}

```
**dubbo生产者拦截器**

```
package com.beidao.netty.dubbo.sever;

import java.lang.reflect.Method;

import com.beidao.netty.dubbo.facade.api.DubboRequest;
import com.beidao.netty.dubbo.facade.api.IUserFacade;
import com.beidao.netty.dubbo.facade.impl.UserFacade;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * netty-dubbo服务端拦截器
 * @author 0200759
 *
 */
public class DubboServerHandler extends ChannelInboundHandlerAdapter {

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务端收到消息:   " + msg);
        DubboRequest req = (DubboRequest) msg;
        // 1. 根据类名返回对象
        Object target = this.getInstenceByInterfaceClass(req.getInterfaceClass());
        // 2. 获取方法名
        String methodName = req.getMethodName();
        // 3. 获取方法参数类型
        // 4. 获取方法
        Method method = target.getClass().getMethod(methodName, req.getParamTypes());
        // 5. 获取参数值
        //调用方法 获取返回值
        Object res = method.invoke(target, req.getArgs());
        // 写回给调用端
        ctx.writeAndFlush(res);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 根据接口返回对应的实例
     * @param clazz
     * @return
     */
    private Object getInstenceByInterfaceClass(Class<?> clazz) {
        if (IUserFacade.class.equals(clazz)) {
            return new UserFacade();
        }
        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}

```

**Dubbo生产者服务端**

```
package com.beidao.netty.dubbo.sever;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author 0200759
 *
 */
public class DubboServer {

	private int port;

    public DubboServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); 
        try {
            ServerBootstrap b = new ServerBootstrap(); 
            b.group(bossGroup)
                    .channel(NioServerSocketChannel.class) 
                    .childHandler(new ChannelInitializer<SocketChannel>() { 
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectDecoder(1024*1024, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new DubboServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new DubboServer(8080).run();
    }

}

```
# 验证
1、启动生产者服务端DubboServer。
2、启动消费者调用端DubboClient。
消费者客户端控制台显示如下：

```
调用成功
I love you, 520
调用成功
I love you, 1314
调用成功
I love you, 1314520
```
生产者服务端控制台显示如下：

```
服务端收到消息:   DubboRequest{interfaceClass=interface com.beidao.netty.dubbo.facade.api.IUserFacade, methodName='getUserName', paramTypes=[class java.lang.Long], args=[520]}
服务端收到消息:   DubboRequest{interfaceClass=interface com.beidao.netty.dubbo.facade.api.IUserFacade, methodName='getUserName', paramTypes=[class java.lang.Long], args=[1314]}
服务端收到消息:   DubboRequest{interfaceClass=interface com.beidao.netty.dubbo.facade.api.IUserFacade, methodName='getUserName', paramTypes=[class java.lang.Long], args=[1314520]}

```

**源码地址：https://github.com/MAXAmbitious/netty-study/tree/master/netty-dubbo**
