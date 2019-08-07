package com.yangjian.Main;

import com.yangjian.entity.Message;
import com.yangjian.util.Comment;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ChatServerFrame extends JFrame {
    private JTextArea ta_info;
    private ServerSocket server; // 声明ServerSocket对象
    private Hashtable<String, Socket> map = new Hashtable<>();// 用于存储连接到服务器的用户和客户端套接字对象
    private Hashtable<String, ObjectInputStream> inputMap = new Hashtable<>();// 用于存储对应用户的读取流对象
    private Hashtable<String, ObjectOutputStream> outputMap = new Hashtable<>();//用于存储对应用户的输入流对象

    public void createSocket() {
        try {
            server = new ServerSocket(1982);// 创建服务器套接字对象
            while (true) {
                ta_info.append("等待新客户连接......\n");
                Socket socket = server.accept();// 获得套接字对象
                ta_info.append("客户端连接成功。" + socket + "\n");
                new ServerThread(socket).start();// 创建并启动线程对象
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread extends Thread {
        Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

                while (true) {

                    Message message = null;
                    try {
                        message = (Message) inputStream.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (message != null) {

                        System.out.println(message);
                        String key = message.getStart();
                        if (message.getComment().equals(Comment.CID)) {
                            map.put(key, socket);
                            inputMap.put(key, inputStream);
                            outputMap.put(key, outputStream);

                            Message customerList;

                            //为每一个client添加当前用户的key
                            for (String clientKey :
                                    map.keySet()) {
                                ObjectOutputStream objectOutputStream = outputMap.get(clientKey);
                                customerList = new Message();
                                customerList.setMessage(key);
                                customerList.setComment(Comment.CUSTOMERLIST);
                                objectOutputStream.writeObject(customerList);
                                objectOutputStream.flush();
                            }

                            //为当前用户添加其他client的key
                            for (String clientKey :
                                    map.keySet()) {
                                if (clientKey.equals(key)) continue;
                                customerList = new Message();
                                customerList.setMessage(clientKey);
                                customerList.setComment(Comment.CUSTOMERLIST);
                                outputStream.writeObject(customerList);
                                outputStream.flush();
                            }
                        } else if (message.getComment().equals(Comment.DEAD)) {

                            map.remove(key);
                            inputMap.remove(key);
                            outputMap.remove(key);

                            //通知所有的活着的client，删除死亡的key；
                            for (String clientKey :
                                    map.keySet()) {
                                ObjectOutputStream objectOutputStream = outputMap.get(clientKey);
                                objectOutputStream.writeObject(message);
                                objectOutputStream.flush();
                            }
                        } else {

                            String target = message.getEnd();

                            ObjectOutputStream objectOutputStream = outputMap.get(target);
                            objectOutputStream.writeObject(message);
                            objectOutputStream.flush();
                        }
                    }
                }
            } catch (IOException e) {
                ta_info.append(socket + "已经退出。\n");
            }
        }
    }

    public static void main(String args[]) {
        ChatServerFrame frame = new ChatServerFrame();
        frame.setVisible(true);
        frame.createSocket();
    }

    /**
     * Create the frame
     */
    public ChatServerFrame() {
        super();
        this.addWindowListener(new WindowAdapter() {
            //当窗口设置为最小的模式时，隐藏当前窗口。
            public void windowIconified(final WindowEvent e) {
                setVisible(false);
            }
        });

        Font font = new Font("等线",Font.PLAIN,18);
        setTitle("聊天室服务器端");
        setBounds(100, 100, 500, 400);
        setFont(font);
        final JScrollPane scrollPane = new JScrollPane();
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

        ta_info = new JTextArea();
        ta_info.setFont(font);
        ta_info.setEditable(false);

        scrollPane.setViewportView(ta_info);

        //托盘
        if (SystemTray.isSupported()) {                                      // 判断是否支持系统托盘
            URL url = ChatServerFrame.class.getResource("/server.png");          // 获取图片所在的URL
            ImageIcon icon = new ImageIcon(url);                            // 实例化图像对象
            Image image = icon.getImage();                                    // 获得Image对象
            TrayIcon trayIcon = new TrayIcon(image);                          // 创建托盘图标
            trayIcon.addMouseListener(new MouseAdapter() {                   // 为托盘添加鼠标适配器
                public void mouseClicked(MouseEvent e) {                     // 鼠标事件
                    if (e.getClickCount() == 2) {                              // 判断是否双击了鼠标
                        showFrame();                                    // 调用方法显示窗体
                    }
                }
            });
            trayIcon.setToolTip("server托盘");                                    // 添加工具提示文本
            PopupMenu popupMenu = new PopupMenu();                    // 创建弹出菜单
            MenuItem exit = new MenuItem("exit");                           // 创建菜单项
            exit.addActionListener(new ActionListener() {                   // 添加事件监听器
                public void actionPerformed(final ActionEvent arg0) {
                    System.exit(0);                                     // 退出系统
                }
            });
            popupMenu.add(exit);                                        // 为弹出菜单添加菜单项
            trayIcon.setPopupMenu(popupMenu);                           // 为托盘图标加弹出菜弹
            SystemTray systemTray = SystemTray.getSystemTray();           // 获得系统托盘对象
            try {
                systemTray.add(trayIcon);                               // 为系统托盘加托盘图标
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showFrame() {
        this.setVisible(true);                                              // 显示窗体
        this.setState(Frame.NORMAL);
    }
}
