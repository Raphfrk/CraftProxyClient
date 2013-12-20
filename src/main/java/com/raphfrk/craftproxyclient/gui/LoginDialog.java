/*
 * This file is part of CraftProxyClient.
 *
 * Copyright (c) 2013-2014, Raphfrk <http://raphfrk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.raphfrk.craftproxyclient.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class LoginDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private final JLabel email;
	private final JTextField emailText;
	private final JLabel password;
	private final JPasswordField passwordText;
	private final JButton loginButton;

	public LoginDialog(JFrame parent) {
		super(parent);
		
		email = new JLabel("Email");
		emailText = new JTextField(30);
		password = new JLabel("Password");
		passwordText = new JPasswordField(30);
		
		JPanel emailPanel = new JPanel();
		emailPanel.setLayout(new BorderLayout());
		emailPanel.add(email, BorderLayout.CENTER);
		emailPanel.add(emailText, BorderLayout.LINE_END);
		
		JPanel passwordPanel = new JPanel();
		passwordPanel.setLayout(new BorderLayout());
		passwordPanel.add(password, BorderLayout.CENTER);
		passwordPanel.add(passwordText, BorderLayout.LINE_END);
		
		JPanel loginPanel = new JPanel();
		loginPanel.setLayout(new BorderLayout());
		loginPanel.setBorder(new TitledBorder("Login Details"));
		loginPanel.add(emailPanel, BorderLayout.PAGE_START);
		loginPanel.add(passwordPanel, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(loginPanel, BorderLayout.PAGE_START);
		
		loginButton = new JButton("Login");
		loginButton.addActionListener(LoginDialog.this);
		
		add(loginButton, BorderLayout.PAGE_END);

		setModal(true);
		
		pack();
		
		setResizable(false);
		
		
	}
	
	public String getEmail() {
		return emailText.getText();
	}
	
	public String getPassword() {
		return new String(passwordText.getPassword());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		setVisible(false);
	}
	
	

}
