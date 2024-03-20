/*
 *    Copyright 2024 Thomas Bonk <thomas@meandmymac.de>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.berrycontrol.lirc.transmitter;


import com.github.berrycontrol.lirc.transmitter.parser.ReplyPacketParser;
import com.github.berrycontrol.lirc.transmitter.parser.exception.ReplyPacketParserException;

import java.io.IOException;
import java.util.List;

public abstract class IRTransmitter {
    public enum ConnectionType {
        TCP_IP,
        UNIX_DOMAIN_SOCKET
    }

    private final ReplyPacketParser parser = new ReplyPacketParser();

    public static IRTransmitter create(ConnectionType type, String address, int port) {
        return switch (type) {
            case TCP_IP -> new SocketIRTransmitter(address, port);
            case UNIX_DOMAIN_SOCKET -> new UnixDomainSocketIRTransmitter(address);
        };
    }

    public static IRTransmitter create(ConnectionType type, String address) {
        return create(type, address, 8765);
    }

    public abstract void connect() throws IOException;

    public abstract void close() throws IOException;

    protected abstract void send(String command) throws IOException;

    protected abstract List<String> receive() throws IOException;

    public List<String> listDevices() throws IOException, ReplyPacketParserException {
        send("LIST");
        List<String> reply = receive();
        List<String> devices = parser.parse(reply);

        return devices;
    }

    public List<String> listKeys(String device) throws IOException, ReplyPacketParserException {
        send("LIST " + device);
        List<String> reply = receive();
        List<String> devices = parser.parse(reply);

        return devices;
    }

    public boolean sendOnce(String device, String key) throws IOException, ReplyPacketParserException {
        return send(device, key, 0);
    }

    public boolean send(String device, String key, int repeats) throws IOException, ReplyPacketParserException {
        send(String.format("SEND_ONCE %s %s %d", device, key, repeats));
        List<String> reply = receive();
        List<String> result = parser.parse(reply);

        return result != null;
    }

}
