package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by guofeng.wu on 2016/4/13.
 */
public class Worker {
    public static final String BROADCAST_START = "filemonitor.tcl.com.filemonitor.broadcast.start";
    public static final String BROADCAST_END = "filemonitor.tcl.com.filemonitor.broadcast.end";
    private static int monkey_times = 300;
    private ShellCommand mShellCommand;

    public Worker() {
        mShellCommand = new ShellCommand();
    }

    public void execute(String device, File[] files){
         mShellCommand = new ShellCommand();
        for (File file :files){
            if (mShellCommand.installApk(device,file)){
                String packageName = mShellCommand.getApkPackageName(file);
                mShellCommand.sendBroadCast(device, BROADCAST_START, packageName);
                mShellCommand.startMonkey(device, file, monkey_times);
                mShellCommand.sendBroadCast(device, BROADCAST_END, packageName);
                if (!isFileMonitorCompleted(device,packageName)){
                    try {
                        Thread.sleep(1000);
                        System.out.println("Thread sleep 1s");
                        System.out.println("Second Check .mark: "+isFileMonitorCompleted(device,packageName));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mShellCommand.uninstallApk(device,file);
                mShellCommand.pull(device,packageName,".txt");
                write(packageName);
            }
        }
    }
    private boolean isFileMonitorCompleted(String device,String packageName){
        mShellCommand.pull(device,packageName,".mark");
        File file = new File("./"+packageName+".mark");
        if (file.exists()){
            mShellCommand.remove(device,packageName,".mark");
            return true;
        }
        return false;
    }
    public void write(String packageName){
        String content =packageName+"\r\n";
        byte[] buff = content.getBytes();
        File file = new File("./init.txt");
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fo = new FileOutputStream(file,true);
            fo.write(buff);
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setMonkey_times(int times){
        monkey_times = times;
    }
}
