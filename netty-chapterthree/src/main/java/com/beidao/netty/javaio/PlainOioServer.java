package com.beidao.netty.javaio;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Blocking networking without Netty
 * @author 0200759
 *
 */
public class PlainOioServer {
	public void server(int port) throws Exception {
		final ServerSocket socket = new ServerSocket(port);
		try{
			while(true){
				//accept connection
				final Socket clientSocket = socket.accept();
				System.out.println("Accepted connection from" + clientSocket);
				//create new thread to handle connection
				new Thread(new Runnable() {
					
					public void run() {
						OutputStream out;
						try {
							out = clientSocket.getOutputStream();
							out.write("Hi\r\n".getBytes(Charset.forName("UTF-8")));
							out.flush();
							//close connection once message written and flushed
							clientSocket.close();
						} catch (Exception e) {
							try {
								clientSocket.close();
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						}
					}
				}).start();
			}
		}catch (Exception e) {
			e.printStackTrace();
			socket.close();
		}
	}
}
