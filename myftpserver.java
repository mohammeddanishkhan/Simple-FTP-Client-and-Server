/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author susha
 */
public class myftpserver {
	static int portNo;
	

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ServerSocket server;

		System.out.println("Enter the port number");
		Scanner sca = new Scanner(System.in);
		portNo = sca.nextInt();
		sca.close();
		server = new ServerSocket(portNo);
		
		System.out.println("The Server is started at the Port : " + portNo);

		while (true) {
			System.out.println("Waiting for connection");
			serverCommands s = new serverCommands(server.accept());
		}
	}
}

class serverCommands extends Thread {
	Socket clientSocket;
	DataInputStream inputStream;
	DataOutputStream outputStream;
	String commandString;
	String function;
	String path;
	static String initialDirectory=System.getProperty("user.dir");
	serverCommands(Socket serverSoc) {
		try {
			clientSocket = serverSoc;
			inputStream = new DataInputStream(clientSocket.getInputStream());
			outputStream = new DataOutputStream(clientSocket.getOutputStream());

			System.out.println("The Client has been connected");

			do {
				String[] cmdlist;
				commandString = inputStream.readUTF();
				cmdlist = commandString.split(" ", 2);
				function = cmdlist[0];
				if (cmdlist.length > 1) {
					path = cmdlist[1];
				}
				switch (function) {
				case "pwd":
					pwd();
					break;
				case "ls":
					ls();
					break;
				case "cd":
					cd(path);
					break;
				case "delete":
					delete(path);
					break;
				case "mkdir":
					mkdir(path);
					break;
				case "get":
					get(path);
					break;
				case "put":
					put(path);
					break;
				case "quit":
					quit();
					break;
				}
			} while (!function.equals("quit"));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void pwd() throws Exception {

		String directory = System.getProperty("user.dir");
		outputStream.writeUTF(directory);
	}

	private void ls() throws Exception {
		String outputFile = "";
		String directory = System.getProperty("user.dir");
		;
		File folder = new File(directory);
		File[] fileList = folder.listFiles();
		for (File allFileList : fileList) {
			if (allFileList.isFile()) {
				outputFile = outputFile + "File - " + allFileList.getName() + "\n";
			} else if (allFileList.isDirectory()) {
				outputFile = outputFile + "Directory - " + allFileList.getName() + "\n";
			}
		}
		outputStream.writeUTF(outputFile);
	}

	/**
	 * @param path
	 * @throws Exception
	 */
	private void cd(String path) throws Exception {
		String currentDirectory;
		currentDirectory = System.getProperty("user.dir");
		String newDirectory = "";

		if (path.equals("..")) {
			newDirectory = currentDirectory.substring(0, currentDirectory.lastIndexOf("/"));
		} else if (path.charAt(0) == 'E' || path.charAt(0) == '/') {
			newDirectory = path;
		} else {
			newDirectory = currentDirectory + "/" + path;
		}

		File file = new File(newDirectory);

		if (file.exists()) {
			System.setProperty("user.dir", newDirectory);

			outputStream.writeUTF("Path Selected");
		} else {
			outputStream.writeUTF("Path Not Found");
		}
	}

	private void delete(String path) throws Exception {
		String currentDirectory = System.getProperty("user.dir");
		currentDirectory = currentDirectory + "/" + path;
		File file = new File(currentDirectory);
		boolean exists = file.exists();
		boolean status = false;
		if (exists) {
			status = file.delete();
			if (status) {
				outputStream.writeUTF("File deleted successfully");
			} else {
				outputStream.writeUTF("Error while deleting file");
			}

		} else {
			outputStream.writeUTF("No such file exists");
		}
	}

	private void mkdir(String path) throws Exception {
		String currentDirectory = System.getProperty("user.dir");
		String directory = (Paths.get(currentDirectory + "/" + path).toString());
		File newDirectory = new File(directory);
		if (!(newDirectory.exists())) {

			newDirectory.mkdir();
			outputStream.writeUTF("Created a new Directory " + directory);
		}
	}

	private void get(String fileName) throws Exception {

		DataOutputStream output;
		output = new DataOutputStream(clientSocket.getOutputStream());
		String currentDirectory = System.getProperty("user.dir");
		fileName = currentDirectory + "/" + fileName;
		BufferedInputStream bis = null;

		File file = new File(fileName);
		boolean exists = file.exists();

		if (exists) {
			outputStream.writeUTF("true");
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];
			bis = new BufferedInputStream(fis);
			bis.read(buffer, 0, buffer.length);
			output.write(buffer, 0, buffer.length);
			bis.close();
			output.flush();

		} else {
			outputStream.writeUTF("false");

		}

	}

	private void put(String fileName) throws Exception {
		try {

			String currentDirectory = System.getProperty("user.dir");
			fileName = currentDirectory + "/" + fileName;

			BufferedOutputStream bos = null;

			FileOutputStream fos = new FileOutputStream(fileName);
			bos = new BufferedOutputStream(fos);
			byte[] buffer = new byte[4096];

			int read = inputStream.read(buffer, 0, buffer.length);
			bos.write(buffer, 0, read);
			bos.flush();
			fos.flush();
			fos.close();
			bos.close();

			outputStream.writeUTF("File sent successfully to server");
		} catch (Exception e) {
			outputStream.writeUTF("Error sending file to server");

		}

	}

	private void quit() throws Exception {
		System.setProperty("user.dir", initialDirectory);
		inputStream.close();
		outputStream.close();
		clientSocket.close();
		System.out.println("The Terminal has been shut down");
	}

}
