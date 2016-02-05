package com.bkelly.cvrig;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;


public class DebugWindow extends JFrame {

	private static final long serialVersionUID = 592170379089398969L;
	private JLabel l;
	
	public DebugWindow() {
		super("Debug");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		l = new JLabel();
		setContentPane(l);
		setSize(640,480);
		setVisible(true);
	}
	
	public void update(Mat m) {
		ImageIcon image = new ImageIcon(Mat2bufferedImage(m));
		l.setIcon(image);
		l.repaint();
	}
	
    private BufferedImage Mat2bufferedImage(Mat mat) {
        MatOfByte mBytes = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mBytes);
        byte[] bytes = mBytes.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage img = null;
        try {
            img = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
}
