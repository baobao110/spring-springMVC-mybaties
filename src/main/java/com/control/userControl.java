package com.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fenye.Fenye;
import com.service.CardService;
import com.service.Service;
import com.service.UserService;
import com.user.User;


@Controller
@RequestMapping("/user")
public class userControl extends control {
	
	@Autowired
	private Service service;
	
	@Autowired
	private UserService userservice;
	
	@Autowired
	private CardService cardservice;
	
	
	@RequestMapping("/load")
	public String load(HttpServletRequest req, HttpServletResponse resp) {
		// TODO Auto-generated method stub
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// Set factory constraints
		factory.setSizeThreshold(10240);
		
		String src = req.getServletContext().getRealPath("/");
		
		String path=null;
		
		if(src.endsWith(File.separator)) {
			path="WEB-INF";
		}
		else {
			path="/WEB-INF";
		}
		//�����ٴ���֤��·��������,����tomcatʱsrc�����Ǵ�/ ���������jetty���෴
		
		factory.setRepository(new File(src +path+"/load-tmp"));

		// Create a new file upload handler
		ServletFileUpload load = new ServletFileUpload(factory);

		// Set overall request size constraint
		load.setSizeMax(1024 * 500);
		// Parse the request
		try {
			List<FileItem> items = load.parseRequest(req);
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = iter.next();

			    if (item.isFormField()) {
			    	 String name = item.getFieldName();
			    	 String value = item.getString();
			    } else {
			    	 HttpSession session = req.getSession();
			    	 User user = (User)session.getAttribute("user");
			    	 String fileName = "" + user.getUsername();
			    	 File loadedFile = new File(src +path+"/load/" +  fileName);
			    	 item.write(loadedFile);
			    }
			}
			return toUsercenter(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	/*
	 * �ļ����ϴ�����ģ��Apache��fileupload
	 */
	
	@RequestMapping("/showPicture")
	public void showPicture(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		String src=req.getServletContext().getRealPath("/");
		System.out.println(">>>>>>>"+src);
		HttpSession session=req.getSession();//�����һ��֪ʶ�����session��request������һ����ȫ�ֱ���һ���Ǿֲ�����
		User user=(User)session.getAttribute("user");

		String path=null;
		
		if(src.endsWith(File.separator)) {
			path="WEB-INF";
		}
		else {
			path="/WEB-INF";
		}
		try (FileInputStream in = new FileInputStream(src+path+"/load/" + user.getUsername());
				OutputStream out = resp.getOutputStream()) {
			byte[] data = new byte[1024];
			int length = -1;
			while((length=in.read(data))!=-1) {
				out.write(data, 0, length);
				out.flush();
			}
		}
	}
	
	@RequestMapping("/toRegister")
	public String  toRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpSession session = req.getSession();
		return "register";
	
	}
	
	@RequestMapping("/Register")
	public String Register(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpSession session = req.getSession();//����н��и�ֵ����ԭ����Session�����û�д����µ�Session
		String username= req.getParameter("username");
		String psssword= req.getParameter("password");
		User user= userservice.register(username,psssword);
		if(user==null) {
			return "register";
		}
		else {
			session.setAttribute("user",user);
			return "login";
		}
	}
	
	@RequestMapping("/toLogin")
	public String toLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpSession session = req.getSession();
		return "login";																																								
	}
	
	@RequestMapping("/login")
	public String  login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpSession session = req.getSession();
		String username= req.getParameter("username");
		String psssword= req.getParameter("password");
		User user=userservice.login(username, psssword);
		if(null==user) {
			return "login";	
		}
		else {
			session.setAttribute("user", user);
			session.setAttribute("username",username);
			return toUsercenter(req, resp);
		}
	}
	
	@RequestMapping("/toUsercenter")
	public String toUsercenter(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("--------");
		javax.servlet.http.HttpSession session = req.getSession();
		String username= (String) session.getAttribute("username");
		System.out.println(username);
		String currentPage = req.getParameter("currentPage");
		currentPage=currentPage ==null ? "1" : currentPage;//ע������ķ�ҳ��̨ʵ��
		Fenye list=cardservice.list(username, Integer.parseInt(currentPage));
		System.out.println("\\\\\\"+list);
		req.setAttribute("fenye", list);
		req.setAttribute("currentPage", Integer.parseInt(currentPage));
		req.setAttribute("username", username);
		return "usercenter";
	}
	

}
