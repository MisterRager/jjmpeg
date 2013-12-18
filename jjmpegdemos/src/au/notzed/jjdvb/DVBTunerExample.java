/*
 * Copyright (c) 2011 Michael Zucchi
 *
 * This file is part of jjdvb, a java binding to linux dvb.
 *
 * jjmpeg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jjmpeg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jjmpeg.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *
 * Simple example which attemps to tune to a channel and record 20MB of data
 * from all streams.
 *
 * The file 'channels.list' must be set up in the current directory -
 * it uses the `tzap' format, also see au.notzed.jjdvb.util.DVBChannel.java.
 *
 * Requires the device to support capturing all streams in the broadcast.
 *
 * Public Domain
 */
package au.notzed.jjdvb;

import au.notzed.jjdvb.util.DVBChannel;
import au.notzed.jjdvb.util.DVBChannels;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author notzed
 */
public class DVBTunerExample {

	public static void setChannel(FE fe, DVBChannel chan) throws IOException, InterruptedException {
		int count = 0;

		// set channel
		fe.setFrontend(chan.params);

		// wait for channel to sync
		FEStatus status = new FEStatus();
		do {
			int st;

			fe.readStatus(status);
			st = status.getStatus();

			if ((st & status.FE_HAS_LOCK) != 0) {
				return;
			}

			System.out.printf("tuning status = %08x\n", st);

			Thread.sleep(100);

		} while (count++ < 100);

		throw new IOException("Timeout tuning channel");
	}

	public static void main(String[] args) {
		try {
			// read channel list
			DVBChannels channels = new DVBChannels("channels.list");

			System.out.println("read " + channels.getChannels().size() + " channels");

			// open frontend
			FE fe = FE.create("/dev/dvb/adapter0/frontend0");

			setChannel(fe, channels.getChannels().get(0));

			// Open demux
			DMX dmx = DMX.create("/dev/dvb/adapter0/demux0");

			// set filter to take whole stream
			DMXPESFilterParams filter = DMXPESFilterParams.create();
			filter.setPid((short) 0x2000);
			filter.setInput(DMXInput.DMX_IN_FRONTEND);
			filter.setOutput(DMXOutput.DMX_OUT_TS_TAP);
			filter.setPesType(DMXPESType.DMX_PES_OTHER);
			filter.setFlags(DMXPESFilterParams.DMX_IMMEDIATE_START);
			dmx.setPESFilter(filter);

			// Open stream
			FileInputStream fis = new FileInputStream("/dev/dvb/adapter0/dvr0");
			FileOutputStream fos = new FileOutputStream("capture.ts");
			byte[] buffer = new byte[4096];
			int len;

			// read 2mb of stream
			int count = 1024 * 1024 * 20;
			while (count > 0 && (len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
				count -= len;
			}

			// Close and clean up
			fos.close();
			fis.close();

			dmx.stop();
			dmx.close();
			fe.close();

		} catch (InterruptedException ex) {
			Logger.getLogger(DVBTunerExample.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(DVBTunerExample.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
