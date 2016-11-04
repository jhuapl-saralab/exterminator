/*
 * Copyright (c) 2016, Johns Hopkins University Applied Physics
 * Laboratory All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.jhuapl.exterminator.coq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.jhuapl.exterminator.coq.coq84.CoqTop84;
import edu.jhuapl.exterminator.coq.message.CoqAboutMessage;
import edu.jhuapl.exterminator.coq.message.CoqAddToLoadPathMessage;
import edu.jhuapl.exterminator.coq.message.CoqEVarsMessage;
import edu.jhuapl.exterminator.coq.message.CoqGoalMessage;
import edu.jhuapl.exterminator.coq.message.CoqHintsMessage;
import edu.jhuapl.exterminator.coq.message.CoqInterpMessage;
import edu.jhuapl.exterminator.coq.message.CoqRewindMessage;
import edu.jhuapl.exterminator.coq.message.CoqStatusMessage;

public abstract class CoqTop {

	public static boolean DEBUG = false;

	public static final String EXE = "coqtop";
	
	protected final String[] command;
	
	protected Process process;
	
	protected PrintWriter writer;
	
	protected BufferedReader reader;
	
	protected CoqTop(String... command) throws IOException {
		this.command = command;
		terminateAndRestart();
	}
	
	public void terminateAndRestart() throws IOException {
		if(this.process != null) {
			this.reader.close();
			this.writer.close();
			this.process.destroy();
		}
		this.process = new ProcessBuilder().command(command).start();
		this.writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
		this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	}
	
	public abstract CoqAddToLoadPathMessage addToLoadPath(Path directory);
	
	public abstract CoqAboutMessage about();
	
	public abstract CoqStatusMessage status();
	
	public abstract CoqGoalMessage goal();
	
	public abstract CoqEVarsMessage evars();
	
	public abstract CoqHintsMessage hints();
	
	public abstract CoqRewindMessage rewind(int steps);
	
	public abstract CoqInterpMessage interp(String code);
	
	protected Document send(CoqCommand command) {
		try {
			sendCommand(command);			
			return readMessage();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void sendCommand(CoqCommand command) {
    	String str = XMLUtils.docToString(command.getDoc());
		if(DEBUG) {
			System.out.println("Write:\t" + str);
		}
		writer.write(str);
		writer.write('\n');
		writer.flush();
    }
    
	protected static final int BUF_SIZE = 262144;
	protected static final int NUM_TRIALS = 1024;
    
	protected Document readMessage() throws IOException {
		while(!reader.ready()) {
			try {
				Thread.sleep(10);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[BUF_SIZE];
		int count = 0;
		while(count < NUM_TRIALS) {
			while(reader.ready()) {
                int numRead = reader.read(buf, 0, BUF_SIZE);
                if(numRead > 0) {
                	sb.append(new String(buf,0,numRead));
                }
            }

			if(DEBUG) {
				System.out.println("trying to parse:"+sb);
			}

			try {
				Document ret = XMLUtils.docFromString(sb.toString().trim());
				return ret;
			} catch(SAXException e) {
				if(DEBUG) {
					System.out.println("failed");
				}
				try {
					Thread.sleep(2 * count);
				} catch(InterruptedException e1) { }
			}
		}
		
		return null;
    }
	
	///////////////////////////////////////////////////////////////////////////
	
	public static CoqTop instance() throws IOException {
		// FIXME run coqtop --version and return appropriate implementation
		return new CoqTop84();
	}
	
}
