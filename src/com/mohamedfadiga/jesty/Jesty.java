package com.mohamedfadiga.jesty;

import java.awt.EventQueue;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import org.json.JSONArray;
import org.json.JSONObject;


public class Jesty implements SerialPortEventListener
{
	private int gestNum = 15;
	private String[] labels = {"Up", "Down", "Left", "Right", "Left - Right", "Right - Left", "Backward", "Forward", "Up - Down", 
			"Down - Up", "Forward - Backward", "Backward - Forward", "Clockwise", "Anti-clockwise", "Wave"}; //All the available Gesture 
	private int[] keys = new int[gestNum]; //Array containing the key codes or mouse button associated to each gesture 
	private boolean[] status = new boolean[gestNum]; //Array containing the status of each gesture
	private String path; 
	private JFrame frame;
	private SerialPort serialPort;
	private static Jesty jesty;
	private Robot r; //A Robot object for automated actions like typing keys and clicking mouse button 
	private JSONArray jA;
	
	public static void main(String[] args) 
	{   
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					jesty = new Jesty();
					jesty.frame.setVisible(true);
				} 
				catch (Exception e){e.printStackTrace();}
			}
		});
	}

	public Jesty() {initialize();}

	private void initialize() 
	{
		try 
		{ 
			r = new Robot();
			path =  new File(Jesty.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
					.getParentFile().getAbsolutePath(); //Gets the folder where Jesty.jar is located
			//Reads previously stored data
			BufferedReader br;
			br = new BufferedReader(new FileReader(path+"\\data.json"));
			String line; 
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null)sb.append(line);
			br.close();
			jA = new JSONArray(sb.toString());
		} 
		catch (Exception e){}  
		
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 320, 620);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); //Because we want to save data before closing the program
        frame.addWindowListener( new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent we) //When the program is closing
            {
            	try 
            	{
            		jA = new JSONArray();
            		for(int i=0;i < gestNum;++i) //puts data into a JSON Array
            		{
            			JSONObject  jO = new JSONObject();
            			jO.put("key", keys[i]);
            			jO.put("active", status[i]);
            			jA.put(i, jO);
            		}           		
            		
            		//Writes JSON to a file 
            		PrintWriter writer = new PrintWriter(path + "\\data.json", "UTF-8");
            		writer.print(jA.toString());
            		writer.close();
            	} 
            	catch (Exception e){}
            	System.exit(0);
            }
        });
		
        frame.getContentPane().setLayout(null);
		
		JComboBox<String> comboBox = new JComboBox<String>(SerialPortList.getPortNames());
		comboBox.setBounds(12, 10, 100, 22);
		frame.getContentPane().add(comboBox);

		JButton btnStart = new JButton("START");
		btnStart.setBounds(160, 9, 97, 25);
		btnStart.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) //When we press btnStart 
			{
				try 
				{
					if((serialPort == null || !serialPort.isOpened())&& (comboBox.getSelectedItem() != null))
					{
						//Creates, opens and setup serial port  
						serialPort =  new SerialPort(comboBox.getSelectedItem().toString());
						serialPort.openPort();
						serialPort.setParams(SerialPort.BAUDRATE_115200,
							SerialPort.DATABITS_8,
					        SerialPort.STOPBITS_1,
					        SerialPort.PARITY_NONE);
						serialPort.addEventListener(jesty, SerialPort.MASK_RXCHAR);
						btnStart.setText("STOP");
					}
					else if((serialPort != null && serialPort.isOpened()))
					{
						btnStart.setText("START");
						serialPort.closePort();
					}
				} 
				catch (SerialPortException e){e.printStackTrace();}
			}
		});
		frame.getContentPane().add(btnStart);		

		KeyListener keyListener = new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				JButton button = (JButton)e.getSource(); //Get the button that generated the event
				button.removeKeyListener(this);
				int code =e.getKeyCode(); 
				if(code == 27)return; //if we press "Esc" key							
				button.setText(""+KeyEvent.getKeyText(code)); //Change the button's label to the key associated to this code
				keys[Integer.parseInt(button.getName())] = code;
			}

			@Override
			public void keyReleased(KeyEvent e){}

			@Override
			public void keyTyped(KeyEvent e){}
		};
		
		MouseListener mouseListener = new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int b = e.getButton(); //Get which mouse button were clicked 
				JButton button = (JButton)e.getSource(); //Get the button that generated the event
				if(button.getKeyListeners().length == 0 && b==1)//If we are not already waiting for a key and we clicked the left button of the mouse
				{
					button.addKeyListener(keyListener); //Wait for a key to associate to  the gesture
				}
				else if(button.getKeyListeners().length == 1) //If we want to associate a mouse click instead of a key 
				{
					keys[Integer.parseInt(button.getName())] = b * -1; //Stores the negative value of the mouse button to distinguish it from key codes
					button.setText("Mouse " + b);
					button.removeKeyListener(keyListener); 
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e){}

			@Override
			public void mouseReleased(MouseEvent e) {}
		};
		
		ActionListener actionListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event) //When we edit a  checkbox
			{
				JCheckBox checkBox = (JCheckBox)event.getSource(); //Gets the checkbox that generated the event
				status[Integer.parseInt(checkBox.getName())] = checkBox.isSelected(); 	
			}
		};
		
		for(int i = 0;i < gestNum;++i)
		{
			try
			{
				JSONObject jO;
				jO = jA.getJSONObject(i);
				keys[i] = jO.getInt("key");
				status[i] = jO.getBoolean("active");
			}
			catch (Exception e) //jA is null, which probably means that data.json does't exists 
			{
				keys[i] = 0;
				status[i] = false; //Disable all the Gestures
			}

			JLabel label = new JLabel(labels[i]);
			label.setBounds(12, 50+35*i, 120, 16);
						
			JButton button = new JButton(keys[i] > -1?KeyEvent.getKeyText(keys[i]):"Mouse "+ keys[i] * -1); //Sets button's label
			button.setBounds(150, 45+35*i, 120, 25);
			button.setName(""+i); //We give a name to each button to know which one generated a event 
			button.addMouseListener(mouseListener);
			
			JCheckBox checkBox = new JCheckBox("");
			checkBox.setBounds(280, 45+35*i, 25, 25);
			checkBox.setName(""+i);
			checkBox.setSelected(status[i]);
			checkBox.addActionListener(actionListener);
			frame.getContentPane().add(label);
			frame.getContentPane().add(button);
			frame.getContentPane().add(checkBox);
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) 
	{
		try 
		{
			byte b  = serialPort.readBytes(1)[0]; //Reads first byte  available on serial port 
			if(b>0 && b<16 && keys[b-1] !=0) //if we have a valid gesture, there is a valid key o mouse button associated
			{
				if(status[b-1]) //if the gesture is enabled
				{
					if(keys[b-1] > -1) //if we associated a key
					{
						r.keyPress(keys[b-1]);
						r.keyRelease(keys[b-1]);
					}
					else //We associated a mouse button
					{
						int mask = InputEvent.getMaskForButton(keys[b-1] * -1);
						r.mousePress(mask);
						r.mouseRelease(mask);
					}
				}
			}
	    }
	    catch (Exception e){e.printStackTrace();}
	}
}