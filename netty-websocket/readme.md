# WebSocket简介

WebSocket协议支持（在受控环境中运行不受信任的代码的）客户端与（选择加入该代码的通信的）远程主机之间进行全双工通信。用于此的安全模型是Web浏览器常用的基于原始的安全模式。 协议包括一个开放的握手以及随后的TCP层上的消息帧。 该技术的目标是为基于浏览器的、需要和服务器进行双向通信的（服务器不能依赖于打开多个HTTP连接（例如，使用XMLHttpRequest或<iframe>和长轮询））应用程序提供一种通信机制。

在实现websocket连线过程中，需要通过浏览器发出websocket连线请求，然后服务器发出回应，这个过程通常称为“握手” 。在 WebSocket API，浏览器和服务器只需要做一个握手的动作，然后，浏览器和服务器之间就形成了一条快速通道。两者之间就直接可以数据互相传送。

# Netty—WebSocket demo

**整体架构图**

![image](https://note.youdao.com/yws/api/personal/file/48209B98A71743B6B7E43EF23A19781C?method=download&shareKey=96f6b48a7e4fa8fb7bf3bfc93a632ab1)

**自定义拦截器—HttpRequestHandler**

```
package com.beidao.netty.websocket.handler;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private final String wsUri;
	private static final File INDEX;
	static {
		URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
		try {
			String path = location.toURI() + "index.html";
			path = !path.contains("file:") ? path : path.substring(5);
			INDEX = new File(path);
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Unable to locate index.html", e);
		}
	}

	public HttpRequestHandler(String wsUri) {
		this.wsUri = wsUri;
	}

	@SuppressWarnings("resource")
	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (wsUri.equalsIgnoreCase(request.uri())) {
			ctx.fireChannelRead(request.retain());
		} else {
			if (HttpUtil.is100ContinueExpected(request)) {
				send100Continue(ctx);
			}
			RandomAccessFile file = new RandomAccessFile(INDEX, "r");
			HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
			boolean keepAlive = HttpUtil.isKeepAlive(request);
			if (keepAlive) {
				response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}
			ctx.write(response);
			if (ctx.pipeline().get(SslHandler.class) == null) {
				ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
			} else {
				ctx.write(new ChunkedNioFile(file.getChannel()));
			}
			ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			if (!keepAlive) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		}
	}

	private static void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
		ctx.writeAndFlush(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
```
**自定义拦截器—TextWebSocketFrameHandler**

```
package com.beidao.netty.websocket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	private final ChannelGroup group;

	public TextWebSocketFrameHandler(ChannelGroup group) {
		this.group = group;
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
			ctx.pipeline().remove(HttpRequestHandler.class);
			group.writeAndFlush(new TextWebSocketFrame("Client " + ctx.channel() + " joined"));
			group.add(ctx.channel());
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		group.writeAndFlush(msg.retain());
	}
}

```
**服务器端初始化连接**

```
package com.beidao.netty.websocket.initializer;

import com.beidao.netty.websocket.handler.HttpRequestHandler;
import com.beidao.netty.websocket.handler.TextWebSocketFrameHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ChatServerInitializer extends ChannelInitializer<Channel> {
	private final ChannelGroup group;

	public ChatServerInitializer(ChannelGroup group) {
		this.group = group;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new ChunkedWriteHandler());
		pipeline.addLast(new HttpObjectAggregator(64 * 1024));
		pipeline.addLast(new HttpRequestHandler("/ws"));
		pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
		pipeline.addLast(new TextWebSocketFrameHandler(group));
	}
}

```
**服务器**

```
package com.beidao.netty.websocket.bootstrap;

import java.net.InetSocketAddress;

import com.beidao.netty.websocket.initializer.ChatServerInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

public class ChatServer {
	private final ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
	private final EventLoopGroup group = new NioEventLoopGroup();
	private Channel channel;

	public ChannelFuture start(InetSocketAddress address) {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(group).channel(NioServerSocketChannel.class).childHandler(createInitializer(channelGroup));
		ChannelFuture future = bootstrap.bind(address);
		future.syncUninterruptibly();
		channel = future.channel();
		return future;
	}

	protected ChannelInitializer<Channel> createInitializer(ChannelGroup group) {
		return new ChatServerInitializer(group);
	}

	public void destroy() {
		if (channel != null) {
			channel.close();
		}
		channelGroup.close();
		group.shutdownGracefully();
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Please give port as argument");
			System.exit(1);
		}
//		int port = Integer.parseInt(args[0]);
		final ChatServer endpoint = new ChatServer();
		ChannelFuture future = endpoint.start(new InetSocketAddress(1111));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				endpoint.destroy();
			}
		});
		future.channel().closeFuture().syncUninterruptibly();
	}
}

```
**WebSocket客户端页面index.html**

*注意WebSocket端口和服务端保持一致*
```
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
    <title>WebSocket客户端</title>
</head>
<body>
<script type="text/javascript">
    var socket;

    //如果浏览器支持WebSocket
    if(window.WebSocket){
        //参数就是与服务器连接的地址
        socket = new WebSocket("ws://localhost:1111/ws");

        //客户端收到服务器消息的时候就会执行这个回调方法
        socket.onmessage = function (event) {
            var ta = document.getElementById("responseText");
            ta.value = ta.value + "\n"+event.data;
        }

        //连接建立的回调函数
        socket.onopen = function(event){
            var ta = document.getElementById("responseText");
            ta.value = "连接开启";
        }

        //连接断掉的回调函数
        socket.onclose = function (event) {
            var ta = document.getElementById("responseText");
            ta.value = ta.value +"\n"+"连接关闭";
        }
    }else{
        alert("浏览器不支持WebSocket！");
    }

    //发送数据
    function send(message){
        if(!window.WebSocket){
            return;
        }

        //当websocket状态打开
        if(socket.readyState == WebSocket.OPEN){
            socket.send(message);
        }else{
            alert("连接没有开启");
        }
    }
</script>
<form onsubmit="return false">
    <textarea name = "message" style="width: 400px;height: 200px"></textarea>

    <input type ="button" value="发送数据" onclick="send(this.form.message.value);">

    <h3>服务器输出：</h3>

    <textarea id ="responseText" style="width: 400px;height: 300px;"></textarea>

    <input type="button" onclick="javascript:document.getElementById('responseText').value=''" value="清空数据">
</form>
</body>
</html>
```
# 验证
1、运行服务端ChatServer main函数
2、运行websocket客户端index.html(用谷歌等浏览器打开即可)
![image](https://note.youdao.com/yws/api/personal/file/871DB604A35F47D8B90BF258A4360FA4?method=download&shareKey=e7a201398915702eb294a4fcd4f1bd42)


