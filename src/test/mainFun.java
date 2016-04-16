package test;
import java.io.File;

import javax.swing.JOptionPane;


public class MainFun {
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

