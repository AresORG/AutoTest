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

                int times =0;
                while (times < 30){ //若检测不到.mark文件，30秒后自动退出
                    try {
                        times++;
                        System.out.println("Waiting for " +times+" s");
                        if (isFileMonitorCompleted(device,packageName)){
                            times =30;
                        }
                        Thread.sleep(1000);

                        System.out.println("Second Check .mark: "+isFileMonitorCompleted(device,packageName));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mShellCommand.uninstallApk(device,file);
                write(packageName,new File("./init.txt"));
            }
        }
        mShellCommand.pull(device,"/db/result.db","./db");
        mShellCommand.rename("./db/result.db",device+".db");
    }
    private boolean isFileMonitorCompleted(String device,String packageName){
        mShellCommand.pull(device,packageName+".mark","./mark");
        File file = new File("./"+packageName+".mark");
        if (file.exists()){
            mShellCommand.remove(device,packageName,".mark");
            return true;
        }
        return false;
    }
    public void write(String content,File file){
        String contents =content+"\r\n";
        byte[] buff = contents.getBytes();
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
