package test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;


public class test {
        public static void main(String[] args) {
        	TaskCenter taskCenter = TaskCenter.getTaskCenter();
        	while (true){
//        		Scanner scanner = new Scanner(System.in);
//                String result = scanner.nextLine();
        		String str = JOptionPane.showInputDialog("enter a string~!");
        		if(str != null){
        			if(str.contains("*")){
						int num = str.indexOf("*");
						String path = str.substring(0, num);
						String taskNum = str.substring(num+1, str.length());
						File file = new File(path);
						if (file.exists()){
							if (file.isDirectory()){
								int temp = Integer.parseInt(taskNum);
								taskCenter.addTask(file, temp);
							}else {
								System.out.println("file is not directory");
							}
						}else {
							System.out.println("file is not exists");
						}
            		}else if ((new File(str)).isDirectory()){
						taskCenter.addTask(new File(str));
					}
					else if(str.equals("refresh")){
            			taskCenter.addDevices();
            		} else if(str.equals("exit")) {
//            			System.exit(0);
						taskCenter.exit();
            		} else if(str.equals("show devices")) {
						taskCenter.showPhone();
            		}
        		}
        	}
        }
}



class TaskCenter{
	
//	private BlockingQueue workPhoneList = new LinkedBlockingQueue(10);
	private BlockingQueue waitPhoneList = new LinkedBlockingQueue();
	private BlockingQueue taskList = new LinkedBlockingQueue(128);

	private final Object listenerLock;

	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	
	private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
	
	private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
	
	private static final int KEEP_ALIVE = 1;
	
	private static final BlockingQueue sPoolWorkQueue = new LinkedBlockingQueue(128);
	
	private static final ThreadFactory sThreadFactory = new ThreadFactory(){
		AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable arg0) {
			// TODO Auto-generated method stub
			return new Thread(arg0, "TaskCenterThread :"+ mCount.getAndIncrement());
		}
	};
	
	private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
	
	private volatile static TaskCenter _instance;
	
	private SchedulingThread mSchedulingThread = new SchedulingThread();
	
	private TaskCenter(){
		listenerLock = new Object();
		addDevices();
		mSchedulingThread.start();
	}
	
	public static TaskCenter getTaskCenter(){
		if(_instance == null) {
			synchronized(TaskCenter.class){
				if(_instance == null){
					_instance = new TaskCenter();
				}
			}
		}
		
		return _instance;
	}
	
	
	
	synchronized public void addDevices() {
		Runtime rt = Runtime.getRuntime();
		Process p;
		BufferedReader br = null;
		try {
			p = rt.exec("cmd.exe /c adb devices");
			StringBuffer bf = new StringBuffer();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			br.readLine();
			while ((line = br.readLine()) != null) {
				if (! line.isEmpty()) {
					int end = line.indexOf('\t');
					String device = line.substring(0,end);
//					&& (!workPhoneList.contains(device))
					if((!waitPhoneList.contains(device))){
						waitPhoneList.add(device);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("");
			e.printStackTrace();
		}
		
	}
	
	public void noteApk(File [] files){
		File node = files[0];
		String path = node.getParent();
		node = new File(path + "\\note.txt");
		if(!node.exists()){
			try {
				if(!node.createNewFile()){
					System.out.println("file create fail");
					return;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("file create fail");
				return;
			}
		}
		try {
			Writer wr = new FileWriter(node, true);
			for(int i = 0; i < files.length; i++) {
				wr.write(files[i].getName()+"\r\n");
			}
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List readNoteApk(File file){
		String path = file.getAbsolutePath();
		path = path + "\\note.txt";
		File node = new File(path);
		if (!node.exists()){
			return new LinkedList();
		}
		InputStreamReader reader = null;
		List fileList = new LinkedList();
		try {
			reader = new InputStreamReader(  
			        new FileInputStream(node));
			BufferedReader br = new BufferedReader(reader);
	        String line = "";
	        line = br.readLine();
	        while (line != null) {
	        	fileList.add(new String(line));
	            line = br.readLine();
	        }  
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileList;
        
	}
	
	public void addTask(File [] files) {
		noteApk(files);
		Task task = new Task(files);
		if(taskList.offer(task)){
			return;
		}else {
			System.out.println("task max is 128");
		}
	}
	
	public void addTask(File target, int num) {
		if (target.exists()){
			if (target.isDirectory()){
				File [] files = screenAPK(target);
				if( files.length > num) {
					File [] node = new File[num];
					for (int i = 0; i < num; i++){
						node[i] = files[i];
					}
					addTask(node);
				}else {
					File [] node = new File[files.length];
					for (int i = 0; i < files.length; i++){
						node[i] = files[i];
					}
					addTask(node);
				}
			}else{
				System.out.println("file is not direcory");
			}
		}else{
			System.out.println("file is not direcory");
		}
	}

	public void addTask(File target) {
		if (target.exists()) {
			if (target.isDirectory()) {
				File[] files = target.listFiles();
				int num = files.length;
				int phone = waitPhoneList.size();
				num = num / phone;
				for (int i = 1; i <= phone; i++) {
					if (i == phone) {
						addTask(target, files.length - num * (i - 1));
					} else {
						addTask(target, num);
					}
				}
			}
		}
	}

	public static File [] screenAPK(File target){
		List fileNode = readNoteApk(target);
		List note = new LinkedList();
		File [] files = target.listFiles();
		if (fileNode.size() == 0){
			for (int i = 0; i < files.length; i++){
				String fileName = files[i].getName();
				if (fileName.charAt(fileName.length()-1) == 'k'){
					note.add(files[i]);
				}
			}
		}else {
			for (int i = 0; i < files.length; i++){
				String fileName = files[i].getName();
				if (!fileNode.contains(fileName)){
					if (fileName.charAt(fileName.length()-1) == 'k'){
						note.add(files[i]);
					}
				}
			}
		}
		int num = note.size();
		File [] noteFils = new File[num];
		for (int i = 0; i < num; i++) {
			noteFils[i] = (File)note.get(i);
		}

		return noteFils;
	}

	public void showPhone(){
		String temp = "";
		try {
			for(int i = 0; i < waitPhoneList.size(); i++) {
				temp = (String) waitPhoneList.take();// 取出队列值，取一次，就从队列中移除
				waitPhoneList.offer(temp);
				System.out.println(temp);
			}
		} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	protected final void exit() {
		mSchedulingThread.interrupt();
	}
	
	public class SchedulingThread extends Thread{

		public void run() {
			// TODO Auto-generated method stub
			super.run();
			System.out.println("SchedulingThread is run");
			while(!Thread.currentThread().isInterrupted()){
				addDevices();
				String device = "";
				Task task = new Task(new File[0]);
				try {
					device = (String) waitPhoneList.take();
					task =(Task) taskList.take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//interrupeedTxception is other people call interrup();
					// So , we must break my loop
					e.printStackTrace();
					System.exit(0);
				}
				final Task dataTask = task;
				final String node = device;
				
				THREAD_POOL_EXECUTOR.execute(new Runnable(){

					public void run() {
						// TODO Auto-generated method stub
						dataTask.doRun(node);
						if( !waitPhoneList.offer(node)){
							addDevices();
						}
						System.out.println(node + " do work finish!!");
					}
				});
			}
		}
		
	}
}



//���package��packageList��ȥ
//	public void getPackages() {
//		File[] files = null;
//		File packages = new File("C:\\Users\\haiyang.tan\\Desktop\\apklist");
//		if(packages.exists()) {
//			 if(packages.isDirectory()) {
//				 files = packages.listFiles();
//				 for (int i = 0; i < files.length; i++){
//					 System.out.println(files[i].getAbsolutePath());
//				 }
//			 }else {
//				 System.out.println("Desktop not hava apklist");
//			 }
//		}else {
//			System.out.println("Desktop not hava apklist");
//		}
//
//		Object var1 = listenerLock;
//		synchronized(this.listenerLock){
//			apkFiles = new LinkedList(Arrays.asList(files));
//		}
//	}

//�Ƴ�ָ��������apk��������
//	private List getApkFiles(int num){
//		List apkTaskFiles = new LinkedList();
//		if(apkFiles.size() < num){
//			System.out.println("apk files is not enough");
//			num = apkFiles.size();
//		}
//		Object var2 = listenerLock;
//		synchronized(this.listenerLock){
//			for (int i = 0; i < num; i++){
//				File file = (File) apkFiles.get(0);
//				apkTaskFiles.add(file);
//				apkFiles.remove(0);
//			}
//		}
//		return apkTaskFiles;
//	}

