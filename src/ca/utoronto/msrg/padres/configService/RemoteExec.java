package main;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import config.*;

import com.jcraft.jsch.*;

/*
 * TO DO:
 * - return output of remote execution
 * - get padres path from environment variable
 * - recognize host OS type to handle Linux/Windows path format
 * - if possible, replace thread sleep with waiting for command to finish execution
 */

public class RemoteExec {
	public void exec(Broker broker)
	{
		String command = createCommand(broker);
		JSch jsch = new JSch();
		try {
			Session session= jsch.getSession(broker.getUsername(), broker.getHost(), 22);
			session.setPassword(broker.getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			Channel channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec)channel).setErrStream(System.err);
			System.out.println("Connecting to " + broker.getUsername() + "@" + broker.getHost());
			System.out.println("Running: " + command);
			channel.connect();
			Thread.sleep(2000);
			channel.disconnect();
			session.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private String getPadresPath(Broker broker)
	{
		return "/home/p00ya/padres-master";
	}
	
	private String createCommand(Broker broker)
	{
		String command = getPadresPath(broker);
		
		command += "/bin/startbroker ";
		command += "-uri " + broker.getType() + "://" + broker.getHost() + ":" +
		           broker.getPort() + "/" + broker.getName() + " ";
		if(broker.getConfiguration() != null)
		{
			for(Param p : broker.getConfiguration().getParam())
			{
				command += "-" + p.getName() + " " + p.getValue() + " ";
			}
		}
		
		return command;
	}
	
	public static void main(String[] args) {
		File file = new File("topologyScripts/deployment.xml");
		JAXBContext jaxbContext;
		RemoteExec remote = new RemoteExec();
		try {
			jaxbContext = JAXBContext.newInstance(Topology.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Topology topology = (Topology) jaxbUnmarshaller.unmarshal(file);
			for(Broker broker : topology.getBroker())
				remote.exec(broker);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
