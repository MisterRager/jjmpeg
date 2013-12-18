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
package jjmpegdemos;

import au.notzed.jjmpeg.mediaplayer.MediaPlayer;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author notzed
 */
public class Main {

	static File chooseFile() {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(null) == fc.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				JFrame win = new JFrame("JJMPEG Demos");
				JPanel jp = new JPanel();

				jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));

				win.add(jp);

				jp.add(new JButton(new AbstractAction("Audio Player") {

					public void actionPerformed(ActionEvent e) {
						try {
							AudioPlayer.main(new String[0]);
						} catch (Throwable ex) {
							JOptionPane.showMessageDialog(null, ex);
						}
					}
				}));
				jp.add(new JButton(new AbstractAction("Title Creator") {

					public void actionPerformed(ActionEvent e) {
						try {
							TitleWriter.main(new String[0]);
						} catch (Throwable ex) {
							JOptionPane.showMessageDialog(null, ex);
						}
					}
				}));
				jp.add(new JButton(new AbstractAction("Media Player") {

					public void actionPerformed(ActionEvent e) {
						try {
							MediaPlayer.main(new String[0]);
						} catch (Throwable ex) {
							JOptionPane.showMessageDialog(null, ex);
						}
					}
				}));

				win.pack();
				win.setDefaultCloseOperation(win.EXIT_ON_CLOSE);
				win.setVisible(true);
			}
		});
	}
}
