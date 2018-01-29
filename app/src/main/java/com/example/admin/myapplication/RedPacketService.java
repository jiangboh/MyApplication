package com.example.admin.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

/**
 * Created by admin on 2018/1/19.
 */

public class RedPacketService extends AccessibilityService {

    static public boolean EnableDebug = false;
    /**
     * 微信几个页面的包名+地址。用于判断在哪个页面
     * LAUCHER-微信聊天界面
     * LUCKEY_MONEY_RECEIVER-点击红包弹出的界面
     * LUCKEY_MONEY_DETAIL-红包领取后的详情界面
     */
    private String LAUCHER = "com.tencent.mm.ui.LauncherUI";
    private String LUCKEY_MONEY_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private String LUCKEY_MONEY_RECEIVER = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";

    private boolean isPackRed = false;
    private boolean isNoRecv = false;
    private boolean isRedPackWindows = false;

    private AccessibilityNodeInfo redPackageNode = null;
    private String RED_PACK_ID = "com.tencent.mm.ui.LauncherUI";
    private boolean isReturn = false;


    /**
     * 服务连接
     */
    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "服务开启", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }

    /**
     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
        Toast.makeText(this, "我快被终结了啊-----", Toast.LENGTH_SHORT).show();
    }

    /**
     * 服务断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "服务已被关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    //@Override
    public void onAccessibilityEvent111(AccessibilityEvent event) {
        // TODO Auto-generated method stub
        //String TAG = "recycle ===> ";
        int eventType = event.getEventType();
        String eventText = "";
        MyPrint( "==============Start====================");
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventText = "TYPE_VIEW_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventText = "TYPE_VIEW_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                eventText = "TYPE_VIEW_LONG_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                eventText = "TYPE_VIEW_SELECTED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                eventText = "TYPE_VIEW_TEXT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                eventText = "TYPE_WINDOW_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                eventText = "TYPE_NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                eventText = "TYPE_ANNOUNCEMENT";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                eventText = "TYPE_VIEW_HOVER_ENTER";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                eventText = "TYPE_VIEW_HOVER_EXIT";
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                eventText = "TYPE_VIEW_SCROLLED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                eventText = "TYPE_VIEW_TEXT_SELECTION_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                eventText = "TYPE_WINDOW_CONTENT_CHANGED";
                break;
        }
        eventText = eventText + ":" + eventType;
        MyPrint( eventText);
        MyPrint( "=============END=====================");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();

        //通知栏来信息，判断是否含有微信红包字样，是的话跳转
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
        {
            MyPrint( "通知栏来消息啦。。。");
        }
        else if (eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            MyPrint( "TYPE_WINDOWS_CHANGED。。。");
        }
        //界面跳转的监听
        else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        {
            MyPrint( "页面换啦。。。");
            String className = event.getClassName().toString();

            MyPrint( "className:" + className);
            //判断是否是微信聊天界面
            if (LAUCHER.equals(className)) {
                //获取当前聊天页面的根布局
                //AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                MyPrint( "开始找红包");
                isRedPackWindows = true;
                //new MyThread().run();
                //new UpDownThread().run();
            }
            else
            {
                MyPrint( "停止找红包");
                isRedPackWindows = false;
            }

            //判断是否是显示‘开’的那个红包界面
            if (LUCKEY_MONEY_RECEIVER.equals(className)) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                //开始抢红包
                if (false == openRedPacket(rootNode))
                    performBack(this);
            }

            //判断是否是红包领取后的详情界面
            if (LUCKEY_MONEY_DETAIL.equals(className)) {
                performBack(this);
            }
        }
        else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            MyPrint( "窗口内容改变啦。。。");
            if (isRedPackWindows) {
                String className = event.getClassName().toString();
                MyPrint( "className:" + className);
                AccessibilityNodeInfo node = event.getSource();
                //AccessibilityNodeInfo node = getRootInActiveWindow();
                intoRedPackWindows(node);
            }

        }
        else if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            MyPrint( "TYPE_VIEW_SCROLLED");
            AccessibilityNodeInfo node = getRootInActiveWindow();
            intoRedPackWindows(node);
        }
    }

    /**
     * 开始打开红包
     */
    private boolean openRedPacket(AccessibilityNodeInfo rootNode) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if ("android.widget.Button".equals(node.getClassName())) {
                MyPrint( "领取红包。。。。。。。。。");
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
            openRedPacket(node);
        }
        return false;
    }

    //点击红包
    private boolean checkRedPackage(AccessibilityNodeInfo node)
    {
        //String TAG = "checkRedPackage ===> ";
        if (node == null)
        {
            return false;
        }
        MyPrint( "child widget----------------------------");
        MyPrint( "ClassName:" + node.getClassName());
        MyPrint( "Class:" + node.getClass().toString());
        MyPrint( "Text：" + node.getText());
        MyPrint( "windowId:" + node.getWindowId());

        AccessibilityNodeInfo parent = node.getParent();
        //while循环,遍历"领取红包"的各个父布局，直至找到可点击的为止
        while (parent != null) {
            if (parent.isClickable()) {
                //模拟点击
                MyPrint( "点击！！！");
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                //isOpenRP用于判断该红包是否点击过
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    //找到红包项
    public void recycle(int rootNum ,AccessibilityNodeInfo info) {
        //String TAG = "recycle ===> ";
        if (isReturn) return ;

        MyPrint( "[" + rootNum + "]输入：isPackRed:" + isPackRed + "; isNoRecv:" + isNoRecv);

        if (info.getChildCount() == 0) {
            MyPrint( "child widget----------------------------");
            MyPrint( "ClassName:" + info.getClassName());
            MyPrint( "Class:" + info.getClass().toString());
            MyPrint( "Text：" + info.getText());
            MyPrint( "windowId:" + info.getWindowId());
            if (info.getText() != null)
            {
                if (info.getText().toString().equals("微信红包"))
                    isPackRed = true;
                if (info.getText().toString().equals("领取红包"))
                    isNoRecv = true;
                MyPrint( "比较结果(isPackRed:" + isPackRed + "; isNoRecv:" + isNoRecv);
                if ((isPackRed == true) && (isNoRecv == true))
                {
                    MyPrint( "退出程序。。。。。。。。。。。");
                    isReturn = true;
                    redPackageNode = info;
                }
            }
        } else {
            rootNum ++ ;
            for (int i = info.getChildCount() -1; i>=0; i--) {
                if(info.getChild(i)!=null){
                    MyPrint( "[" + rootNum + "]再次查找(" + i +")。。。");
                    recycle(rootNum,info.getChild(i));
                }
            }
        }
        rootNum --;
        MyPrint( "[" + rootNum + "]查完！！！");
        return;
    }

    private AccessibilityNodeInfo findRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            //从最后一行开始找起
            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                //如果node为空则跳过该节点
                if (node == null) {
                    continue;
                }
                if ((node.getText() != null) && (node.getText().toString().equals("微信红包")))
                {
                    return node;
                }
                else
                {
                    findRedPacket(node.getChild(node.getChildCount()-1));
                }

            }
        }
        return null;
    }


    //模拟返回事件
    public void performBack(AccessibilityService service) {
        if(service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public void performPower(AccessibilityService service) {
        if(service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
    }

    private void MyPrint(String str)
    {
        if (!EnableDebug) return;
        String TAG = "myPrint:";
        Log.i(TAG,  str );
    }
    
    public void intoRedPackWindows(AccessibilityNodeInfo node)
    {
        //String TAG = "intoRedPackWindows";
        isReturn = false;
        redPackageNode = null;
        isPackRed = false;
        isNoRecv = false;

        recycle(1,node);
        //找到红包
        if (redPackageNode != null)
        {
            if (checkRedPackage(redPackageNode))
            {
                MyPrint( "进入了“开”界面。。。" );
                return;
            }
        }
        else
        {
            MyPrint( "未查询到，继续查询。。。");
        }
    }

    public class MyThread extends Thread {
        //继承Thread类，并改写其run方法
        //private final static //String TAG = "My Thread ===> ";
        public void run(){
            MyPrint( "run");
            int i = 0;
            while(true)
            {
                i++;
                MyPrint( "run----" + i );
                //获取当前聊天页面的根布局
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode == null || rootNode.getPackageName() == null)
                    continue;

                MyPrint( "packageName:" + rootNode.getPackageName().toString() );
                if (!rootNode.getPackageName().toString().equals("com.tencent.mm"))
                    return;
                //Toast.makeText(this, "开始找红包", Toast.LENGTH_SHORT).show();
                //开始找红包
                isReturn = false;
            }
        }
    }

    public class UpDownThread extends Thread {
        //继承Thread类，并改写其run方法
        //private final static //String TAG = "My Thread ===> ";
        public void run(){
            MyPrint( "run");
            while(true)
            {
                //获取当前聊天页面的根布局
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode == null || rootNode.getPackageName() == null)
                    continue;

                MyPrint( "packageName:" + rootNode.getPackageName().toString() );
                if (!rootNode.getPackageName().toString().equals("com.tencent.mm"))
                    return;

                rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                try {
                    Thread.sleep(2000);//延时1s
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                try {
                    Thread.sleep(2000);//延时1s
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



}
