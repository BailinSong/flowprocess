package com.blueline.flowprocess.components.command.channel;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.blueline.flowprocess.core.command.CedfCommand;
import com.blueline.flowprocess.core.command.CommandUtils;
import com.blueline.flowprocess.core.command.ICommandChannel;
import com.blueline.flowprocess.core.log.LogUtils;
public class UdpCommandChannel implements ICommandChannel {
	public static final String PARAM_CHANNEL_ADDR = "ChannelAddr";
	public static final String PARAM_CHANNEL_PORT = "ChannelPort";
	int m_command_channel_port;
	InetAddress m_command_channel_addr;
	DatagramSocket m_datagram_rcv_socket;
	DatagramSocket m_datagram_snd_socket;
	Thread thread;
	@SuppressWarnings("unchecked")
	public void init(Map<String, Object> config) {
		try {
			m_command_channel_addr = InetAddress.getByName((String) config.get(PARAM_CHANNEL_ADDR));
			m_command_channel_port = Integer.parseInt((String) config.get(PARAM_CHANNEL_PORT));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void subscribe() {
		try {
			m_datagram_rcv_socket = new DatagramSocket(null);
			m_datagram_rcv_socket.setBroadcast(true);
			m_datagram_rcv_socket.setReuseAddress(true);
			m_datagram_rcv_socket.bind(new InetSocketAddress("0.0.0.0", m_command_channel_port));
			final DatagramSocket datagram_rcv_socket = m_datagram_rcv_socket;
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					byte[] buf = new byte[1024];
					while (true) {
					DatagramPacket dp = new DatagramPacket(buf, buf.length);
					try {
						datagram_rcv_socket.receive(dp);
						String message = new String(dp.getData(), 0, dp.getLength(),"UTF-8");
						LogUtils.debugFormat("Receive: channel[%s]:[%s], message[%s] ", dp.getAddress(), dp.getPort(),
								message);
						Map<String, Object> data = (Map<String, Object>) JSON.parse(message);
						CedfCommand command = new CedfCommand(data);
						CommandUtils.execCommand(command);
					} catch (IOException e) {
						e.printStackTrace();
					}
					}
				}
			});
			thread.setDaemon(true);
			thread.setName("UdpCommandChannel");
			thread.start();
		} catch (Exception e) {
			LogUtils.error(e);
			throw new RuntimeException(e);
		}
	}
	public void publish(CedfCommand command) {
		try {
			LogUtils.debugFormat("publish: channel[{}], message[{}]", m_command_channel_addr, command.toJsonString());
			m_datagram_snd_socket = new DatagramSocket();
			m_datagram_snd_socket.setBroadcast(true);
			m_datagram_snd_socket.setReuseAddress(true);
			byte[] bytes = command.toJsonString().getBytes("utf-8");
			DatagramPacket out = new DatagramPacket(bytes, bytes.length, m_command_channel_addr,
					m_command_channel_port);
			m_datagram_snd_socket.send(out);
			m_datagram_snd_socket.close();
			m_datagram_snd_socket=null;
		} catch (Exception e) {
			LogUtils.error(e);
			throw new RuntimeException(e);
		}
	}
}
