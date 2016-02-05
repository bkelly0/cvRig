package com.bkelly.cvrig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class Serial implements SerialPortEventListener {

	public static final int DEFAULT_BAUD = 9600;
	
	private SerialPort serialPort;
	private OutputStream out;
	private InputStream in;
	private boolean isConnected = false;

	void connect(String portName)  {
		this.connect(portName, DEFAULT_BAUD);
	}
	
    void connect ( String portName, int baud ) {
	    try {
	        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	        if ( portIdentifier.isCurrentlyOwned() )
	        {
	            System.out.println("Port is already is use: " + portName);
	        }
	        else
	        {
	            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
	            
	            if ( commPort instanceof SerialPort )
	            {
	                serialPort = (SerialPort) commPort;
	                serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	                
	                serialPort.addEventListener(this);
	                serialPort.notifyOnDataAvailable(true);
	                
	                out = serialPort.getOutputStream();
	                in = serialPort.getInputStream();
	            }
	            else
	            {
	                System.out.println("Port is not serial port: " + portName);
	            }
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    isConnected = true;
    }
    
    void write(byte [] ba) {
    	try {
    		if (out != null) {
				out.write(ba);
				out.flush();
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    void disconnect() {
    	isConnected = false;
    	serialPort.removeEventListener();
    	serialPort.close();
    	try {
			out.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
	 
	@Override
	public void serialEvent(SerialPortEvent e) {
		// TODO Auto-generated method stub
		 if (e.getEventType() == SerialPortEvent.DATA_AVAILABLE) {     
			byte b;
			try {
				b = (byte)in.read();
				System.out.println((char)b);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
           
	     }
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}	
}
