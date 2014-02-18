package com.prosoft.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.prosoft.form.TextArea;

public class JstlServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		TextArea textArea = new TextArea(request.getParameter("userName"));
        request.setAttribute("textArea", textArea);
        RequestDispatcher view = request.getRequestDispatcher("index.jsp");
        view.forward(request, response);
	}

}
