package com.beidao.netty.javaio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class PlainNioServer {

	public void server(int port) throws Exception {
		System.out.println("Listening for connections on port " + port);
		//open Selector that handles channels
		Selector selector = Selector.open();
		//open ServerSocketChannel
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		//get ServerSocket
		ServerSocket serverSocket = serverChannel.socket();
		//bind server to port
		serverSocket.bind(new InetSocketAddress(port));
		//set to non-blocking
		serverChannel.configureBlocking(false);
		//register ServerSocket to selector and specify that it is interested in new accepted clients
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
		while (true) {
			//Wait for new events that are ready for process. This will block until something happens
			int n = selector.select();
			if(n > 0){
				//Obtain all SelectionKey instances that received events
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();
					try {
						//Check if event was because new client ready to get accepted
						if (key.isAcceptable()) {
							ServerSocketChannel server = (ServerSocketChannel) key.channel();
							SocketChannel client = server.accept();
							System.out.println("Accepted connection from "+ client);
							client.configureBlocking(false);
							//Accept client and register it to selector
							client.register(selector, SelectionKey.OP_WRITE,msg.duplicate());
						}
						
						//Check if event was because socket is ready to write data
						if (key.isWritable()) {
							SocketChannel client = (SocketChannel) key.channel();
							ByteBuffer buff = (ByteBuffer) key.attachment();
							//write data to connected client
							while (buff.hasRemaining()) {
								if (client.write(buff) == 0) {
									break;
								}
							}
							client.close();
						}
					} catch (Exception e) {
						key.cancel();
						key.channel().close();
					}
				}
			}
		}
	}
}
