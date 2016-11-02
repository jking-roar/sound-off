package com.kingjoshdavid;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


public class Look extends JPanel implements ActionListener {

    private final Visualizer visualizer;
    private final JCheckBox visualizeCheckBox;

    public Look() {
        setLayout(new BorderLayout());
        visualizeCheckBox = new JCheckBox("Visualize");
        WaveForm waveForm = new WaveForm();
        visualizer = new Visualizer(waveForm);

        visualizeCheckBox.addActionListener(this);
        visualizeCheckBox.setSelected(visualizer.visualize);

        add(visualizeCheckBox, BorderLayout.WEST);
        add(visualizer, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        final Look look = new Look();
        JFrame f = new JFrame("Look");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setContentPane(look);
        f.pack();
        f.setVisible(true);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                look.stop();
            }
        });
    }

    private void stop() {
        visualizer.stop();
    }

    public void actionPerformed(ActionEvent e) {
        visualizer.setVisualize(visualizeCheckBox.isSelected());
    }

    private static class Visualizer extends JPanel {
        private boolean visualize = true;
        private WaveForm waveForm;
        private boolean running = true;

        public Visualizer(WaveForm waveForm) {
            this.waveForm = waveForm;
            new Thread() {
                @Override
                public void run() {
                    running = true;
                    while (running) {
                        repaint();
                        try {
                            Thread.sleep(50l);
                        } catch (InterruptedException ignored) {

                        }
                    }
                }
            }.start();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 100);
        }

        public void setVisualize(boolean visualize) {
            this.visualize = visualize;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(Color.white);
            double width = getWidth();
            double height = getHeight();
            g2.fill(new Rectangle2D.Double(0, 0, width, height));


            if (visualize) {
                g2.setPaint(Color.green);
                double MID_HEIGHT = height / 2;

                double[] signal = waveForm.getSignal();
                double maxMag = 1;
                for (double v : signal) {
                    maxMag = Math.max(maxMag, Math.abs(v));
                }
                maxMag = maxMag * 1.05;

                Point2D prev = new Point2D.Double(0, MID_HEIGHT - MID_HEIGHT * signal[0] / maxMag);
                double tick = width / (signal.length - 1);
                for (int i = 1; i < signal.length; i++) {
                    Point2D next = new Point2D.Double(i * tick, MID_HEIGHT - MID_HEIGHT * signal[i] / maxMag);
                    g2.draw(new Line2D.Double(prev, next));
                    prev = next;
                }

                g2.setPaint(Color.black);
                g2.draw(new Line2D.Double(0, MID_HEIGHT, width, MID_HEIGHT));
            }
        }

        public void stop() {
            waveForm.stop();
            running = false;
        }
    }
}

class WaveForm {
    private final TargetDataLine line;
    private final int sampleSizeInBits;
    private final int channels;
    private final int sampleRate = 8000;
    private byte[] signal = new byte[sampleRate];
    private boolean running = true;

    public WaveForm() {
        try {
            sampleSizeInBits = 8;
            channels = 1;
            AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, true, true);
            line = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, audioFormat));
            line.open();
            line.start();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    while (running) {
                        line.read(signal, 0, signal.length);
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            };
            thread.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public double[] getSignal() {
        double[] signal = new double[this.signal.length];
        for (int i = 0; i < this.signal.length; i++) {
            signal[i] = this.signal[i];
        }
        return signal;
    }

    public void stop() {
        this.running = false;
    }
}