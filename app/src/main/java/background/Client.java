package background;

import android.text.TextUtils;

import java.net.*;
import java.io.*;
import java.util.*;
import com.alibaba.fastjson.*;

/**需要单独开线程
 *
 */


/**
 * 需要单独开线程
 *
 */
public class Client {
    private A a;
    private Thread t;
    private static Client instance;
    private long frac = 50;
    private long timeout;

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public static Client getInstance() {
        if (instance == null)
            instance = new Client();
        return instance;
    }

    public boolean init() {
        a = A.getInstance();
        boolean res = a.init();
        if (!res)
            return false;
        t = new Thread(a);
        t.start();
        return true;
    }

    public synchronized int login(String username, String password) {
        a.setUsername(username, password);
        a.command(A.LOGIN);
        long count = 0;
        while (!a.hasResponse()) {
            if (count > timeout)
                break;
            count += frac;
            try {
                Thread.sleep(frac);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!a.hasResponse())
            return -1;
        if (!a.isSuccess())
            return 0;
        return 1;
    }

    public synchronized void logout() {
        a.command(A.LOGOUT);
    }

    public synchronized int register(String username, String password) {
        a.setUsername(username, password);
        a.command(A.REGISTER);
        long count = 0;
        while (!a.hasResponse()) {
            if (count > timeout)
                break;
            count += frac;
            try {
                Thread.sleep(frac);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!a.hasResponse())
            return -1;
        if (!a.isSuccess())
            return 0;
        return 1;
    }

    public synchronized boolean upload(Data data) {
        a.setContent(data);
        a.command(A.UPLOAD);
        long count = 0;
        while (!a.hasResponse()) {
            if (count > timeout)
                break;
            count += frac;
            try {
                Thread.sleep(frac);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!a.hasResponse())
            return false;
        return true;
    }

    public synchronized Data[] fetch() {
        a.command(A.FETCH);
        long count = 0;
        while (!a.hasResponse()) {
            if (count > timeout)
                break;
            count += frac;
            try {
                Thread.sleep(frac);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!a.hasResponse())
            return null;
        return JSONObject.parseArray(a.getContent(), Data.class).toArray(new Data[0]);
    }

    public synchronized boolean delete(String id) {
        a.setRemove(id);
        a.command(A.DELETE);
        long count = 0;
        while (!a.hasResponse()) {
            if (count > timeout)
                break;
            count += frac;
            try {
                Thread.sleep(frac);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!a.hasResponse())
            return false;
        return true;
    }

    public boolean isValid() {
        return a==null ? false : a.isValid;
    }
}

class A implements Runnable {// code=0
    private static A instance;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private boolean response = false, success = false, free = true;
    final static int LOGIN = 1, REGISTER = 2, LOGOUT = 3, UPLOAD = 4, FETCH = 5, DELETE = 6, IDLE = 0;
    boolean isValid=false;

    private A() {

    }

    public boolean init() {
        try {
            socket = new Socket("2658l9u930.zicp.vip", 2526);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream());
            return isValid=true;
        } catch (IOException e) {
            e.printStackTrace();
            terminate();
            return false;
        }
    }

    public static A getInstance() {
        if (instance == null)
            instance = new A();
        return instance;
    }

    K lock = new K();

    private void terminate() {
        try {
            isValid=false;
            br.close();
            pw.close();
            socket.close();
            return;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        synchronized (lock) {
            while (true) {
                success = false;
                response = false;
                free = false;
                switch (lock.code) {
                    case REGISTER:
                        synchronized (pw) {
                            pw.println("REGISTER " + lock.username + " " + lock.password);
                            pw.flush();
                            String res;
                            synchronized (br) {
                                res = readLine();
                            }
                            if (res.equals("SUCCESS")) {
                                response = true;
                                success = true;
                                System.out.println("REGISTER SUCCESS");
                            } else {
                                response = true;
                                System.out.println("REGISTER FAILED");
                            }
                        }
                        break;
                    case LOGIN:
                        pw.println("LOGIN " + lock.username + " " + lock.password);
                        pw.flush();
                        String res;
                        synchronized (br) {
                            res = readLine();
                        }
                        if (res.equals("SUCCESS")) {
                            response = true;
                            success = true;
                            System.out.println("LOGIN SUCCESS");
                        } else {
                            response = true;
                            System.out.println("LOGIN FAILED");
                        }
                        break;
                    case LOGOUT:
                        terminate();
                        return;
                    case UPLOAD:// the newID of news to be removed & the news to be upload
                        synchronized (pw) {
                            pw.println("UPLOAD START");
                            pw.println(lock.content);
                            pw.println("END");
                            pw.flush();
                            String line;
                            synchronized (br) {
                                line = readLine();
                            }
                            if (line.equals("SUCCESS")) {
                                response = true;
                                success = true;
                                System.out.println("UPLOAD SUCCESS");
                            }
                        }
                        break;
                    case FETCH:
                        synchronized (pw) {
                            pw.println("FETCH");
                            pw.flush();
                        }
                        synchronized (br) {
                            String line = null;
                            StringBuilder sb = new StringBuilder();
                            while (true) {
                                line = readLine();
                                if (line.equals("END"))
                                    break;
                                sb.append(line);
                            }
                            lock.content = sb.toString();
                            System.out.println("FETCH:" + lock.content);
                            response = true;
                            success = true;
                        }
                        break;
                    case DELETE:
                        synchronized (pw) {
                            pw.println("DELETE " + lock.removeID);
                            pw.flush();
                        }
                        String line = readLine();
                        if (line.equals("SUCCESS")) {
                            response = true;
                            success = true;
                            System.out.println("delete success");
                        }
                        break;
                    default:
                        break;
                }
                lock.code = IDLE;
                try {
                    lock.wait();
                    free = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("WAKEN");
                if (lock.code == 0) {
                    System.out.println("logout");
                    return;
                }
            }
        }
    }

    public synchronized void command(int i) {
        synchronized (lock) {
            lock.code = i;
            response = false;
            success = false;
            lock.notify();
        }
    }

    public synchronized void setUsername(String username, String password) {
        synchronized (lock) {
            lock.username = username;
            lock.password = password;
        }
    }

    public synchronized void setContent(String content) {
        synchronized (lock) {
            lock.content = content;
        }
    }

    public synchronized void setContent(Data data) {
        synchronized (lock) {
            lock.content = JSONObject.toJSONString(data);
        }
    }

    public synchronized void setRemove(String id) {
        lock.removeID = id;
    }

    private String readLine() {
        synchronized (br) {
            String line = null;
            while (true) {
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    terminate();
                    return null;
                }
                if (line == null) {
                    if(!socket.isConnected()) {
                        terminate();
                        return null;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                } else
                    return line;
            }
        }
    }

    public boolean hasResponse() {
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFree() {
        return free;
    }

    public String getContent() {
        return lock.content;
    }
}

class K {
    public int code = 0;
    public String username, password, content;
    public String removeID;
}