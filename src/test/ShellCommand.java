package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by guofeng.wu on 2016/4/12.
 */
public class ShellCommand {
    private Process mProcess;
    public ShellCommand() {
    }
    public void startCmd(String cmd){
        try {
           mProcess = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Process getProcess() {
        return mProcess;
    }
    private String showCMDResult(String cmd){ //信息在获取一次之后，将会自动清空
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        try {
            while ((line =bufferedReader.readLine()) != null){
                line = line.trim(); //去空格
                if (line.length() != 0) {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close(); //包装流会自动关闭上层流，无需手动关闭
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("<"+cmd+" Result Info>: "+stringBuffer.toString());
        return stringBuffer.toString();
    }
    public boolean installApk(String device,File apkFile){
        String cmd = "adb -s "+device+" install -r "+apkFile.getAbsolutePath();
        startCmd(cmd);
        return showCMDResult("Install").contains("Success");
    }
    public boolean uninstallApk(String device,File apkFile){
        startCmd("adb -s " + device + " uninstall " + getApkPackageName(apkFile));
        return showCMDResult("Uninstall").contains("Success");
    }
    public String getApkPackageName(File apkFile){
        String str = apkFile.getName();
        return str.substring(0, str.lastIndexOf("."));
    }
    public void sendBroadCast(String device,String action,String extra){
        startCmd("adb -s "+device+" shell am broadcast -a "+action+" --es message "+extra);
        showCMDResult("SendBroadCast");
    }
    public void startMonkey(String device,File apkFile,int monkey_time){
        int seed = (int)(Math.random()*1000); //生成伪随机序列的种子数
        System.out.println(seed);
        startCmd("adb -s "+device+" shell monkey -p "+getApkPackageName(apkFile)+" --pct-touch 55 --pct-motion 5 --pct-trackball 20 --pct-majornav 20 -s "
                +seed+" --throttle 500 --ignore-crashes --ignore-timeouts --ignore-security-exceptions --monitor-native-crashes -v "
                        +monkey_time);
        showCMDResult("Monkey");
    }

    /**
     *
     * @param device 设备号
     * @param fileName 文件名
     * @param newPath 新路径
     */
    public void pull(String device,String fileName,String newPath){
        File file = new File(newPath);
        if (!file.exists()){
            if (!file.mkdirs()){
                System.out.println("Pull Error: make dirs failed ！");
            }
        }
        try {
            String path = file.getCanonicalPath();
            startCmd("adb -s "+device+" pull ./sdcard/"+fileName+" "+path);
            showCMDResult("Pull");
            showErrorResult("Pull");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void remove(String device,String fileName,String fileSuffix){
        startCmd("adb -s "+device+" shell rm -f "+"./sdcard/"+fileName+fileSuffix);
        showCMDResult("Remove");
        showErrorResult("Remove");
    }
    public String showErrorResult(String cmd){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        try {
            while ((line =bufferedReader.readLine()) != null){
                line = line.trim(); //去空格
                if (line.length() != 0) {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("<" + cmd + " Error Info>: " + stringBuffer.toString());
        return stringBuffer.toString();
    }
    public void stopApp(String device,File apkFile){
        startCmd("adb -s "+device+" shell am force-stop "+getApkPackageName(apkFile));
        showCMDResult("stopApp");
    }
    public void rename(String oldName,String newName){
        File file = new File(oldName);
        try {
            String path = file.getCanonicalPath();
            startCmd("cmd.exe /c ren "+path+" "+newName);
            showCMDResult("Rename");
            showErrorResult("Rename");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
