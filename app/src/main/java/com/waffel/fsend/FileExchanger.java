package com.waffel.fsend;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.blogspot.debukkitsblog.net.Datapackage;

import com.waffel.fsend.utils.Client;
import com.waffel.fsend.utils.Server;

public class FileExchanger {
	
	public static final String IP_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)(?::\\d{1,5})?$";

	private static Server server = null;
	private static Client client = null;

	private static Context context;

	private static final int port = 44444;
	
	/**
	 * Init the Server and Client.
	 */
	public static void init(String outputDir, Context context) {
		server = new Server(port, true, outputDir);
		client = new Client();
		FileExchanger.context = context;
	}

	public static Context getContext() {
		return context;
	}

	/**
	 * Sends a file to the specified IP-Address
	 * @param path The path to the file or directory to send
	 * @return A boolean resembling whether sending the file was successful
	 */
	public static boolean sendFile(String path, String ip) throws IOException {
		if(!Pattern.matches(IP_PATTERN, ip))
			throw new IllegalArgumentException("Invalid ip formatting");

		client.start(ip, port, 3000, false);
		boolean ret = client.sendFile(new File(path));
		client.stop();

		return ret;
	}

	public static boolean sendIS(InputStream is, String filename, String ip) throws IOException {
		if(!Pattern.matches(IP_PATTERN, ip))
			throw new IllegalArgumentException("Invalid ip formatting");

		client.start(ip, port, 5000, false);
		boolean ret = client.sendIS(is, filename);
		client.stop();

		return ret;
	}


	public static List<String[]> queryIPs() {
		String ip;
		List<String[]> ips = new ArrayList<>();
		for(int i = 1; i < 255; i++) {
			System.out.println(i);
			ip = "192.168.178." + i;
			client.start(ip, port, 100, true);
			String[] ipAndHost = new String[2];
			ipAndHost[0] = client.ping();
			ipAndHost[1] = ip;
			ips.add(ipAndHost);
		}
		return ips;
	}

}
