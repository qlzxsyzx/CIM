package com.qlzxsyzx.user.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class IPUtil {

    public static String getRealIP(HttpServletRequest request){
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-Real-IP");
            if (check(ipAddress)){
                ipAddress = request.getHeader("X-forwarded-for");
            }
            if (check(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (check(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (check(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (check(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    try {
                        ipAddress = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        log.error("getRealIP error", e);
                    }
                }
            }
            // 通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null) {
                if (ipAddress.contains(",")) {
                    return ipAddress.split(",")[0];
                } else {
                    return ipAddress;
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            log.error("getRealIP error", e);
            return "";
        }
    }

    public static String getRealAddress(HttpServletRequest request){
        String ip = getRealIP(request);
        if (ip == null || ip.isEmpty() || "0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)){
            return null;
        }
        String address = null;
        try {
            address = getRealAddress(ip);
        } catch (Exception e) {
            log.error("getRealAddress error", e);
        }
        return address;
    }

    public static String getRealAddress(String ip) throws Exception{
        if (ip == null || ip.isEmpty())
            return "";
        String forObject = new RestTemplate().getForObject("https://qifu-api.baidubce.com/ip/geo/v1/district?ip=" + ip, String.class);
        JSONObject json = JSON.parseObject(forObject);
        if (json.get("code").equals("Success")) {
            StringBuilder builder = new StringBuilder();
            JSONObject data = JSON.parseObject(json.get("data").toString());
            Object district = data.get("district");
            builder.append(data.get("continent"))
                    .append("-").
                    append(data.get("country"))
                    .append("-").
                    append(data.get("prov"))
                    .append("-").
                    append(data.get("city"))
                    .append(district == null ? "" : "-"+district);
            return builder.toString();
        }
        return "2";
    }

    private static boolean check(String ip){
        return ip == null || ip.isEmpty() || "unknown".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip);
    }

}
