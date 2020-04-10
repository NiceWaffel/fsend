package com.waffel.fsend.utils;

import android.content.Intent;
import android.provider.MediaStore;

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
import com.waffel.fsend.FileExchanger;
import com.waffel.fsend.MainActivity;

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

				String filename = (String) pack.get(1);
				byte[] file_contents = (byte[]) pack.get(2);

				((MainActivity) FileExchanger.getContext()).writeFile(filename, file_contents);

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
