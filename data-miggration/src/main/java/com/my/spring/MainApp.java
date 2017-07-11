package com.my.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.my.spring.jdbc.dao.StudentDaoImpl;
import com.my.spring.jdbc.entities.Student;

public class MainApp {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("jdbc/Beans.xml");
		StudentDaoImpl studentDaoImpl = context.getBean("studentDaoImpl", StudentDaoImpl.class);
		Student s = studentDaoImpl.getById(1);
		s.setAge(45);
		studentDaoImpl.update(s);
		
	}

}
