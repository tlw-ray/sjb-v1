//package com.xskr.onk_v1;
//
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//
//public class WebUtil {
//    public static String getCurrentUserName(){
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        return userDetails.getUsername();
//    }
//}
