package com.blueline.flowprocess.core.monitor;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import com.blueline.commons.ReflectUtils;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.log.LogUtils;
public class MonitorUtils {
	public final static String TYPE_PROCESS_NODE_START = "PROCESS_NODE_START";
	public final static String TYPE_PROCESS_NODE_STOP = "PROCESS_NODE_STOP";
	static ICollect collect = null;
	public static boolean init(Map<String, Object> config) {
		try {
			Map<String, Object> monitor_config = (Map<String, Object>) config.get(ConfigUtils.PARAM_MONITOR);
			if(null==monitor_config){
				LogUtils.warn("Monitor config is null.");
				return true;
			}
			String monitor = (String) monitor_config.get(ConfigUtils.PARAM_CLASS);
			if (monitor != null && !monitor.isEmpty()) {
				collect = ReflectUtils.newObject(monitor);
				collect.init((Map<String, Object>) config.get("Collect"));
				return true;
			} else {
				return true;
			}
		} catch (Exception e) {
			LogUtils.warn(e);
		}
		return false;
	}
	public static boolean start() {
		if (collect != null) {
			return collect.start();
		} else {
			return false;
		}
	}
	public static boolean stop() {
		if (collect != null) {
			return collect.stop();
		} else {
			return false;
		}
	}
	public static boolean collect(String type, Map<String, Object> data) {
		if (collect != null) {
			try {
				collect.collect(type, data);
				return true;
			} catch (Exception e) {
				LogUtils.warn(e);
			}
		}
		return false;
	}
	public static List<String> getAllHostAddress() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			List<String> addresses = new ArrayList<String>();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
							&& inetAddress.getHostAddress().indexOf(":") == -1) {
						addresses.add(inetAddress.getHostAddress());
					}
				}
			}
			return addresses;
		} catch (SocketException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
