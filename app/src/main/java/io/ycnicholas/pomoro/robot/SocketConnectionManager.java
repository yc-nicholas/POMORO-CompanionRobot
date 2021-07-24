package io.ycnicholas.pomoro.robot;

import android.os.Handler;
import android.os.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import io.ycnicholas.pomoro.constant.Command;

/**
 * Created by Akexorcist on 9/5/15 AD.
 * This class is created to manage socket connection on the robot side. (server)
 */
public class SocketConnectionManager {
    private ConnectionListener connectionListener;
    private ControllerCommandListener commandListener;
    private SendCommandListener sendListener;
    private OutputStream out;
    private DataOutputStream dos;
    private RobotService robotService;
    private String password;


    public SocketConnectionManager(String password) {
        this.password = password;
    }

    public void setConnectionListener(ConnectionListener listener) {
        connectionListener = listener;
    }

    public void setCommandListener(ControllerCommandListener listener) {
        commandListener = listener;
    }

    public void setSendCommandListener(SendCommandListener sendListener) {
        this.sendListener = sendListener;
    }

    public void start() {
        robotService = new RobotService(mHandler, password);
        robotService.execute();
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            onDataIncoming();
            int messageType = msg.what;
            if (messageType == Command.MESSAGE_PASS) {
                onControllerConnected((Socket) msg.obj);
            } else if (messageType == Command.MESSAGE_WRONG) {
                onControllerPasswordWrong((Socket) msg.obj);
            } else if (messageType == Command.MESSAGE_DISCONNECTED) {
                onControllerDisconnected();
            } else if (messageType == Command.MESSAGE_CLOSE) {
                onControllerClosed();
            } else if (messageType == Command.MESSAGE_FLASH) {
                onFlashCommand(msg.obj.toString());
            } else if (messageType == Command.MESSAGE_SNAP) {
                onRequestTakePicture();
            } else if (messageType == Command.MESSAGE_FOCUS) {
                onRequestAutoFocus();
            } else if (messageType == Command.MESSAGE_UP) {
                onMoveForwardCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_UPRIGHT) {
                onMoveForwardRightCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_UPLEFT) {
                onMoveForwardLeftCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_DOWN) {
                onMoveBackwardCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_DOWNRIGHT) {
                onMoveBackwardRightCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_DOWNLEFT) {
                onMoveBackwardLeftCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_RIGHT) {
                onMoveRightCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_LEFT) {
                onMoveLeftCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_STOP) {
                onMoveStopCommand();
            } else if (messageType == Command.MESSAGE_PT_LEFT) {
                onPanLeftCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_PT_RIGHT) {
                onPanRightCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_PT_UP) {
                onTiltUpCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_PT_DOWN) {
                onTiltDownCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_PT_STOP) {
                onPanTiltStopCommand();
            }
        }
    };

    public void onDataIncoming() {
        if(connectionListener != null)
            connectionListener.onDataIncoming();
    }

    public void onControllerConnected(Socket socket) {
        try {
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);
            if(connectionListener != null)
                connectionListener.onControllerConnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onControllerPasswordWrong(Socket socket) {
        try {
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);
            restart();
            if(connectionListener != null)
                connectionListener.onWrongPassword();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onControllerDisconnected() {
        restart();
        if(connectionListener != null)
            connectionListener.onControllerDisconnected();
    }

    public void onControllerClosed() {
        restart();
        if(connectionListener != null)
            connectionListener.onControllerClosed();
    }

    public void onFlashCommand(String command) {
        if(commandListener != null)
            commandListener.onFlashCommand(command);
    }

    public void onRequestTakePicture() {
        if(commandListener != null)
            commandListener.onRequestTakePicture();
    }

    public void onRequestAutoFocus() {
        if(commandListener != null)
            commandListener.onRequestAutoFocus();
    }

    public void onMoveForwardCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveForwardCommand(speed);
    }

    public void onMoveForwardRightCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveForwardRightCommand(speed);
    }

    public void onMoveForwardLeftCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveForwardLeftCommand(speed);
    }

    public void onMoveBackwardCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveBackwardCommand(speed);
    }

    public void onMoveBackwardRightCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveBackwardRightCommand(speed);
    }

    public void onMoveBackwardLeftCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveBackwardLeftCommand(speed);
    }

    public void onMoveRightCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveRightCommand(speed);
    }

    public void onMoveLeftCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveLeftCommand(speed);
    }

    public void onMoveStopCommand() {
        if(commandListener != null)
            commandListener.onMoveStopCommand();
    }

    public void stop() {
        if (robotService != null)
            robotService.killTask();
    }

    public void onPanLeftCommand(int speed){
        if(commandListener != null)
            commandListener.onPanLeftCommand(speed);
    }

    public void onPanRightCommand(int speed){
        if(commandListener != null)
            commandListener.onPanRightCommand(speed);
    }

    public void onTiltUpCommand(int speed){
        if(commandListener != null)
            commandListener.onTiltUpCommand(speed);
    }

    public void onTiltDownCommand(int speed){
        if(commandListener != null)
            commandListener.onTiltDownCommand(speed);
    }

    public void onPanTiltStopCommand() {
        if(commandListener != null)
            commandListener.onPanTiltStopCommand();
    }

    public void restart() {
        stop();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                start();
            }
        }, 1000);
    }

    public void sendImageData(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dos.writeInt(data.length);
                    dos.write(data);
                    out.flush();
                    if(sendListener != null)
                        sendListener.onSendCommandSuccess();
                } catch (IOException e) {
                    e.printStackTrace();
                    if(sendListener != null)
                        sendListener.onSendCommandFailure();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendCommand(final String str) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dos.writeInt(str.length());
                    dos.write(str.getBytes());
                    out.flush();
                    if(sendListener != null)
                        sendListener.onSendCommandSuccess();
                } catch (IOException e) {
                    e.printStackTrace();
                    if(sendListener != null)
                        sendListener.onSendCommandFailure();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface ConnectionListener {
        public void onControllerConnected();
        public void onWrongPassword();
        public void onControllerDisconnected();
        public void onControllerClosed();
        public void onDataIncoming();
    }

    public interface ControllerCommandListener {
        public void onFlashCommand(String command);
        public void onRequestTakePicture();
        public void onRequestAutoFocus();
        public void onMoveForwardCommand(int speed);
        public void onMoveForwardRightCommand(int speed);
        public void onMoveForwardLeftCommand(int speed);
        public void onMoveBackwardCommand(int speed);
        public void onMoveBackwardRightCommand(int speed);
        public void onMoveBackwardLeftCommand(int speed);
        public void onMoveLeftCommand(int speed);
        public void onMoveRightCommand(int speed);
        public void onMoveStopCommand();
        public void onPanLeftCommand(int speed);
        public void onPanRightCommand(int speed);
        public void onTiltUpCommand(int speed);
        public void onTiltDownCommand(int speed);
        public void onPanTiltStopCommand();
    }

    public interface SendCommandListener {
        public void onSendCommandSuccess();
        public void onSendCommandFailure();
    }
}
