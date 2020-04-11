package com.waffel.fsend.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;

import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Executable;
import com.waffel.fsend.FileExchanger;

public class Client {

	private com.blogspot.debukkitsblog.net.Client client;
	
	private void registerMethods() {
		client.registerMethod("/error", new Executable() {
			@Override
			public void run(Datapackage pack, Socket socket) {
				if (pack.size() < 3)
					return;
				String errorMessage = (String) pack.get(1);
				Exception e = (Exception) pack.get(2);
				System.err.println("Error occurred on target machine:\n");
				System.err.println(errorMessage + ":\n");
				e.printStackTrace();
				client = null;
			}
		});
		client.registerMethod("/success", new Executable() {
			@Override
			public void run(Datapackage pack, Socket socket) {
				client = null;
			}
		});
	}

	public void start(String ip, int port, int timeout, boolean ping) {
		if(client != null) {
			client.stop();
		}
		client = new com.blogspot.debukkitsblog.net.Client(ip, port, timeout);
		client.setMuted(true);

		if(ping)
			return;

		registerMethods();
		client.start();
	}

	public void stop() {
		if(client != null)
			client.stop();
		client = null;
	}

	public Datapackage sendMessage(Datapackage message) {
		return client.sendMessage(message);
	}

	public Datapackage sendMessage(Datapackage message, int timeout) {
		return client.sendMessage(message, timeout);
	}

	public Datapackage sendMessage(String ID, Object... content) {
		return client.sendMessage(ID, content);
	}

	public boolean sendFile(File file) throws IOException {
		if(!file.exists())
			return false;
		return this.sendBytes(file.getName(), Files.readAllBytes(file.toPath())).get(0).equals("/success");
	}

	public boolean sendIS(InputStream is, String filename) throws IOException {
		byte[] bytes = new byte[is.available()];
		is.read(bytes);
		return this.sendBytes(filename, bytes).get(0).equals("/success");
	}
	
	public Datapackage sendBytes(String filename, byte[] bytes) {
		return this.sendMessage("/filetransfer", filename, bytes);
	}

	public String ping() {
		Datapackage dp = this.sendMessage("/ping");
		if(dp != null && dp.get(0).equals("/pong"))
			return (String) dp.get(1);
		return null;
	}
}
