/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */

package tigase.monitor.util;

import tigase.monitor.MonitorMain;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;

/**
 * Created by wojtek on 19/07/16.
 */
public class AlertDialogDisplay extends Thread implements LineListener {

    private MonitorMain monitorMain;
    private int type = Integer.MIN_VALUE;
    private String title = null;
    private String message = null;
    private Clip clip = null;
    private File soundFile = new File("alarm1.wav");

    public AlertDialogDisplay(MonitorMain monitorMain, String alarmFile) {
        super();
        this.monitorMain = monitorMain;
        soundFile = new File(alarmFile);
        try {
            // get and play sound
            Line.Info linfo = new Line.Info(Clip.class);
            Line line = AudioSystem.getLine(linfo);
            clip = (Clip) line;
            clip.addLineListener(this);
            // clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void wakeup(int type, String title, String message) {
        this.type = type;
        this.title = title;
        this.message = message;
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    this.wait();
                }
                // Guard from spontaneous awaking from wait.
                if (title != null) {
                    if (type == JOptionPane.ERROR_MESSAGE) {
                        try {
                            AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
                            clip.open(ais);
                            clip.loop(Clip.LOOP_CONTINUOUSLY);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    JOptionPane.showMessageDialog(monitorMain, message, title, type);
                    title = null;
                    if (type == JOptionPane.ERROR_MESSAGE) {
                        try {
                            clip.stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (InterruptedException ex) {
            }
        }
    }

    public void update(LineEvent le) {
        LineEvent.Type type = le.getType();
        if (type == LineEvent.Type.OPEN) {
            // System.out.println("OPEN");
        } else if (type == LineEvent.Type.CLOSE) {
            // System.out.println("CLOSE");
            // System.exit(0);
        } else if (type == LineEvent.Type.START) {
            // System.out.println("START");
            // playingDialog.setVisible(true);
        } else if (type == LineEvent.Type.STOP) {
            // System.out.println("STOP");
            // playingDialog.setVisible(false);
            clip.close();
        }

    }

}
