/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package rtmp.message;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;

import rtmp.RtmpHeader;

public abstract class Metadata extends AbstractMessage {

	protected String name;
	protected Object[] data;

	public Metadata(String name, Object... data) {
		this.name = name;
		this.data = data;
		header.setSize(encode().readableBytes());
	}

	public Metadata(RtmpHeader header, ChannelBuffer in) {
		super(header, in);
	}

	public Object getData(int index) {
		if (data == null || data.length < index + 1) {
			return null;
		}
		return data[index];
	}

	private Object getValue(String key) {
		final Map<String, Object> map = getMap(0);
		if (map == null) {
			return null;
		}
		return map.get(key);
	}

	

	public Map<String, Object> getMap(int index) {
		return (Map<String, Object>) getData(index);
	}

	public double getDuration() {
		if (data == null || data.length == 0) {
			return -1;
		}
		final Map<String, Object> map = getMap(0);
		if (map == null) {
			return -1;
		}
		final Object o = map.get("duration");
		if (o == null) {
			return -1;
		}
		return ((Double) o).longValue();
	}

	public void setDuration(final double duration) {
		if (data == null || data.length == 0) {
			data = new Object[] { map(pair("duration", duration)) };
		}
		final Object meta = data[0];
		final Map<String, Object> map = (Map) meta;
		if (map == null) {
			data[0] = map(pair("duration", duration));
			return;
		}
		map.put("duration", duration);
	}

	// ==========================================================================

	public static Metadata onPlayStatus(double duration, double bytes) {
		Map<String, Object> map = Command.onStatus(Command.OnStatus.STATUS,
				"NetStream.Play.Complete", pair("duration", duration), pair(
						"bytes", bytes));
		return new MetadataAmf0("onPlayStatus", map);
	}

	public static Metadata rtmpSampleAccess() {
		return new MetadataAmf0("|RtmpSampleAccess", false, false);
	}

	public static Metadata dataStart() {
		return new MetadataAmf0("onStatus", object(pair("code",
				"NetStream.Data.Start")));
	}

	// ==========================================================================

	/**
	 * [ (map){ duration=112.384, moovPosition=28.0, width=640.0, height=352.0,
	 * videocodecid=avc1, audiocodecid=mp4a, avcprofile=100.0, avclevel=30.0,
	 * aacaot=2.0, videoframerate=29.97002997002997, audiosamplerate=24000.0,
	 * audiochannels=2.0, trackinfo= [ (object){length=3369366.0,
	 * timescale=30000.0, language=eng,
	 * sampledescription=[(object){sampletype=avc1}]},
	 * (object){length=2697216.0, timescale=24000.0, language=eng,
	 * sampledescription=[(object){sampletype=mp4a}]} ]}]
	 */

	// ==========================================================================

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("name: ").append(name);
		sb.append(" data: ").append(Arrays.toString(data));
		return sb.toString();
	}

}
