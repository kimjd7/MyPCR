package loopTest;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;
import com.hidapi.CallbackDeviceChange;
import com.hidapi.DeviceChange;
import com.hidapi.DeviceConstant;
import com.hidapi.HidClassLoader;

import javax.swing.JLabel;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;

import java.awt.Component;

import javax.swing.SwingConstants;
import javax.swing.JPanel;

import java.awt.FlowLayout;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.GridLayout;

import javax.swing.JInternalFrame;

import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JButton;
import javax.swing.JEditorPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;


public class mainUI extends JDialog implements DeviceChange {

	private static final long serialVersionUID = 1L;

	private HIDManager m_Manager = null;
	private HIDDevice m_Device = null;
	private CallbackDeviceChange m_Callback_DeviceChange = null;
	private boolean IsConnected = false;	
	
	private String serialNumber = null;
	private JLabel conLed = new JLabel("");

	private JTextPane textPane = new JTextPane();


	static
	{
		// HID 관련 Native 라이브러리를 운영체제별로 부를 수 있는 함수이다. 프로그램이 동작시 1번만 실행된다.
		if( !HidClassLoader.LoadLibrary() )
		{
			// 만약 로드에 실패하면 오류 메시지창을 띄우고, 프로그램을 종료시킨다.
			JOptionPane.showMessageDialog(null, "Not Supported OS.. Exit the Program.");
			System.exit(-1);
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainUI dialog = new mainUI();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public mainUI() {
		setBounds(100, 100, 498, 340);
		getContentPane().setLayout(null);
		
		conLed.setAlignmentX(Component.CENTER_ALIGNMENT);
		conLed.setIcon(new ImageIcon(mainUI.class.getResource("/img/ledGLow.png")));
		conLed.setBounds(455, 10, 20, 25);
		getContentPane().add(conLed);
		
		JButton btnSend = new JButton("send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str="";
				for ( int i=0; i < 64; i++ ) {
					int val;
					if ( i%2 == 0 ) val=2*i;
					else val = i;
					str += String.format("%d = %d\n", i, val);
				}
				textPane.setText(str);
			}
		});
		btnSend.setBounds(12, 10, 69, 23);
		getContentPane().add(btnSend);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 39, 130, 253);
		getContentPane().add(scrollPane);
		
		scrollPane.setViewportView(textPane);
	
		init();
	}
	
	private void init() {
		// Device 연결 체크용 콜백 함수 설정
		
		try
		{
			// DeviceManager 인스턴스 생성
			m_Manager = HIDManager.getInstance();
			// Device 연결 상태를 표시해주는 콜백함수 등록
			m_Callback_DeviceChange = CallbackDeviceChange.getInstance(m_Manager, this);
			m_Callback_DeviceChange.setDaemon(true);
			m_Callback_DeviceChange.start();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		
		
	}

	public void setSerialNumber(String serialNumber){
		this.serialNumber = serialNumber;
		m_Callback_DeviceChange.setSerialNumber(serialNumber);
	}

	public HIDDevice getDevice()
	{
		return m_Device;
	}
	
	private void connectToDevice(){
		try
		{
			if( m_Device != null )
			{
				m_Device.close();
				m_Device = null;
			}
			
			m_Device = m_Manager.openById(DeviceConstant.VENDOR_ID, DeviceConstant.PRODUCT_ID, serialNumber);
			if( m_Device != null )
			{
				IsConnected = true;
				m_Device.disableBlocking();
				
				setTitle(m_Device.getSerialNumberString());
				setSerialNumber(m_Device.getSerialNumberString());
				
			}
			else
			{
				// 연결 에러 처리
				System.out.println("Fatal Error!");
			}
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	
	@Override
	public void OnMessage(int MessageType, Object data)
	{
		int i=0;
		i++;
		switch( MessageType )
		{
			case CONNECTED:
				String count = (String)data;
//				if( count.equals("1") ) {
					connectToDevice();
					conLed.setIcon(new ImageIcon(mainUI.class.getResource("/img/ledGHigh.png")));
//				}
				break;
			case DISCONNECTED:
				IsConnected = false;
				conLed.setIcon(new ImageIcon(mainUI.class.getResource("/img/ledGLow.png")));
				break;
		}
	}	
}
