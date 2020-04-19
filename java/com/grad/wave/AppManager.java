package com.grad.wave;

import android.app.Application;
import android.content.Context;
import android.icu.util.Output;
import android.media.ResourceBusyException;
import android.os.StrictMode;
import android.renderscript.ScriptGroup;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppManager extends Application {

    public static String UserName;
    public static int UserID;
    public static boolean is_admin = false;

    private static String UserPw = null;
    public static String getUserPw() {return UserPw;}
    public static void setUserPw(String pw){UserPw = pw;}

    public void StartKeepingAlive(){
        KeepAliveThread keepalive = new KeepAliveThread();
        keepalive.start();
    }

    class KeepAliveThread extends Thread {

        public KeepAliveThread(){}

        public void run() {
            while(true) {
                try {
                    sleep(10000);
                    if (!AppIO.taskRunning)
                        NetIO.SendCmd(C1.ALIVE);
                } catch (Exception ex) {
                    run();
                }
            }
        }
    }

    public static class NetIO {
        private static Socket serv_socket;
        private static Context appcontext;
        private static boolean is_startup = false;
        public static boolean is_contconnect = false;

        public static InputStream is;
        public static OutputStream os;

        public static boolean check_startup() {
            return is_startup;
        }

        private static void re_startup(){
            StartUp(appcontext);
            AppIO.Login(UserName,getUserPw());

        }

        public static void StartUp(Context context)  {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
                StrictMode.setThreadPolicy(policy);
                appcontext = context;
                InetAddress servaddr = InetAddress.getByName("106.52.253.223");
                serv_socket = new Socket();
                serv_socket.connect(new InetSocketAddress(servaddr.getHostAddress(), 1080), 10000);
                while (!serv_socket.isConnected()) ;
                is_startup = true;
                is = serv_socket.getInputStream();
                os = serv_socket.getOutputStream();

            } catch (Exception ex) {
                is_contconnect = true;
                if (ex.getClass() == java.net.UnknownHostException.class) {
                    Toast.makeText(appcontext, "连接服务器失败，不如先挑战下飞花令？", Toast.LENGTH_SHORT).show();
                } else if (ex.getClass() == java.net.SocketTimeoutException.class) {
                    Toast.makeText(appcontext, "连接服务器超时", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public static void SendCmd(C1 c1) {
            try {
                byte[] buffer = new byte[1024];
                buffer[0] = (byte) 0xAA;
                buffer[1] = c1.v;
                buffer[1023] = C2.STOP.v;
                os.write(buffer, 0, 1024);
            } catch (Exception ex) {
                Toast.makeText(appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        public static void SendDat(byte c1, @NonNull byte[] dat, byte c2) {
            try {
                if (!is_startup)
                    return;
                byte[] buffer = new byte[1024];
                buffer[0] = (byte) 0xFF;
                buffer[1] = c1;
                for (int i = 0; i < dat.length && i < 1021; ++i)
                    buffer[i + 2] = dat[i];
                buffer[1023] = c2;
                os.write(buffer, 0, 1024);
            } catch (Exception ex) {
                Toast.makeText(appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        public static void SendDat(C1 c1, @NonNull byte[] dat, C2 c2) {
            try {
                if (!is_startup)
                    return;
                byte[] buffer = new byte[1024];
                buffer[0] = (byte) 0xAA;
                buffer[1] = c1.v;
                for (int i = 0; i < dat.length && i < 1021; ++i)
                    buffer[i + 2] = dat[i];
                buffer[1023] = c2.v;
                os.write(buffer, 0, 1024);
            } catch (Exception ex) {
                //Toast.makeText(appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                re_startup();
                SendDat(c1,dat,c2);
            }
        }

        public static byte[] RecvDat() {
            final byte[] buffer = new byte[1023];
            try {
                SendCmd(C1.WFCM);
                if (!is_startup)
                    return null;
                Thread.sleep(300);
                is.read(buffer, 0, 1);
                while (buffer[0] != (byte) 0xAA)
                    is.read(buffer, 0, 1);
                is.read(buffer, 0, 1023);

                return buffer;
            } catch (Exception ex) {
                //Toast.makeText(appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                re_startup();
                return RecvDat();
                //return new byte[1023];
            }
        }

        public static byte[] XRecvDat(int timeouts) {
            try {
                double timeout = timeouts * 1000;
                DataReceivingThread recv = new DataReceivingThread(appcontext);
                recv.start();
                while (!recv.is_received) {
                    Thread.sleep(100);
                    timeout -= 100;
                    if (timeout <= 0) {
                        System.out.println("XRecvDat timeout");
                        return new byte[1023];
                    }
                }
                System.out.println("XRecvDat returned");
                return recv.dat;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                Toast.makeText(appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                return new byte[1023];
            }
        }

        public static C1 WFCM(@Nullable C1[] cmd) {
            byte rcmd = XRecvDat(5)[0];
            if (cmd != null) {
                for (int i = 0; i < cmd.length; ++i) {
                    if (rcmd == cmd[i].v)
                        return cmd[i];
                }
                rcmd = XRecvDat(5)[0];
            }
            for (int i = 0; i < C1.values().length; ++i) {
                if (rcmd == C1.values()[i].v)
                    return C1.values()[i];
            }
            return null;
        }
    }

    public static class AppIO {
        public static boolean taskRunning = false;

        public static ArrayList<ArticleInfo> GetList(int blockID, int offset, int size) {
            taskRunning = true;
            try {
                if (!NetIO.check_startup()) return null;
                ArrayList<ArticleInfo> list = new ArrayList<ArticleInfo>();

                byte[] dat = new byte[9];
                dat[0] = (byte) blockID;
                for (int i = 0; i < 4; ++i)
                    dat[i + 1] = intToBytes(offset)[i];
                for (int i = 0; i < 4; ++i)
                    dat[i + 5] = intToBytes(size)[i];
                NetIO.SendDat(C1.GLIST, dat, C2.STOP);

                byte[] buffer = NetIO.XRecvDat(5);
                if(buffer[0] == C1.REJ.v)
                    throw new Exception("服务器拒绝了请求");
                while (buffer[0] == C1.ARTINI.v || buffer[0] == C1.BNULL.v) {
                    if (buffer[0] == C1.BNULL.v){
                        taskRunning = false;
                        return list;
                    }

                    int code = ByteToInt(buffer, 1, 4);
                    byte bcode = buffer[5];
                    int time = ByteToInt(buffer, 6, 4);
                    String title = new String(buffer, 10, 256);
                    String author = new String(buffer, 266, 256);
                    String uploader = new String(buffer, 522, 256);
                    int length = ByteToInt(buffer, 778, 4);
                    ArticleType type = ArticleType.Unknown;
                    switch (buffer[782]) {
                        case 0:
                            type = ArticleType.ModernChinese;
                            break;
                        case 1:
                            type = ArticleType.ClassicChinese;
                            break;
                        case 2:
                            type = ArticleType.Foreign;
                            break;
                        case 3:
                            type = ArticleType.Appreciation;
                            break;
                    }
                    short repcout = ByteToShort(buffer, 783, 2);
                    int userid = ByteToInt(buffer, 785, 4);
                    list.add(new ArticleInfo(code, bcode, time, title, author, uploader, length, type, repcout, userid));
                    if (buffer[1022] == C2.STOP.v) {
                        taskRunning = false;
                        return list;
                    }
                    buffer = NetIO.XRecvDat(5);
                }
                return list;
            } catch (Exception ex) {
                Toast.makeText(NetIO.appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                taskRunning = false;
                return new ArrayList<ArticleInfo>();
            }
        }

        public static ArticleData GetArticleData(@NonNull ArticleInfo info) {
            taskRunning = true;
            try {
                byte[] IDbytes = intToBytes(info.ID);
                byte[] req = new byte[5];
                for (int i = 0; i < 4; ++i) req[i] = IDbytes[i];
                req[4] = info.BlockID;
                NetIO.SendDat(C1.GTART, req, C2.STOP);
                int time = 0;

                byte[] buffer = NetIO.XRecvDat(5);
                byte[] dat = new byte[info.Length];
                int offset = 0;
                while (buffer[0] == C1.ARTDAT.v) {
                    if (buffer[1022] != C2.STOP.v) {
                        for (int i = 0; i < 1021; ++i)
                            dat[i + offset] = buffer[i + 1];
                        offset += 1021;
                        buffer = NetIO.XRecvDat(5);
                    } else {
                        for (int i = 0; i < info.Length - offset; ++i) {
                            dat[i + offset] = buffer[i + 1];
                        }
                        break;
                    }
                }
                ArticleData data = new ArticleData(info, new String(dat));{
                    taskRunning=false;
                    return data;
                }
            } catch (Exception ex) {
                //Toast.makeText(NetIO.appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                taskRunning = false;
                return null;
            }
        }

        public static boolean UploadArticle(@NonNull ArticleData data) {
            taskRunning = true;
            byte[] inforeq = new byte[1021];
            inforeq[4] = data.info.BlockID;
            for (int i = 0; i < 4; ++i)
                inforeq[i + 5] = intToBytes(data.info.CreatedTimeS)[i];
            for (int i = 0; i < 256; ++i)
                inforeq[i + 9] = data.info.GetTitleBytes()[i];
            for (int i = 0; i < 256; ++i)
                inforeq[i + 265] = data.info.GetAuthorBytes()[i];
            for (int i = 0; i < 256; ++i)
                inforeq[i + 521] = data.info.GetUploaderBytes()[i];
            for (int i = 0; i < 4; ++i)
                inforeq[i + 777] = intToBytes(data.info.Length)[i];
            inforeq[781] = data.info.Type.getValue();
            inforeq[782] = ShortToByte(data.info.RepCount)[0];
            inforeq[783] = ShortToByte(data.info.RepCount)[1];
            for (int i = 0; i < 4; ++i)
                inforeq[784 + i] = intToBytes(data.info.UserID)[i];

            NetIO.SendDat(C1.ARTINI, inforeq, C2.STOP);

            byte[] databytes = data.content.getBytes();
            byte[] datareq = new byte[1021];
            int offset = 0;
            while (databytes.length - offset > 1021) {
                for (int i = 0; i < 1021; ++i)
                    datareq[i] = databytes[i + offset];
                NetIO.SendDat(C1.ARTDAT, datareq, C2.KEEP);
                offset += 1021;
                datareq = new byte[1021];
            }
            for (int i = 0; i < databytes.length - offset; ++i)
                datareq[i] = databytes[i + offset];
            NetIO.SendDat(C1.ARTDAT, datareq, C2.STOP);

            C1[] cmds = {C1.DATSUCS};
            if (NetIO.WFCM(cmds) == C1.DATSUCS) {
                taskRunning = false;
                return true;
            }
            taskRunning = false;
            return false;
        }

        public static boolean DeleteArticle(@NonNull ArticleInfo info) {
            taskRunning = true;
            try {
                byte[] req = new byte[5];
                for (int i = 0; i < 4; ++i)
                    req[i] = intToBytes(info.ID)[i];
                req[4] = info.BlockID;
                NetIO.SendDat(C1.DLART, req, C2.STOP);
                C1[] cmds = {C1.DATSUCS, C1.DATFAIL};
                if (NetIO.WFCM(cmds) == C1.DATSUCS) {
                    taskRunning =false;
                    return true;
                }
                else
                    throw new Exception("删除失败");
            } catch (Exception ex) {
                Toast.makeText(NetIO.appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                taskRunning = false;
                return false;
            }
        }

        public static boolean UploadComment(@NonNull String comment, int id, byte bcode) {
            taskRunning = true;
            try {
                byte[] commentbytes = comment.getBytes();
                byte[] dat = new byte[1021];
                for (int i = 0; i < 4; ++i)
                    dat[i] = intToBytes(id)[i];
                dat[4] = bcode;
                for (int i = 0; i < 765 && i < commentbytes.length; ++i)
                    dat[i + 5] = commentbytes[i];
                NetIO.SendDat(C1.COMTUP, dat, C2.STOP);

                C1[] cmds = {C1.DATSUCS};
                if (NetIO.WFCM(cmds) == C1.DATSUCS) {
                    taskRunning = false;
                    return true;
                }
                taskRunning = false;
                return false;
            } catch (Exception ex) {
                Toast.makeText(NetIO.appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                taskRunning = false;
                return false;
            }
        }

        public static ArrayList<CommentData> GetComments(int id, byte bcode,int offset,int size) {
            taskRunning = true;
            try {
                byte[] req = new byte[13];
                for (int i = 0; i < 4; ++i)
                    req[i] = intToBytes(id)[i];
                req[4] = bcode;
                for(int i=0;i<4;++i)
                    req[i+5] = intToBytes(offset)[i];
                for(int i=0;i<4;++i)
                    req[i+9] = intToBytes(size)[i];
                NetIO.SendDat(C1.GTCMT, req, C2.STOP);

                ArrayList<CommentData> list = new ArrayList<>();
                byte[] cmt = new byte[1023];
                while (cmt[0] != C1.COMTDL.v && cmt[0] != C1.BNULL.v)
                    cmt = NetIO.XRecvDat(5);
                if (cmt[0] == C1.BNULL.v)
                    throw new Exception("暂无评论");
                else if(cmt[0] == C1.REJ.v)
                    throw new Exception("服务器拒绝了请求");
                while (cmt[1022] != C2.STOP.v) {
                    /*
                    byte[] makerbytes = new byte[256];
                    byte[] commentbytes = new byte[765];
                    for(int i=0;i<256;++i)
                        makerbytes[i] = cmt[i+1];
                    for(int i=0;i<765;++i)
                        commentbytes[i] = cmt[i+256];*/
                    String maker = new String(cmt, 1, 256);
                    String comment = new String(cmt, 257, 765);
                    list.add(0, new CommentData(maker, comment));
                    cmt[0] = 0;
                    cmt = NetIO.XRecvDat(5);
                    if (cmt[0] == C1.BNULL.v)
                        throw new Exception("暂无评论");
                    if (cmt[0] == 0) {
                        taskRunning = false;
                        return list;
                    }
                }
                byte[] makerbytes = new byte[256];
                byte[] commentbytes = new byte[765];
                for (int i = 0; i < 256; ++i)
                    makerbytes[i] = cmt[i + 1];
                for (int i = 0; i < 765; ++i)
                    commentbytes[i] = cmt[i + 256];
                String maker = new String(makerbytes);
                String comment = new String(commentbytes);
                list.add(0, new CommentData(maker, comment));
                taskRunning = false;
                return list;
            } catch (Exception ex) {
                //Toast.makeText(NetIO.appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                taskRunning = false;
                return null;
            }
        }

        public static boolean Login(String username, String pw) {
            taskRunning = true;
            try {
                if (!NetIO.check_startup())
                    return false;
                byte[] dat = new byte[1021];
                for (int i = 0; i < username.getBytes().length && i < 256; ++i)
                    dat[i] = (byte) username.getBytes()[i];
                for (int i = 0; i < pw.getBytes().length && i < 256; ++i)
                    dat[i + 256] = (byte) pw.getBytes()[i];
                NetIO.SendDat(C1.LOGIN, dat, C2.STOP);
                byte[] result = NetIO.XRecvDat(5);
                while (result[0] != C1.LOGSUCS.v && result[0] != C1.PWFAIL.v && result[0] != C1.USFAIL.v && result[0] != C1.REJ.v)
                    result = NetIO.XRecvDat(5);
                if (result[0] == C1.LOGSUCS.v) {
                    AppManager.UserName = username;
                    AppManager.UserID = ByteToInt(result, 1, 4);
                    setUserPw(pw);
                    taskRunning = false;
                    return true;
                }
                if (result[0] == C1.PWFAIL.v)
                    throw new Exception("密码错误");
                else if(result[0] == C1.USFAIL.v)
                    throw new Exception("用户名错误");
                else
                    throw new Exception("服务器拒绝了请求");
            } catch (Exception ex) {
                Toast.makeText(NetIO.appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                taskRunning = false;
                return false;
            }
        }

        public static boolean Regist(String username, String pw) {
            taskRunning = true;
            try {
                byte[] dat = new byte[1021];
                for (int i = 0; i < username.getBytes().length && i < 256; ++i)
                    dat[i] = (byte) username.getBytes()[i];
                for (int i = 0; i < pw.getBytes().length && i < 256; ++i)
                    dat[i + 256] = (byte) pw.getBytes()[i];
                NetIO.SendDat(C1.REG, dat, C2.STOP);

                dat = NetIO.XRecvDat(5);
                while (dat[0] != C1.REGSUCS.v && dat[0] != C1.USFAIL.v && dat[0] != C1.REJ.v)
                    dat = NetIO.XRecvDat(5);
                if (dat[0] == C1.REGSUCS.v) {
                    taskRunning = false;
                    return true;
                } else if(dat[0] == C1.USFAIL.v) {
                    throw new Exception("用户名已存在");
                }else
                    throw new Exception("服务器拒绝了请求");
            } catch (Exception ex) {
                Toast.makeText(NetIO.appcontext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                taskRunning = false;
                return false;
            }
        }

        public static boolean SetServerStatus(ServerConfig conf) {
            taskRunning = true;
            try {
                byte[] dat = new byte[1021];
                for (int i = 0; i < 4; ++i)
                    dat[i] = intToBytes(conf.MaxClient)[i];
                for (int i = 0; i < 4; ++i)
                    dat[i + 4] = intToBytes(conf.ConnectedClient)[i];
                for (int i = 0; i < 4; ++i)
                    dat[i + 14] = intToBytes(conf.Check())[i];
                if (conf.RegPermitted)
                    dat[8] = 1;
                if (conf.UploadPermitted)
                    dat[9] = 1;
                if (conf.CommentPermitted)
                    dat[10] = 1;
                if (conf.RefreshPermitted)
                    dat[11] = 1;
                if (conf.LoginPermitted)
                    dat[12] = 1;
                if(conf.FlyingOrderPermitted)
                    dat[13] = 1;
                NetIO.SendDat(C1.SETSTAT, dat, C2.STOP);
                byte[] result = NetIO.XRecvDat(5); 
                while (result[0] != C1.DATSUCS.v && result[0] != C1.REJ.v && result[0] != C1.DATFAIL.v)
                    result = NetIO.XRecvDat(5);
                if(result[0] == C1.REJ.v)
                    throw new Exception("服务器拒绝了请求");
                else if(result[0] == C1.DATFAIL.v)
                    throw new Exception("更改失败");
                taskRunning = false;
                return true;
            }catch(Exception ex){
                Toast.makeText(NetIO.appcontext,ex.getMessage(),Toast.LENGTH_SHORT).show();
                taskRunning = false;
                return false;
            }
        }

        public static ServerConfig RequestServerConfig(){
            taskRunning = true;
            try {
                ServerConfig conf = new ServerConfig();
                int errorcount = 0;
                while (!conf.checkpass) {
                    if(errorcount >=5)
                        throw new Exception("多次请求服务器状态失败");
                    NetIO.SendCmd(C1.GTSTAT);
                    byte[] result = NetIO.XRecvDat(5);
                    while (result[0] != C1.STAT.v && result[0] != C1.REJ.v)
                        result = NetIO.XRecvDat(5);
                    if(result[0] == C1.REJ.v)
                        throw new Exception("服务器拒绝了请求");
                    int maxclnt = ByteToInt(result, 1, 4);
                    int conclnt = ByteToInt(result, 5, 4);
                    boolean regper, upper, comper, refper, logper,foper;
                    if (result[9] == 1) regper = true;
                    else regper = false;
                    if (result[10] == 1) upper = true;
                    else upper = false;
                    if (result[11] == 1) comper = true;
                    else comper = false;
                    if (result[12] == 1) refper = true;
                    else refper = false;
                    if (result[13] == 1) logper = true;
                    else logper = false;
                    if (result[14] == 1) foper = true;
                    else foper = false;
                    conf = new ServerConfig(maxclnt, conclnt, regper, upper, comper, refper, logper,foper);
                        if (conf.Check() == ByteToInt(result, 15, 4))
                        conf.checkpass = true;
                    errorcount ++;
                }
                taskRunning = false;
                return conf;
            }catch(Exception ex){
                Toast.makeText(NetIO.appcontext,ex.getMessage(),Toast.LENGTH_SHORT).show();
                taskRunning  = false;
                return null;
            }
        }

        public static boolean RequestFlyingOrder(){
            taskRunning = true;
            NetIO.SendCmd(C1.REQFO);
            byte[] result = NetIO.XRecvDat(5);
            if(result[0] == C1.DATSUCS.v){
                taskRunning = false;
                return true;
            }
            taskRunning = false;
            return false;
        }

        public static NetPoem GetDailyVerse(){
            taskRunning = true;
            try{
                int dayofyear = Integer.parseInt(new SimpleDateFormat("DDD").format(new Date()));
                byte[] req = intToBytes(dayofyear);
                NetIO.SendDat(C1.REQDV,req,C2.STOP);

                byte[] dat = NetIO.XRecvDat(5);
                while(dat[0] != C1.DV.v && dat[0] != C1.REJ.v)
                    dat = NetIO.XRecvDat(5);
                if(dat[0] == C1.REJ.v)
                    throw new Exception();
                String author = new String(dat,1,100);
                String source = new String(dat,101,100);
                String content = new String(dat,201,821);
                NetPoem verse = new NetPoem(content,source,author);
                taskRunning = false;
                return verse;
            }catch (Exception ex){
                taskRunning = false;
                return new NetPoem("我感觉\n没有今天的\n每日一句","《错误》","勤恳的每日一句机器");
            }
        }

        //短式获取
        public static ArrayList<NetPoem> GetPoemsShort(char word, int size, int skip) {
            ArrayList<NetPoem> result = new ArrayList<>();
            try {
                String path = "http://www.shicimingju.com/chaxun/shiju/" + word;
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.connect();
                InputStream downloadstream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(downloadstream);
                char[] cbuf = new char[1024];
                StringBuilder res = new StringBuilder();
                while (reader.read(cbuf) > 0) {
                    res.append(cbuf);
                }
                reader.close();
                downloadstream.close();

                Pattern mainpattern = Pattern.compile("<div class=\"shiju_list_main\">[\\s\\S]*?href=\".*?\"");
                Matcher mainmatch = mainpattern.matcher(res);
                int count = 0;
                while (mainmatch.find() && count < size) {
                    count++;
                    for (int i = 0; i < skip - 1; ++i)
                        mainmatch.find();
                    StringBuilder versepath = new StringBuilder();
                    Pattern pathPattern = Pattern.compile("href=\".*?\"");
                    Matcher pathMathcer = pathPattern.matcher(mainmatch.group());
                    if (pathMathcer.find())
                        versepath = new StringBuilder(pathMathcer.group());
                    for (int j = 0; j < 6; ++j)
                        versepath.deleteCharAt(0);
                    versepath.deleteCharAt(versepath.length() - 1);

                    HttpURLConnection verseConnection = (HttpURLConnection) new URL("http://www.shicimingju.com" + versepath.toString()).openConnection();
                    verseConnection.setRequestMethod("GET");
                    verseConnection.setConnectTimeout(5000);
                    verseConnection.connect();
                    downloadstream = verseConnection.getInputStream();
                    reader = new InputStreamReader(downloadstream);
                    cbuf = new char[1024];
                    StringBuilder subres = new StringBuilder();
                    while (reader.read(cbuf) > 0) {
                        subres.append(cbuf);
                    }
                    reader.close();
                    downloadstream.close();

                    Pattern contentPattern = Pattern.compile("<div id=\"item_div\"[\\S\\s]*?<script");
                    Matcher contentMachter = contentPattern.matcher(subres);


                    StringBuilder verse = new StringBuilder();
                    if (contentMachter.find()) {
                        Pattern poemPattern = Pattern.compile("<div class=\"item_content\"[\\S\\s]*?</div");
                        Matcher poemMachter = poemPattern.matcher(contentMachter.group());
                        if (poemMachter.find()) {
                            StringBuilder t_verse = GetBasicVerse(new StringBuilder(poemMachter.group()),
                                    Pattern.compile("[>。？！；][\\S\\s]*?" + word + ".*?[。？！；<]"));
                            char[] verseBytes = new char[t_verse.length()];
                            t_verse.getChars(0, t_verse.length(), verseBytes, 0);
                            for (int i = 0; i < verseBytes.length; ++i)
                                if (String.valueOf(verseBytes[i]).getBytes().length > 1)
                                    verse.append(verseBytes[i]);
                        }

                        StringBuilder title = new StringBuilder();
                        Matcher titleMachter = Pattern.compile("<h1>.*?</h1>").matcher(contentMachter.group());
                        if (titleMachter.find()) {
                            char[] titleChars = new char[titleMachter.group().length()];
                            titleMachter.group().getChars(0, titleMachter.group().length(), titleChars, 0);
                            for (int i = 0; i < titleChars.length; ++i)
                                if (String.valueOf(titleChars[i]).getBytes().length > 1)
                                    title.append(titleChars[i]);
                        }

                        StringBuilder author = new StringBuilder();
                        Matcher authorMachter = Pattern.compile("zuozhe\">[\\s\\S]*?</a>").matcher(contentMachter.group());
                        if (authorMachter.find()) {
                            char[] authorChars = new char[authorMachter.group().length()];
                            authorMachter.group().getChars(0, authorMachter.group().length(), authorChars, 0);
                            for (int i = 0; i < authorChars.length; ++i)
                                if (String.valueOf(authorChars[i]).getBytes().length > 1 || authorChars[i] == '[' || authorChars[i] == ']')
                                    author.append(authorChars[i]);
                        }
                        result.add(new NetPoem(verse.toString(), title.toString(), author.toString()));
                    }
                }
                Collections.shuffle(result);
                return result;
            } catch (Exception ex) {
                Toast.makeText(NetIO.appcontext, "飞花令出错辣" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                return result;
            }
        }

        //完整获取
        public static ArrayList<NetPoem> GetPoems(char word, int page) {
            ArrayList<NetPoem> result = new ArrayList<>();
            try {
                String path = "http://www.shicimingju.com/chaxun/shiju/nd_0/" + word + "/" + page + "/0";
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.connect();
                InputStream downloadstream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(downloadstream);
                char[] cbuf = new char[1024];
                StringBuilder res = new StringBuilder();
                while (reader.read(cbuf) > 0) {
                    res.append(cbuf);
                }
                reader.close();
                downloadstream.close();

                Pattern mainpattern = Pattern.compile("<div class=\"shiju_list_main\">[\\s\\S]*?href=\".*?\"");
                Matcher mainmatch = mainpattern.matcher(res);
                while (mainmatch.find()) {
                    StringBuilder versepath = new StringBuilder();
                    Pattern pathPattern = Pattern.compile("href=\".*?\"");
                    Matcher pathMathcer = pathPattern.matcher(mainmatch.group());
                    if (pathMathcer.find())
                        versepath = new StringBuilder(pathMathcer.group());
                    for (int j = 0; j < 6; ++j)
                        versepath.deleteCharAt(0);
                    versepath.deleteCharAt(versepath.length() - 1);

                    HttpURLConnection verseConnection = (HttpURLConnection) new URL("http://www.shicimingju.com" + versepath.toString()).openConnection();
                    verseConnection.setRequestMethod("GET");
                    verseConnection.setConnectTimeout(5000);
                    verseConnection.connect();
                    downloadstream = verseConnection.getInputStream();
                    reader = new InputStreamReader(downloadstream);
                    cbuf = new char[1024];
                    StringBuilder subres = new StringBuilder();
                    while (reader.read(cbuf) > 0) {
                        subres.append(cbuf);
                    }
                    reader.close();
                    downloadstream.close();

                    Pattern contentPattern = Pattern.compile("<div id=\"item_div\"[\\S\\s]*?<script");
                    Matcher contentMachter = contentPattern.matcher(subres);


                    StringBuilder verse = new StringBuilder();
                    if (contentMachter.find()) {
                        Pattern poemPattern = Pattern.compile("<div class=\"item_content\"[\\S\\s]*?</div");
                        Matcher poemMachter = poemPattern.matcher(contentMachter.group());
                        if (poemMachter.find()) {
                            StringBuilder t_verse = GetBasicVerse(new StringBuilder(poemMachter.group()),
                                    Pattern.compile("[>。？！；][\\S\\s]*?" + word + ".*?[。？！；<]"));
                            char[] verseBytes = new char[t_verse.length()];
                            t_verse.getChars(0, t_verse.length(), verseBytes, 0);
                            for (int i = 0; i < verseBytes.length; ++i)
                                if (String.valueOf(verseBytes[i]).getBytes().length > 1)
                                    verse.append(verseBytes[i]);
                        }

                        StringBuilder title = new StringBuilder();
                        Matcher titleMachter = Pattern.compile("<h1>.*?</h1>").matcher(contentMachter.group());
                        if (titleMachter.find()) {
                            char[] titleChars = new char[titleMachter.group().length()];
                            titleMachter.group().getChars(0, titleMachter.group().length(), titleChars, 0);
                            for (int i = 0; i < titleChars.length; ++i)
                                if (String.valueOf(titleChars[i]).getBytes().length > 1)
                                    title.append(titleChars[i]);
                        }

                        StringBuilder author = new StringBuilder();
                        Matcher authorMachter = Pattern.compile("zuozhe\">[\\s\\S]*?</a>").matcher(contentMachter.group());
                        if (authorMachter.find()) {
                            char[] authorChars = new char[authorMachter.group().length()];
                            authorMachter.group().getChars(0, authorMachter.group().length(), authorChars, 0);
                            for (int i = 0; i < authorChars.length; ++i)
                                if (String.valueOf(authorChars[i]).getBytes().length > 1 || authorChars[i] == '[' || authorChars[i] == ']')
                                    author.append(authorChars[i]);
                        }
                        result.add(new NetPoem(verse.toString(), title.toString(), author.toString()));
                    }
                }
                Collections.shuffle(result);
                return result;
            } catch (Exception ex) {
                //Toast.makeText(NetIO.appcontext,"飞花令出错辣"+ex.getMessage(),Toast.LENGTH_SHORT).show();
                Collections.shuffle(result);
                return result;
            }
        }

        public static boolean CheckVerseExists(String verse) {
            try {
                String path = "https://so.gushiwen.org/search.aspx?value=" + verse;
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.connect();
                InputStream downloadstream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(downloadstream);
                char[] cbuf = new char[1024];
                StringBuilder res = new StringBuilder();
                while (reader.read(cbuf) > 0) {
                    res.append(cbuf);
                }
                reader.close();
                downloadstream.close();

                Pattern mainpattern = Pattern.compile("[！？。>\\s]<span style=\"color:#B00815\">([\\s\\S]*?)</span>");
                Matcher mainmatch = mainpattern.matcher(res);
                if (mainmatch.find()) {
                    if (mainmatch.group(1).equals(verse))
                        return true;
                }
                return false;
            } catch (Exception ex) {
                return false;
            }
        }

        private static ByteBuffer bytebuffer = ByteBuffer.allocate(1024);

        private static byte[] ShortToByte(short s) {
            bytebuffer.putShort(0, s);
            return bytebuffer.array();
        }

        private static short ByteToShort(byte b[], int offset, int len) {
            bytebuffer.put(b, offset, len);
            return bytebuffer.getShort();
        }

        public static byte[] intToBytes(int a) {
            byte[] byteArray = new byte[4];
            byteArray[0] = (byte) (a & 0xFF);
            byteArray[1] = (byte) (a >> 8 & 0xFF);
            byteArray[2] = (byte) (a >> 16 & 0xFF);
            byteArray[3] = (byte) (a >> 24 & 0xFF);
            return byteArray;
        }

        public static int ByteToInt(byte[] b, int offset, int len) {
            int a = 0;
            byte[] bs = new byte[len];
            for (int i = 0; i < len; ++i)
                bs[bs.length - i - 1] = b[i + offset];
            int i = 0;
            i += ((bs[0] & 0xff) << 24);
            i += ((bs[1] & 0xff) << 16);
            i += ((bs[2] & 0xff) << 8);
            i += ((bs[3] & 0xff));
            return i;
        }

        public static StringBuilder GetBasicVerse(StringBuilder source, Pattern pattern) {
            Matcher matcher = pattern.matcher(source);
            //如果尚可继续匹配，则继续匹配
            if (matcher.find()) {
                source = new StringBuilder(matcher.group());
                source.deleteCharAt(0);
                return GetBasicVerse(source, pattern);
            }
            return source;
        }
    }

    enum C1 {
        WFCM((byte) 100),
        LOGIN((byte) 101),
        REG((byte) 102),
        GTART((byte) 103),
        GLIST((byte) 104),
        ARTINI((byte) 105),
        DLART((byte) 106),
        ARTDAT((byte) 107),
        COMTUP((byte) 108),
        COMTDL((byte) 109),
        GTCMT((byte) 110),
        GTSTAT((byte) 111),
        STAT((byte) 112),
        SETSTAT((byte) 113),
        REQFO((byte)114),
        REQDV((byte)115),
        DV((byte)116),
        PWFAIL((byte) 200),
        USFAIL((byte) 201),
        REGSUCS((byte) 202),
        LOGSUCS((byte) 203),
        BNULL((byte) 204),
        BADCODE((byte) 205),
        DATSUCS((byte) 206),
        DATFAIL((byte) 207),
        REJ((byte) 208),
        ALIVE((byte) 222);
        byte v;

        C1(byte value) {
            v = value;
        }
    }

    enum C2 {
        STOP((byte) 0xFF),
        KEEP((byte) 0XDD),
        PARTEND((byte) 0xEE);
        byte v;

        C2(byte value) {
            v = value;
        }
    }
}
