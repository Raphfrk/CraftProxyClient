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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import com.raphfrk.craftproxyclient.io.PropertiesFile;

public class CraftProxyGUI extends JFrame implements WindowListener, ActionListener {
	private static final long serialVersionUID = 1L;

	JPanel topPanel = new JPanel();
	JPanel secondPanel = new JPanel();
	JPanel combinedTop = new JPanel();
	JTextField serverName;
	JTextField portNum;
	JPanel filePanel;
	JTextField currentSize;
	JTextField desiredSize;
	JLabel localServerName;
	JTextField localServerPortnum;
	JLabel info;
	JButton connect;

	final Object statusTextSync = new Object();
	String statusText = "";

	final Object buttonTextSync = new Object();
	String buttonText = "Start";

	Thread serverMainThread = null;

	public JFrame main;

	PropertiesFile pf;

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

	public void safeSetStatus(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				info.setText(text);
			}
		});
	}

	public void safeSetButton(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				connect.setText(text);
				connect.updateUI();
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
		if(serverMainThread != null) {
			serverMainThread.interrupt();
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

			int desired = 48;
			try {			
				pf.setString("connect_hostname", serverName.getText());
				pf.setInt("connect_port", Integer.parseInt(portNum.getText()));
				pf.setInt("listen_port", Integer.parseInt(localServerPortnum.getText()));
				try {
					desired = Integer.parseInt(desiredSize.getText());
				} catch (NumberFormatException nfe) {
					desired = 48;
					desiredSize.setText("48");
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
			} catch (NumberFormatException nfe) {
			}

			if(serverMainThread == null || !serverMainThread.isAlive()) {

				safeSetButton("Stop");

			} else {
				safeSetButton("Stopping");
				serverMainThread.interrupt();
				desiredSize.setEditable(true);
				serverName.setEditable(true);
				portNum.setEditable(true);
				localServerPortnum.setEditable(true);			}
		}
	}


}
