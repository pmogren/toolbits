#!/usr/bin/env groovy
/*
 * Copyright 2009 Edward Mostrom
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.*
import java.util.Map

this.version = "1.0.1"
this.program = "Groovy Wave Check"
this.imageWidth = 128
this.imageHeight = 128
this.email = null
this.passwd = null
this.authCode = null
this.trayIcon = null
this.image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR)
this.unread = 0


if (!SystemTray.isSupported()) {
	println "System Tray not supported"
	System.exit(0)
}


this.exitMenu = {e -> System.exit(0)}

this.checkMenu = {e -> check()}

this.signInMenu = {e ->
	if(showLoginDialog())
	{
		auth()
		if(authCode == null)
		{
			trayIcon.toolTip = program + ": Auth Failure - Sign in again"
			setTrayColor(Color.red)
		}
		else
		{
			trayIcon.toolTip = program
			setTrayColor(Color.gray)
		}
	}
}

this.click = {e ->
	if (e.button == 1 && Desktop.isDesktopSupported()) {
		desktop = Desktop.getDesktop()
		if (desktop.isSupported(Desktop.Action.BROWSE)) {
			uri = new URI("https://wave.google.com/wave/?nouacheck&auth=" + authCode)
			desktop.browse(uri)
		}
	
	}
}

def synchronized showLoginDialog()
{
	def connectOptionNames = [ "Login", "Cancel" ]
	
	JPanel      connectionPanel = new JPanel(false)
	connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.X_AXIS))
	
	JLabel     userNameLabel = new JLabel("Email:   ", JLabel.RIGHT)
	JLabel     passwordLabel = new JLabel("Password:   ", JLabel.RIGHT)
	JPanel namePanel = new JPanel(false)
	namePanel.setLayout(new GridLayout(0, 1))
	namePanel.add(userNameLabel)
	namePanel.add(passwordLabel)
	
	JTextField userNameField = new JTextField("", 24)
	JTextField passwordField = new JPasswordField("", 24)
	JPanel fieldPanel = new JPanel(false)
	fieldPanel.setLayout(new GridLayout(0, 1))
	fieldPanel.add(userNameField)
	fieldPanel.add(passwordField)
	
	connectionPanel.add(namePanel)
	connectionPanel.add(fieldPanel)
	
	if(JOptionPane.showOptionDialog(null, connectionPanel, 
	"Wave Login", JOptionPane.OK_CANCEL_OPTION,	JOptionPane.INFORMATION_MESSAGE,
	null, connectOptionNames.toArray(), connectOptionNames[0]) != 0) 
	{
		return false
	}
	
	email = userNameField.getText();
	passwd = passwordField.getText();
	return true
}

def setTrayColor(trayColor)
{
	def t = new Color(0,0,0,0)
	image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR)
	def graphics = image.createGraphics()
	graphics.color = t
	graphics.fillRect(0,0,imageWidth,imageHeight)
	graphics.font = graphics.font.deriveFont(Font.BOLD, (float)imageWidth)
	graphics.color = trayColor?: Color.red
	graphics.drawString("~", 6, (int)(imageHeight/4)*2)
	graphics.color = trayColor?: Color.yellow
	graphics.drawString("~", 6, (int)(imageHeight/4)*3)
	graphics.color = trayColor?: Color.blue
	graphics.drawString("~", 6, (int)(imageHeight/4)*4)

	graphics.font = graphics.font.deriveFont(Font.BOLD, 42)
	graphics.color = Color.black
	if(unread > 0 && unread < 10)
		graphics.drawString(unread.toString(), imageWidth - 42, imageHeight)
	if(unread > 9)
		graphics.drawString("+", imageWidth - 42, imageHeight)
	
	graphics.dispose()
	trayIcon.image = image.getScaledInstance(32, 32, Image.SCALE_SMOOTH)
}

def setupTray()
{
	def tray = SystemTray.getSystemTray()
	def popup = new PopupMenu()
	popup.add(new MenuItem(label:"Sign In", actionPerformed:signInMenu))
	popup.add(new MenuItem(label:"Check Waves", actionPerformed:checkMenu))
	popup.add(new MenuItem(label:"Exit", actionPerformed:exitMenu))
	trayIcon = new TrayIcon(image:image.getScaledInstance(32, 32, Image.SCALE_SMOOTH), tooltip:program, popup:popup, imageAutoSize:true, mouseClicked:click)
	setTrayColor(Color.gray)
	tray.add(trayIcon)
}

def post(Map<String,String> parms, location)
{
	StringBuffer data = new StringBuffer()
	parms.eachWithIndex {it, i->  if(i > 0) data << "&"; data << it.key + "=" + it.value}
	
	URL url = new URL(location)
	URLConnection conn = url.openConnection()
	
	conn.setRequestMethod("POST")
	conn.setRequestProperty 'content-type', 'application/x-www-form-urlencoded'
	conn.doOutput = true
	
	try{
		Writer wr = new OutputStreamWriter(conn.outputStream)
	
		wr.write(data.toString())
		wr.flush()
		wr.close()
	
		conn.connect()
	} catch(Exception e)
	{
		return [responseCode : 0, text : "ERROR"]
	}
	
	if(conn.responseCode == 200)
	{
		return [responseCode : conn.responseCode, text : conn.content.text]
	}
	else
	{
		StringBuffer out = new StringBuffer()
		conn.errorStream.eachLine{ out << "$it\n"}
		return [responseCode : conn.responseCode, text : out.toString()]
	}
}

def auth()
{
	authCode = null
	def result = post("https://www.google.com/accounts/ClientLogin",
			accountType : "GOOGLE", Email : email, Passwd : passwd, service : "wave", source : "groovyWaveCheck")
	
	if(result.responseCode == 200)
	{
		result.text.eachLine{String line ->
			if(line.startsWith("Auth="))
				authCode = line.replaceFirst("Auth=", "")
		}
	}
}

def synchronized check()
{
	boolean inboxFound = false
	unread = 0
	def total = 0
		
	trayIcon.toolTip = program + ": Checking..."
	def result = post("https://wave.google.com/wave/?nouacheck", auth : authCode)
	if(result.responseCode == 200)
	{
		result.text.eachLine{
			if(it.trim().startsWith("var json ="))
			{
				String line = it.replaceFirst("var json =", "")
				s = line.replaceAll("[{]", "[")
				s = s.replaceAll("[}]", "]")
				// For Security
				s = s.replaceAll("[(]", "[")
				s = s.replaceAll("[)]", "]")
				s = s.replaceAll("[;]", "")
				if(s.trim().startsWith("["))
				{
					def o = Eval.me(s)
					
					if(o?.r == "^d1")
					{
						inboxFound = true
						def w = o?.p?."1"
	//					w.each{r = (it?."7" == 0); println "${r} ${it?."9"?."1"}"}
						w.each{
							total++
							if(it?."7" != 0)
								unread++
						}
					}
				}
				else
				{
					println "Not JSON String: $s"
				}
			}
		}
		
		if(inboxFound)
		{
			trayIcon.toolTip = program + ": " + new Date()
			if(unread > 0)
				setTrayColor(null)
			else
				setTrayColor(Color.gray)
		}
		else
		{
			authCode = null
		}
	}
	else
	{
		trayIcon.toolTip = program + ": Connection Error"
		setTrayColor(Color.red)
	}
	
}


// --------
// Main
//--------

if(!showLoginDialog())
	System.exit(0)

setupTray()
auth()

while(true)
{
	if(authCode != null)
	{
		check()
	}
	if(authCode == null)
	{
		trayIcon.toolTip = program + ": Auth Failure - Sign in again"
		setTrayColor(Color.red)
	}
	sleep(60000)
}

