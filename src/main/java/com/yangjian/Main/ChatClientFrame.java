package com.yangjian.Main;

import com.yangjian.entity.Message;
import com.yangjian.util.Comment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClientFrame extends JFrame {
    private JTextField tf_newUser;
    private JList user_list;
    private JTextArea ta_info;
    private JTextField tf_send;
    private ObjectOutputStream out;// 声明输出流对象
    private boolean loginFlag = false;// 为true时表示已经登录，为false时表示未登录

    public static void main(String args[]) {

        ChatClientFrame frame = new ChatClientFrame();
        frame.setVisible(true);
        frame.createClientSocket();// 调用方法创建套接字对象
    }

    public void createClientSocket() {
        try {
            Socket socket = new Socket("127.0.0.1", 1982);// 创建套接字对象
            out = new ObjectOutputStream(socket.getOutputStream());// 创建输出流对象
            new ClientThread(socket).start();// 创建并启动线程对象
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientThread extends Thread {
        Socket socket;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            ObjectInputStream inputStream = null;
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            DefaultComboBoxModel model = (DefaultComboBoxModel) user_list
                    .getModel();// 获得列表框的模型
            while (true) {

                Message message = null;
                try {
                    message = (Message) inputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (message != null) {
                    String key = message.getStart();

                    if (message.getComment().equals(Comment.CUSTOMERLIST)) {

                        boolean itemFlag = false;
                        String clientKey = message.getMessage();
                        for (int i = 0; i < model.getSize(); i++) {
                            if (model.getElementAt(i).equals(clientKey)) {
                                itemFlag = true;
                                break;
                            }
                        }

                        if (!itemFlag)
                            model.addElement(message.getMessage());
//                        user_list.updateUI();
                    } else if (message.getComment().equals(Comment.DEAD))
                        model.removeElement(key);
                    else {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                        String date = simpleDateFormat.format(new Date());

                        ta_info.append("  " + message.getStart() + "    " + date + "\n  " + message.getMessage() + "\n");// 在文本域中显示信息

                        ta_info.setSelectionStart(ta_info.getText().length() - 1);// 设置选择起始位置
                        ta_info.setSelectionEnd(ta_info.getText().length());// 设置选择的结束位置
                        tf_send.requestFocusInWindow();
                    }
                }
            }
        }
    }

    private void send() {
        if (!loginFlag) {
            JOptionPane.showMessageDialog(null, "请先登录。");
            return;// 如果用户没登录则返回
        }
        String sendUserName = tf_newUser.getText().trim();// 获得登录用户名
        String info = tf_send.getText();// 获得输入的发送信息
        if (info.equals("")) {
            JOptionPane.showMessageDialog(null, "输入信息不能为空！");
            return;// 如果没输入信息则返回，即不发送
        }

        List receiveUserNames = user_list.getSelectedValuesList();// 获得选择的用户数组

        if (receiveUserNames.size() <= 0) {
            JOptionPane.showMessageDialog(null, "请选择进行会话的用户！");
            return;// 如果没选择用户则返回
        }

        for (int i = 0; i < receiveUserNames.size(); i++) {
            Message message = new Message();
            message.setStart(sendUserName);
            message.setComment(Comment.MESSAGE);
            message.setMessage(info);

            String target = receiveUserNames.get(i).toString();

            //打印目标用户的名字
            System.out.println(target);
            message.setEnd(target);
            //打印发送的信息
            System.out.println(message);
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        String date = simpleDateFormat.format(new Date());

        ta_info.append("  " + sendUserName + "    " + date + "\n  " + info + "\n");// 在文本域中显示信息
        tf_send.setText(null);// 清空文本框

        ta_info.setSelectionStart(ta_info.getText().length() - 1);// 设置选择的起始位置
        ta_info.setSelectionEnd(ta_info.getText().length());// 设置选择的结束位置
        tf_send.requestFocus();// 使发送信息文本框获得焦点
    }

    /**
     * 用户登录
     */
    private void login(){
        if (loginFlag) {// 已登录标记为true
            JOptionPane.showMessageDialog(null, "在同一窗口只能登录一次。");
            return;
        }

        String userName = tf_newUser.getText().trim();// 获得登录用户名
        if (userName.equals("")) {
            JOptionPane.showMessageDialog(null, "用户名不能为空！");
            return;
        }

        Message message = new Message();
        message.setStart(userName);
        message.setComment(Comment.CID);
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        tf_newUser.setEnabled(false);// 禁用用户文本框
        loginFlag = true;// 将已登录标记设置为true
    }

    /**
     * Create the frame
     */
    public ChatClientFrame() {
        super();

        Font font = new Font("等线",Font.PLAIN,18);
        setTitle("聊天室客户端");
        setBounds(100, 100, 550, 400);
        setFont(font);

        final JPanel panel_bottom = new JPanel();
        getContentPane().add(panel_bottom, BorderLayout.SOUTH);

        final JLabel label = new JLabel();
        label.setText("输入聊天内容：");
        label.setFont(font);
        panel_bottom.add(label);

        //发送信息的文本框
        tf_send = new JTextField();
        tf_send.setFont(font);
        tf_send.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                send();// 调用方法发送信息
            }
        });
        tf_send.setPreferredSize(new Dimension(180, 25));
        panel_bottom.add(tf_send);

        //发送按钮
        final JButton button_send = new JButton();
        button_send.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                send();// 调用方法发送信息
            }
        });
        button_send.setText("发  送");
        button_send.setFont(font);
        panel_bottom.add(button_send);

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(100);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        final JScrollPane scrollPane = new JScrollPane();
        splitPane.setRightComponent(scrollPane);

        //信息显示文本框
        ta_info = new JTextArea();
        ta_info.setFont(new Font("", Font.BOLD, 14));
        ta_info.setFont(font);
        ta_info.setEditable(false);
        scrollPane.setViewportView(ta_info);

        final JScrollPane scrollPane_list = new JScrollPane();
        splitPane.setLeftComponent(scrollPane_list);

        //用户列表框
        user_list = new JList();
        user_list.setFont(font);
        user_list.setModel(new DefaultComboBoxModel(new String[]{""}));
        scrollPane_list.setViewportView(user_list);

        //最上边的面板
        final JPanel panel_top = new JPanel();
        getContentPane().add(panel_top, BorderLayout.NORTH);

        //用户名称标签
        final JLabel label_username = new JLabel();
        label_username.setText("用户名称：");
        label_username.setFont(font);
        panel_top.add(label_username);

        //用户名称文本框
        tf_newUser = new JTextField();
        tf_newUser.setPreferredSize(new Dimension(140, 25));
        tf_newUser.setFont(font);
        tf_newUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        panel_top.add(tf_newUser);

        //登录按钮
        final JButton button_login = new JButton();
        button_login.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                login();
            }
        });
        button_login.setText("登  录");
        button_login.setFont(font);
        panel_top.add(button_login);

        //退出按钮
        final JButton button_exit = new JButton();
        button_exit.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                String exitUser = tf_newUser.getText().trim();

                Message message = new Message();
                message.setStart(exitUser);
                message.setComment(Comment.DEAD);

                try {
                    out.writeObject(message);
                    out.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                System.exit(0);                                     // 退出系统
            }
        });
        button_exit.setText("退  出");
        button_exit.setFont(font);
        panel_top.add(button_exit);
        //托盘
        if (SystemTray.isSupported()) {                                      // 判断是否支持系统托盘
            URL url = ChatClientFrame.class.getResource("/client.png");          // 获取图片所在的URL
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
            trayIcon.setToolTip("client托盘");                                    // 添加工具提示文本
            PopupMenu popupMenu = new PopupMenu();                    // 创建弹出菜单
            MenuItem exit = new MenuItem("exit");                           // 创建菜单项
            exit.addActionListener(new ActionListener() {                   // 添加事件监听器
                public void actionPerformed(final ActionEvent arg0) {
                    String exitUser = tf_newUser.getText().trim();

                    Message message = new Message();
                    message.setStart(exitUser);
                    message.setComment(Comment.DEAD);

                    try {
                        out.writeObject(message);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

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
