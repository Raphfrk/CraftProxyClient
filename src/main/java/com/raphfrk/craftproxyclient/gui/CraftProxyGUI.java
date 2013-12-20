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
/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
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
 ******************************************************************************/
package com.raphfrk.craftproxyclient.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.json.simple.JSONObject;

import com.raphfrk.craftproxyclient.io.PropertiesFile;
import com.raphfrk.craftproxyclient.net.ConnectionListener;
import com.raphfrk.craftproxyclient.net.auth.AuthManager;

public class CraftProxyGUI extends JFrame implements WindowListener, ActionListener {
	
	private static final long serialVersionUID = 1L;

	private JPanel topPanel = new JPanel();
	private JPanel secondPanel = new JPanel();
	private JPanel combinedTop = new JPanel();
	private JTextField serverName;
	private JTextField portNum;
	private JPanel filePanel;
	private JTextField currentSize;
	private JTextField desiredSize;
	private JLabel localServerName;
	private JTextField localServerPortnum;
	private JLabel info;
	private JButton connect;

	private String buttonText = "Logging in";

	private final PropertiesFile pf;
	
	private ConnectionListener connectionListener;

	public CraftProxyGUI() {
		
		pf = new PropertiesFile("CraftProxyClientGUI.txt");

		try {
			pf.load();
		} catch (IOException e) {
		}
		
		String defaultHostname = pf.getString("connect_hostname", "localhost");
		int defaultPort = pf.getInt("connect_port", 20000);
		int listenPort = pf.getInt("listen_port", 25565);
		int desired = pf.getInt("cache_size", 48);

		setTitle("CraftProxyClient Local");
		setSize(450,325);
		setLocation(40,150);

		topPanel.setLayout(new BorderLayout());
		topPanel.setBorder(new TitledBorder("Remote Server"));
		topPanel.setBackground(Color.WHITE);
		secondPanel.setLayout(new BorderLayout());
		secondPanel.setBorder(new TitledBorder("Local Server"));
		secondPanel.setBackground(Color.WHITE);

		serverName = new JTextField(defaultHostname, 20);
		TitledBorder border = new TitledBorder("Name");
		serverName.setBorder(border);
		serverName.addActionListener(this);

		portNum = new JTextField(Integer.toString(defaultPort) , 6);
		border = new TitledBorder("Port");
		portNum.setBorder(border);
		portNum.addActionListener(this);

		localServerName = new JLabel("localhost");
		localServerName.setBackground(Color.GRAY);
		border = new TitledBorder("Name");
		localServerName.setBorder(border);

		localServerPortnum = new JTextField(Integer.toString(listenPort), 6);
		border = new TitledBorder("Port");
		localServerPortnum.setBorder(border);
		localServerPortnum.addActionListener(this);

		topPanel.add(serverName, BorderLayout.CENTER);
		topPanel.add(portNum, BorderLayout.LINE_END);

		secondPanel.setLayout(new BorderLayout());
		secondPanel.add(localServerName, BorderLayout.CENTER);
		secondPanel.add(localServerPortnum, BorderLayout.LINE_END);

		combinedTop.setLayout(new BorderLayout());
		combinedTop.add(topPanel, BorderLayout.CENTER);
		combinedTop.add(secondPanel, BorderLayout.SOUTH);
		
		currentSize = new JTextField("Unknown");
		currentSize.setBorder(new TitledBorder("Current Size (MB)"));
		currentSize.setEditable(false);
		
		desiredSize = new JTextField(Integer.toString(desired));
		desiredSize.setBorder(new TitledBorder("Max Size (MB)"));
		
		connect = new JButton(buttonText);
		connect.addActionListener(this);
		
		filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());
		JPanel fileLinePanel = new JPanel();
		fileLinePanel.setBorder(new TitledBorder("Cache Size"));
		fileLinePanel.setLayout(new GridLayout(1,3));
		fileLinePanel.add(currentSize);
		fileLinePanel.add(desiredSize);
		filePanel.add(fileLinePanel, BorderLayout.CENTER);
		filePanel.add(connect, BorderLayout.PAGE_END);
		
		info = new JLabel();
		border = new TitledBorder("Status");
		info.setBorder(border);

		setLayout(new BorderLayout());
		add(combinedTop, BorderLayout.PAGE_START);
		add(info, BorderLayout.CENTER);
		add(filePanel, BorderLayout.PAGE_END);

		this.setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.addWindowListener(this);
		
	}

	public void init() {
		this.setVisible(true);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				final JSONObject loginDetails = GUIManager.getLoginDetails();

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (loginDetails == null) {
							JOptionPane.showMessageDialog(CraftProxyGUI.this, "Login failed");
							dispose();
						} else {
							connect.setText("Start");
							info.setText("Logged in as " + AuthManager.getUsername());
							
						}
					}
				});
			}
		});
		t.start();
	}
	
	public void setDone() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (connect.getText().equals("Stop") || connect.getText().equals("Stopping")) {
					connect.setText("Start");
					desiredSize.setEditable(true);
					serverName.setEditable(true);
					portNum.setEditable(true);
					info.setText("Server halted");
					localServerPortnum.setEditable(true);
				} else {
					JOptionPane.showMessageDialog(CraftProxyGUI.this, "Error: server stopped notice received when it shouldn't have been running", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	public void setStatus(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				info.setText(text);
			}
		});
	}
	
	public void safeSetFileSize(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				currentSize.setText(text);
				currentSize.updateUI();
			}
		});
	}

	public void windowClosing(WindowEvent paramWindowEvent) {
		if (connectionListener != null) {
			connectionListener.interrupt();
		}
	}

	public void windowOpened(WindowEvent paramWindowEvent) {
	}

	public void windowClosed(WindowEvent paramWindowEvent) {
	}

	public void windowIconified(WindowEvent paramWindowEvent) {

	}

	public void windowDeiconified(WindowEvent paramWindowEvent) {
	}

	public void windowActivated(WindowEvent paramWindowEvent) {
	}

	public void windowDeactivated(WindowEvent paramWindowEvent) {
	}

	public void actionPerformed(ActionEvent action) {
		if(action.getSource().equals(connect)) {

			if (action.getActionCommand().equals("Start")) {
				pf.setString("connect_hostname", serverName.getText());
				int connectPort;
				try {
					connectPort = Integer.parseInt(portNum.getText());
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(CraftProxyGUI.this, "Unable to parse server port number", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				pf.setInt("connect_port", connectPort);
				int localPort;
				try {
					localPort = Integer.parseInt(localServerPortnum.getText());
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(CraftProxyGUI.this, "Unable to parse local port number", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				pf.setInt("listen_port", localPort);
				int desired;
				try {
					desired = Integer.parseInt(desiredSize.getText());
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(CraftProxyGUI.this, "Unable to desired cache size", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				pf.setInt("cache_size", desired);
				desiredSize.setEditable(false);
				serverName.setEditable(false);
				portNum.setEditable(false);
				localServerPortnum.setEditable(false);
				try {
					pf.save();
				} catch (IOException e) {
				}
				try {
					connectionListener = new ConnectionListener(this, localPort, serverName.getText(), connectPort);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(CraftProxyGUI.this, "Unable to start proxy server, " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				connectionListener.start();
				connect.setText("Stop");
				info.setText("Starting proxy server");
			} else if (action.getActionCommand().equals("Stop")) {
				connectionListener.interrupt();
				connect.setText("Stopping");
				info.setText("Halting proxy server");
			} else if (action.getActionCommand().equals("Stopping")) {
				JOptionPane.showMessageDialog(CraftProxyGUI.this, "Server halt is in progress", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}


}
