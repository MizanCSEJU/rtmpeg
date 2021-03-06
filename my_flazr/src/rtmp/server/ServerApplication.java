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

package rtmp.server;

import io.flv.FlvReader;
import io.flv.FlvWriter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rtmp.RtmpConfig;
import rtmp.RtmpReader;
import rtmp.RtmpWriter;
import util.Utils;

public class ServerApplication {

    private final String name;
    private final Map<String, ServerStream> streams;

    public ServerApplication(final String rawName) {
        this.name = cleanName(rawName);        
        streams = new ConcurrentHashMap<String, ServerStream>();        
    }

    public String getName() {
        return name;
    }

    public RtmpReader getReader(final String rawName) {
        final String streamName = Utils.trimSlashes(rawName);
        final String path = RtmpConfig.SERVER_HOME_DIR + "/apps/" + name + "/";
        final String readerPlayName;
        try {
            if(streamName.startsWith("mp4:")) {
                readerPlayName = streamName.substring(4);
                return null; //I changed this, only flv will be used
            } else {                
                if(streamName.lastIndexOf('.') < streamName.length() - 4) {
                    readerPlayName = streamName + ".flv";
                } else {
                    readerPlayName = streamName;
                }
                return new FlvReader(path + readerPlayName);
            }
        } catch(Exception e) {
          //  logger.info("reader creation failed: {}", e.getMessage());
            return null;
        }
    }

    public RtmpWriter getWriter(final String rawName) {
        final String streamName = Utils.trimSlashes(rawName);
        final String path = RtmpConfig.SERVER_HOME_DIR + "/apps/" + name + "/";
        return new FlvWriter(path + streamName + ".flv");
    }

    public static ServerApplication get(final String rawName) {
        final String appName = cleanName(rawName);
        ServerApplication app = RtmpServer.APPLICATIONS.get(appName);
        if(app == null) {
            app = new ServerApplication(appName);
            RtmpServer.APPLICATIONS.put(appName, app);
        }
        return app;
    }

    public ServerStream getStream(final String rawName) {        
        return getStream(rawName, null);
    }

    public ServerStream getStream(final String rawName, final String type) {
        final String streamName = cleanName(rawName);
        ServerStream stream = streams.get(streamName);
        if(stream == null) {
            stream = new ServerStream(streamName, type);
            streams.put(streamName, stream);
        }
        return stream;
    }

    private static String cleanName(final String raw) {
        return Utils.trimSlashes(raw).toLowerCase();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[name: '").append(name);
        sb.append("' streams: ").append(streams);
        sb.append(']');
        return sb.toString();
    }

}
