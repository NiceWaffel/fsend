package com.waffel.fsend.utils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;

import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Executable;

public class Server extends com.blogspot.debukkitsblog.net.Server {
	private final String outputDir;
	private static String lastMessageHostAddress = null;
	private static int lastMessagePort = -1;
	
	@Deprecated
	public Server(int port) {
		super(port);
		this.outputDir = null;
	}
	
	@Deprecated
	public Server(int port, String outputDir) {
		super(port);
		this.outputDir = outputDir;
	}
	
	public Server(int port, boolean muted) {
		super(port, muted);
		this.outputDir = null;
	}
	
	public Server(int port, boolean muted, String outputDir) {
		super(port, muted);
		this.outputDir = outputDir;
	}
	
	@Deprecated
	public Server(int port, boolean autoRegisterEveryClient, boolean keepConnectionAlive, boolean useSSL) {
		super(port, autoRegisterEveryClient, keepConnectionAlive, useSSL);
		this.outputDir = null;
	}
	
	@Deprecated
	public Server(int port, boolean autoRegisterEveryClient, boolean keepConnectionAlive, boolean useSSL, String outputDir) {
		super(port, autoRegisterEveryClient, keepConnectionAlive, useSSL);
		this.outputDir = outputDir;
	}
	
	public Server(int port, boolean autoRegisterEveryClient, boolean keepConnectionAlive, boolean useSSL, boolean muted) {
		super(port, autoRegisterEveryClient, keepConnectionAlive, useSSL, muted);
		this.outputDir = null;
	}
	
	public Server(int port, boolean autoRegisterEveryClient, boolean keepConnectionAlive, boolean useSSL, boolean muted, String outputDir) {
		super(port, autoRegisterEveryClient, keepConnectionAlive, useSSL, muted);
		this.outputDir = outputDir;
	}
	
	@Override
	public void preStart() {
		this.printIp();
		this.registerMethod("/filetransfer", new Executable() {
			@Override
			public void run(Datapackage pack, Socket socket) {
				sendReply(socket, "Received");

				String filename = ((String) pack.get(1));
				String outputDir = Server.this.outputDir;
				if (outputDir.contains("\\") && File.separatorChar != '\\')
					outputDir = outputDir.replace('\\', '/');
				else if (outputDir.contains("/") && File.separatorChar != '/')
					outputDir = outputDir.replace('/', '\\');
				if (!outputDir.endsWith(File.separator))
					outputDir += File.separator;
				File file = new File(outputDir + filename);
				try {
					String tempPath = file.getAbsolutePath();
					tempPath = tempPath.substring(0, tempPath.lastIndexOf(File.separatorChar));
					new File(tempPath).mkdirs();
					Files.write(file.toPath(), (byte[]) pack.get(2));
				} catch (IOException e) {
					e.printStackTrace();
					Server.this.sendMessage(new RemoteClient("_DEFAULT_ID_", socket), "/error", "Error Saving File", e);
					return;
				}

				Server.this.sendMessage(new RemoteClient("_DEFAULT_ID_", socket), "/success");
			}
		});
		this.registerMethod("/message", new Executable() {
			@Override
			public void run(Datapackage pack, Socket socket) {
				Server.lastMessageHostAddress = socket.getInetAddress().getHostAddress();
				Server.lastMessagePort = socket.getLocalPort();
				sendReply(socket, "Received");
			}
		});
	}
	
	private void printIp() {
		try(final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
		} catch (SocketException | UnknownHostException e) {
			System.err.println("Error getting local IP:\n");
			e.printStackTrace();
		}
	}
	
	public static String getLastMessageHostAddress() {
		return Server.lastMessageHostAddress;
	}
	
	public static int getLastMessagePort() {
		return Server.lastMessagePort;
	}
}
