/*
 * Copyright (c) 2011 Michael Zucchi
 *
 * This file is part of jjmpegdemos.
 * 
 * jjmpegdemos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jjmpegdemos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jjmpegdemos.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.notzed.jjmpeg.mediaplayer;

import au.notzed.jjmpeg.util.JJFileChooser;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A simple(?) example of a media player using jjmpeg.
 * 
 * @author notzed
 */
public class MediaPlayer {

	MediaReader reader;
	MediaPlayerThread playert;
	BufferedImage image;
	JLabel label;
	static AL al;

	public synchronized static AL getAL() {
		if (al == null) {
			int res;

			ALut.alutInit();
			al = ALFactory.getAL();
			res = al.alGetError();
			if (res != AL.AL_NO_ERROR) {
				throw new RuntimeException("Unable to open audio: " + al.alGetString(res));
			}
		}
		return al;
	}

	public MediaPlayer(String name) throws IOException {

		reader = new MediaReader(name);
		playert = new MediaPlayerThread(reader);
		reader.createDefaultDecoders(playert);

		//Set<Entry<Integer, MediaDecoder>> decoders = reader.getDecoders();
		for (Entry<Integer, MediaDecoder> e : reader.getDecoders()) {
			MediaDecoder md = e.getValue();
			if (md instanceof VideoDecoder) {
				VideoDecoder vd = (VideoDecoder) md;

				image = new BufferedImage(vd.getWidth(), vd.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			}
		}

		if (image != null) {
			label = new JLabel(new ImageIcon(image));
			playert.initRenderers(label, image);
		} else {
			label = new JLabel();
			playert.initRenderers();
		}
		playert.initAudioOutput(getAL());
	}
	JSlider slider;
	boolean inseek;
	JLabel pos;
	JToggleButton play;

	public void start() {
		reader.start();
		if (playert != null) {
			playert.start();
		}

		JFrame sliders = new JFrame("jjmpeg Media Player Example");
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		pos = new JLabel("00:00:00.000");
		play = new JToggleButton(playAction);

		panel.add(label, BorderLayout.CENTER);
		slider = new JSlider(0, (int) reader.getDuration(), 0);
		slider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (!inseek) {// && !slider.getValueIsAdjusting()) {
					System.out.println("seeking to " + slider.getValue());
					reader.seek(slider.getValue(), 0);
				}
			}
		});
		label.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("resized " + e);
			}
		});
		playert.addMediaSinkListener(new MediaSinkListener() {

			public void positionChanged(MediaPlayerThread source) {
				//System.out.println("player moved to " + playert.getDisplayTime());
				long time = playert.getDisplayTime();
				inseek = true;
				slider.setValue((int) time);
				inseek = false;

				String times = String.format("%02d:%02d:%02d.%03d",
						time / 1000 / 60 / 60,
						time / 1000 / 60 % 60,
						time / 1000 % 60,
						time % 1000);
				pos.setText(times);
			}
		});
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

		bottom.add(play);
		bottom.add(pos);
		bottom.add(slider);

		panel.add(bottom, BorderLayout.SOUTH);
		sliders.add(panel);
		sliders.pack();
		sliders.setDefaultCloseOperation(sliders.EXIT_ON_CLOSE);
		sliders.setVisible(true);
	}
	AbstractAction playAction = new AbstractAction("Play") {

		boolean selected = true;

		@Override
		public Object getValue(String key) {
			if (key.equals(SELECTED_KEY)) {
				return selected;
			}
			return super.getValue(key);
		}

		@Override
		public void putValue(String key, Object newValue) {
			if (key.equals(SELECTED_KEY)) {
				selected = (Boolean) newValue;
				return;
			}
			super.putValue(key, newValue);
		}

		public void actionPerformed(ActionEvent e) {
			if (selected) {
				reader.unpause();
			} else {
				reader.pause();
			}
		}
	};

	public static void main(final String[] args) throws IOException {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					if (args.length == 0) {
						JJFileChooser fc = new JJFileChooser();
						if (fc.showOpenDialog(null) != fc.APPROVE_OPTION) {
							System.exit(0);
						}

						MediaPlayer vp = new MediaPlayer(fc.getSelectedFile().getAbsolutePath());
						vp.start();
					} else {
						for (int i = 0; i < args.length; i++) {
							MediaPlayer vp = new MediaPlayer(args[i]);
							vp.start();
						}
					}
				} catch (IOException x) {
				}
			}
		});
	}
}
