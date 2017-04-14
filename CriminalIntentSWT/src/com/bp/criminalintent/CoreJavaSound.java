package com.bp.criminalintent;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class CoreJavaSound extends Object implements LineListener{

	// FIELDS / MEMBER VARIABLES
	private Clip clip;
	
	// METHODS / MEMBER FUNCTIONS
	public CoreJavaSound() throws Exception{
		InputStream wav = CoreJavaSound.class.getResourceAsStream("res/dun_dun_1.wav");
		InputStream bufferedIn = new BufferedInputStream(wav);
		
		Line.Info linfo = new Line.Info(Clip.class);
		Line line = AudioSystem.getLine(linfo);
		clip = (Clip)line;
		clip.addLineListener(this);
		AudioInputStream ais = AudioSystem.getAudioInputStream(bufferedIn);
		clip.open(ais);
				
	}
	
	public void start() {
		if (clip.getFramePosition() == clip.getFrameLength())
			clip.setFramePosition(0);
		clip.start();
	}
	
	public void stop() {
		clip.stop();
	}
	
	@Override
	public void update(LineEvent event) {
		if (event.getType() == LineEvent.Type.CLOSE)
			System.out.println("CLOSE");
		else if (event.getType() == LineEvent.Type.OPEN)
			System.out.println("OPEN");
		else if (event.getType() == LineEvent.Type.START)
			System.out.println("START");
		else if (event.getType() == LineEvent.Type.STOP)
			System.out.println("STOP");
	}

}
