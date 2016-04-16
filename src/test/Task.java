package test;
import java.io.File;


public class Task {
//	String device;
	File[] packageList;
//	File target;
	public Task(File[] packageList){
		this.packageList = packageList;
	}
	
	public void doRun(String device){
		new Worker().execute(device, packageList);
		for(int i = 0; i< packageList.length; i++) {
			System.out.println(device +" : " + packageList[i].getName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
