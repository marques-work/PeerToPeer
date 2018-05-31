package com.shangyun.p2ptester.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.p2p.pppp_api.PPCS_APIs;
import com.p2p.pppp_api.st_PPCS_Session;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

public class ReadWriteTesterTask extends BaseTask{
	
	private Context mContext;
	private String mdid;
	private byte mMode = 1;

	private int mHandleSession = -1;
	public static final int MAX_SIZE_BUF	=65536; //64*1024;
	
	// define for Read/Write test mode
	private int TEST_WRITE_SIZE = 1004;  // (251 * 4), 251 is a prime number
	private int TOTAL_WRITE_SIZE = (4*1024*TEST_WRITE_SIZE);
	private int TEST_NUMBER_OF_CHANNEL = 8;
	private List<RWInfo> rwInfos = new ArrayList<ReadWriteTesterTask.RWInfo>();
	
	public ReadWriteTesterTask(Context context,Handler handler, String did, byte mode) {
		super(handler, did);
		mContext = context;
		mdid = did;
		mMode = mode;
	}
	
	public void stopConnect(){
		isStop = true;
		PPCS_APIs.PPCS_Connect_Break();	
	}

	//读写测试
	public void readWriteTest(){

		networkDetect();
		
		
		mHandleSession = PPCS_APIs.PPCS_Connect(mdid, mMode, UDP_Port);
		System.out.println("m_handleSession:" + mHandleSession);

		if (mHandleSession >= 0) {
			
			st_PPCS_Session SInfo = new st_PPCS_Session();
			
			if (PPCS_APIs.PPCS_Check(mHandleSession, SInfo) == PPCS_APIs.ERROR_PPCS_SUCCESSFUL){
				String str = String.format("Remote Address=%s:%d", SInfo.getRemoteIP(), SInfo.getRemotePort());
				System.out.println(str);

				String str1 = String.format("Mode= %s", (SInfo.getMode() == 0) ? "P2P" : "RLY");
				updateStatus(str + "," + str1 + "\n");
				
				String str0 = "Connect Success!! gSessionID=" + mHandleSession;
				updateStatus(str0 + "\n");
				
				doJob();
			}
			
		}else {
			if (mHandleSession == PPCS_APIs.ERROR_PPCS_USER_CONNECT_BREAK) {
				updateStatus("Connect break is called !\n");
			}else {
				String err = getErrorMessage(mHandleSession);
				String text = String.format("Connect failed(%d) : %s \n", mHandleSession,err);
				updateStatus(text);
			}
		}

		PPCS_APIs.PPCS_Close(mHandleSession);
	}
	
	private void doJob() {
		
		byte[] packet = new byte[1];
		packet[0] = mMode;
		int ret = PPCS_APIs.PPCS_Write(mHandleSession, CH_CMD, packet, packet.length);
		System.out.println("PPCS_Write ret:"+ret);
		if (ret < 0) {
			updateStatus(getErrorMessage(ret) + "\n");
		}else {
			switch (mMode)
			{
				case 0: 
					ft_Test(); // File transfer test
					break;	
				case 1: 
					RW_Test();// Bidirectional read write test
					break;	
				case 2: 
					pkt_Test(); // PktRecv/PktSend test
					break;	
				default:
					break;
			}
		}
		
	}

	private void ft_Test(){
		File cachePath = mContext.getExternalCacheDir();
		File file = new File(cachePath, "minion.mp4");
		if (file.exists()) {
			file.delete();
		}
		
		FileOutputStream fos = null;
		
		try {
			file.createNewFile();
			fos = new FileOutputStream(file,true);
		
			int sizeCounter = 0; 
			
			long start_time = getTimes();
			
			while(true){
				
				byte[] pReadBuffer = new byte[1024];
				int[] ReadSize = new int[1];
				ReadSize[0] = 1024;
				int ret = PPCS_APIs.PPCS_Read(mHandleSession, CH_DATA, pReadBuffer, ReadSize, 0xFFFFFFFF);
				System.out.println("PPCS_Read ret:"+ret + ",buffersize:"+pReadBuffer.length);
				if (0 > ret){
					System.out.println("PPCS_Read: Channel=" + CH_CMD + ",ret=" +  ret);
					if (PPCS_APIs.ERROR_PPCS_SESSION_CLOSED_TIMEOUT == ret){
						updateStatus("\nSession Closed TimeOUT!!\n");
						break;
					}else if (PPCS_APIs.ERROR_PPCS_SESSION_CLOSED_REMOTE == ret){
						updateStatus("\nSession Remote Close!!");
						
						long end_time = getTimes();
						double time = (end_time-start_time)*1.0 / 1000;
						int speed = (int)(sizeCounter / 1024 / time);
						
						String timeValue = String.format("%.2f Sec", time);
						
						updateStatus("\nFile Transfer Done!! Read Size = " + sizeCounter + " byte,Time = " + timeValue + ",Speed = " + speed  +"kByte/sec\n");
						break;

					}
				}
				
				int size = ReadSize[0];	
				if (fos != null) {
					fos.write(pReadBuffer, 0, size);
				}
				
				sizeCounter += size;
	
				System.out.println("ft_Test size:"+size + ",sizeCounter:"+sizeCounter);
				
				if (sizeCounter % (1024*1024) == 0) 
				{
					updateStatus("* ");
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private void pkt_Test(){
		int counter = 0;
		int expectValue = 0;
		updateStatus("PPCS_PktRecv ...\n");
		while(true){
			byte[] pktBuf = new byte[1024];
			
			int[] pktSize = new int[1];
			pktSize[0] = 1024;
				
			int ret = PPCS_APIs.PPCS_PktRecv(mHandleSession, CH_DATA, pktBuf, pktSize, 0xFFFFFFF);			
			System.out.println("--counter:" + counter + ",PPCS_PktRecv ret:"+ret + ",data:"+pktBuf[0]);
			
			if (ret < 0) {
				if (PPCS_APIs.ERROR_PPCS_SESSION_CLOSED_TIMEOUT == ret)
				{
					updateStatus("Session Closed TimeOUT!!\n");
					break;
				}
				else if (PPCS_APIs.ERROR_PPCS_SESSION_CLOSED_REMOTE == ret)
				{
					updateStatus("Session Remote Close!!\n");
					break;
				}
			}else {
				
				int recvSize = pktSize[0];
				if (recvSize != 1024) {
					String text = String.format("Packet size error!! PktSize=%d, should be 1024\n", recvSize);
					updateStatus(text);
				}else {
					System.out.println("data:"+ pktBuf[0] + ",size:"+recvSize);
				}
				
				if (expectValue != pktBuf[0]) {
					updateStatus("Packet Lost Detect!! Value = " +  pktBuf[0] +  "(should be " +  expectValue + ")\n"); 
					expectValue = (pktBuf[0] + 1) % 100;
				}else 
					expectValue = (expectValue + 1) % 100;
				
				if (counter % 100 == 99){
					String log = String.format("----->Recv %d packets. (1 packets=%d byte)\n", counter+1, pktBuf.length);
					updateStatus(log);
				}
	
				counter++;
				
			}
			
		}
		
		
	}
	
	private void RW_Test() {
		// meanwhile create 16 threads to write and read
		System.out.println("RW_Test");
		
		try {
			
			List<Thread> writeThreads = new ArrayList<Thread>();
			List<Thread> readThreads = new ArrayList<Thread>();
			
			for (int i = 0; i < TEST_NUMBER_OF_CHANNEL; i++) {
				
				rwInfos.add(new RWInfo());
				
				Thread w = new Thread(new ThreadWrite(i));
				w.start();
				writeThreads.add(w);
				
				Thread r = new Thread(new ThreadRead(i));
				r.start();
				readThreads.add(r);	
	
				Thread.sleep(20);
			}
			
			for (int i = 0; i < TEST_NUMBER_OF_CHANNEL; i++) {
				Thread w = writeThreads.get(i);
				w.join();
				
				Thread r = readThreads.get(i);
				r.join();
				
			}
			
			for (int i = 0; i < TEST_NUMBER_OF_CHANNEL; i++) {
				
				RWInfo info = rwInfos.get(i);
				if (info != null) {
					String readLog = String.format("\nThreadRead  Channel %d Exit - TotalSize: %d byte , Time:%d sec, Speed:%d KByte/sec\n", i, info.totalRead,info.readTimes/1000,info.totalRead/info.readTimes);
					updateStatus(readLog);
					
					String writeLog = String.format("ThreadWrite Channel %d Exit - TotalSize: %d byte, Time:%d sec, Speed:%d KByte/sec\n", i, info.totalWrite,info.writeTimes/1000,info.totalWrite/info.writeTimes);
					updateStatus(writeLog);	
				}
			}
	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}


	private class ThreadWrite implements Runnable{

		private int channel;
		
		public ThreadWrite(int channel) {
			// TODO Auto-generated constructor stub
			this.channel = channel;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] buffer = new byte[TEST_WRITE_SIZE];
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = (byte)(i % 251);
			}
			updateStatus(String.format("ThreadWrite Channel %d running...\n", channel));
			
			long startTime = getTimes();
			int totalSize = 0;

			int[] wsize = new int[1]; 
	    	wsize[0] = 1024;
	    	while (PPCS_APIs.PPCS_Check_Buffer(mHandleSession, (byte)channel, wsize, null) == PPCS_APIs.ERROR_PPCS_SUCCESSFUL) {
	    		int writeSize = wsize[0];
	    		
	    		if (writeSize < 256*1024 && totalSize < TOTAL_WRITE_SIZE) {
					
	    			int n1 = (int) (Math.random()* (TEST_WRITE_SIZE/3) % (TEST_WRITE_SIZE/3));
	    			int n2 = (int) (Math.random()* (TEST_WRITE_SIZE/3) % (TEST_WRITE_SIZE/3));
	    			int n3 = (int) (Math.random()* (TEST_WRITE_SIZE/3) % (TEST_WRITE_SIZE/3));
	    			int n4 = TEST_WRITE_SIZE - n1 - n2 - n3;
	    			
	    			byte[] buff1 = new byte[n1];
	    			System.arraycopy(buffer, 0, buff1, 0, n1);
					PPCS_APIs.PPCS_Write(mHandleSession, (byte)channel, buff1, n1);
					
					byte[] buff2 = new byte[n2];
	    			System.arraycopy(buffer, n1, buff2, 0, n2);
					PPCS_APIs.PPCS_Write(mHandleSession, (byte)channel, buff2, n2);
					
					byte[] buff3 = new byte[n3];
	    			System.arraycopy(buffer, n1+n2, buff3, 0, n3);
					PPCS_APIs.PPCS_Write(mHandleSession, (byte)channel, buff3, n3);
					
					byte[] buff4 = new byte[n4];
	    			System.arraycopy(buffer,n1+n2+n3, buff4, 0, n4);
					PPCS_APIs.PPCS_Write(mHandleSession, (byte)channel, buff4, n4);
				
					totalSize += TEST_WRITE_SIZE;
				}
	    		//When writesize equals 0, all the data in this channel is sent out
	    		else if (0 == writeSize) 
	    			break;
	    		else{
	    			try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}

			}
	    	
			long endTime = getTimes();
			
			RWInfo info = rwInfos.get(channel);
			info.writeTimes = endTime - startTime;
			info.totalWrite = totalSize;	
		}

	}
	
	private class ThreadRead implements Runnable{

		private int channel;
		
		public ThreadRead(int channel) {
			// TODO Auto-generated constructor stub
			this.channel = channel;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int TotalSize = 0;
			int timeout_ms = 200;
			long startTime = getTimes();
			updateStatus(String.format("ThreadRead Channel %d running...\n", channel));
			while (true) {
				
				byte[] pReadBuffer = new byte[1];
				int[] ReadSize = new int[1];
				ReadSize[0] = 1;
				int ret = PPCS_APIs.PPCS_Read(mHandleSession, (byte)channel, pReadBuffer, ReadSize, timeout_ms);
				if (ret<0 && ret != PPCS_APIs.ERROR_PPCS_TIME_OUT) {
					if (TotalSize == TOTAL_WRITE_SIZE) break;	
					String log = String.format("\n PPCS_Read ret=%s, CH=%s, ReadSize=%s byte, TotalSize=%s byte\n", ret, channel, ReadSize[0], TotalSize);
					updateStatus(log);
					break;
				}
				
				int readSize = ReadSize[0];
				int zz = pReadBuffer[0];
				if (zz < 0) {//if the write is negative, add 256 to the positive number
					zz = pReadBuffer[0] & 0xff;  
				}
				
				if (readSize > 0 && TotalSize % 251 != zz) {
					String log = String.format("\n PPCS_Read ret=%s, Channel:%s Error!! ReadSize=%s, TotalSize=%s, zz=%s\n", ret, channel, readSize, TotalSize, zz);
					updateStatus(log);
					break;
				}else if (TotalSize%(1*1024*1024) == 1*1024*1024-1) {
					updateStatus(channel+" ");
				}
				
				TotalSize += readSize;
				if (TotalSize == TOTAL_WRITE_SIZE) 
					break;
			}
			
			long endTime = getTimes();
			RWInfo info = rwInfos.get(channel);
			info.readTimes = endTime - startTime;
			info.totalRead = TotalSize;	
		}
		
	}
	
	
	private class RWInfo{
		
		public int totalRead;
		
		public int totalWrite;
		
		public long readTimes;
		
		public long writeTimes;
		
	}

}
