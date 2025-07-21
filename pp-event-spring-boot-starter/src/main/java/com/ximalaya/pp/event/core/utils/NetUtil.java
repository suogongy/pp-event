package org.ppj.pp.event.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetUtil {
  private static final Logger LOG = LoggerFactory.getLogger(NetUtil.class);
  public static final String LOCALHOST = "127.0.0.1";
  public static final String ANYHOST = "0.0.0.0";
  private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
  private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("192.168(\\.\\d{1,3}){2}$");
  private static final Pattern LOCAL_IP_PATTERN172_1 = Pattern.compile("172.1[6-9](\\.\\d{1,3}){2}$");
  private static final Pattern LOCAL_IP_PATTERN172_2 = Pattern.compile("172.2[0-9](\\.\\d{1,3}){2}$");
  private static final Pattern LOCAL_IP_PATTERN172_3 = Pattern.compile("172.3[0-1](\\.\\d{1,3}){2}$");
  private static final Pattern LOCAL_IP_PATTERN10 = Pattern.compile("10(\\.\\d{1,3}){3}$");

  public static boolean isValidAddress(InetAddress address) {
    if (address == null || address.isLoopbackAddress()) {
      return false;
    }
    String name = address.getHostAddress();
    return name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches() &&
           (LOCAL_IP_PATTERN.matcher(name).matches() || LOCAL_IP_PATTERN172_1.matcher(name).matches() ||
            LOCAL_IP_PATTERN172_2.matcher(name).matches() || LOCAL_IP_PATTERN172_3.matcher(name).matches() ||
            LOCAL_IP_PATTERN10.matcher(name).matches());
  }

  private static volatile InetAddress LOCAL_ADDRESS = null;

  private static InetAddress getLocalAddress0() {
    InetAddress localAddress = null;
    try {
      localAddress = InetAddress.getLocalHost();
      if (isValidAddress(localAddress)) {
        return localAddress;
      }
    } catch (Throwable e) {
      LOG.warn("Failed to retriving ip address, " + e.getMessage(), e);
    }
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      if (interfaces != null) {
        while (interfaces.hasMoreElements()) {
          try {
            NetworkInterface network = interfaces.nextElement();
            Enumeration<InetAddress> addresses = network.getInetAddresses();
            if (addresses != null) {
              while (addresses.hasMoreElements()) {
                try {
                  InetAddress address = addresses.nextElement();
                  if (isValidAddress(address)) {
                    return address;
                  }
                } catch (Throwable e) {
                  LOG.warn("Failed to retriving ip address, " + e.getMessage(), e);
                }
              }
            }
          } catch (Throwable e) {
            LOG.warn("Failed to retriving ip address, " + e.getMessage(), e);
          }
        }
      }
    } catch (Throwable e) {
      LOG.warn("Failed to retriving ip address, " + e.getMessage(), e);
    }

    LOG.error("Could not get local host ip address, will use 127.0.0.1 instead.");
    return localAddress;
  }

  /**
   * 根据 ip 获取 host
   *
   * @param address ip in str
   * @return hostName
   */
  public static String getHostName(String address) {
    try {
      int index = address.indexOf(':');
      if (index > -1) {
        address = address.substring(0, index);
      }
      InetAddress inetAddress = InetAddress.getByName(address);
      if (inetAddress != null) {
        String hostname = inetAddress.getHostName();
        return hostname;
      }
    } catch (Throwable e) {
      // ignore
    }
    return address;
  }

  /**
   * 获取 localHost
   * <p>
   * 如果获取不到 host 抛出异常
   *
   * @return localHost
   */
  public static String getLocalHost() {
    String host = null;
    try {
      InetAddress inetAddress = InetAddress.getLocalHost();
      host = inetAddress.getHostName();

    } catch (UnknownHostException e) {
      // pass
    }
    if (host == null) {
      InetAddress address = getLocalAddress();
      try {
        host = address.getHostName();
      } catch (RuntimeException e) {
        LOG.error("get hostName error!, address = {}", address, e);
        throw e;
      }
    }
    return host;
  }

  /**
   * 获取 local IP
   *
   * @return localIP
   */
  public static String getLocalIp() {
    String host = null;
    try {
      InetAddress inetAddress = InetAddress.getLocalHost();
      host = inetAddress.getHostAddress();
    } catch (Throwable e) {
      // pass
    }
    if (host == null || LOCALHOST.equals(host) || host.startsWith("127.0")) {
      InetAddress address = getLocalAddress();
      host = address == null ? LOCALHOST : address.getHostAddress();
    }
    return host;
  }

  /**
   * 遍历本地网卡，返回第一个合理的IP。
   *
   * @return 本地网卡IP
   */
  public static InetAddress getLocalAddress() {
    if (LOCAL_ADDRESS != null) {
      return LOCAL_ADDRESS;
    }
    InetAddress localAddress = getLocalAddress0();
    LOCAL_ADDRESS = localAddress;
    return localAddress;
  }

  /**
   * Parse the socket address, omit the leading "/" if present.
   *
   * e.g.1 /127.0.0.1:1234 -> 127.0.0.1:1234
   * e.g.2 sofatest-2.stack.alipay.net/10.209.155.54:12200 -> 10.209.155.54:12200
   *
   * @param socketAddress
   * @return String
   */
  public static String parseSocketAddressToString(SocketAddress socketAddress) {
    if (socketAddress != null) {
      return doParse(socketAddress.toString().trim());
    }
    return StringUtils.EMPTY;
  }

  /**
   * Parse the host ip of socket address.
   *
   * e.g. /127.0.0.1:1234 -> 127.0.0.1
   *
   * @param socketAddress
   * @return String
   */
  public static String parseSocketAddressToHostIp(SocketAddress socketAddress) {
    final InetSocketAddress addrs = (InetSocketAddress) socketAddress;
    if (addrs != null) {
      InetAddress addr = addrs.getAddress();
      if (null != addr) {
        return addr.getHostAddress();
      }
    }
    return StringUtils.EMPTY;
  }

  /**
   * <ol>
   * <li>if an address starts with a '/', skip it.
   * <li>if an address contains a '/', substring it.
   * </ol>
   *
   * @param addr
   * @return
   */
  private static String doParse(String addr) {
    if (StringUtils.isBlank(addr)) {
      return StringUtils.EMPTY;
    }
    if (addr.charAt(0) == '/') {
      return addr.substring(1);
    } else {
      int len = addr.length();
      for (int i = 1; i < len; ++i) {
        if (addr.charAt(i) == '/') {
          return addr.substring(i + 1);
        }
      }
      return addr;
    }
  }
}
