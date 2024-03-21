/*
 * Copyright (C) 2024 Thomas Bonk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.berrycontrol.lirc.transmitter;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

class UnixDomainSocketIRTransmitter extends IRTransmitter {
    private final UnixDomainSocketAddress socketAddress;

    private SocketChannel channel;

    UnixDomainSocketIRTransmitter(String address) {
        super();
        socketAddress = UnixDomainSocketAddress.of(Path.of(address));
    }

    @Override
    public void connect() throws IOException {
        channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        channel.connect(socketAddress);
    }

    @Override
    public void close() throws IOException {
        channel.close();
        channel = null;
    }

    @Override
    protected void send(String command) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(command.length() + 1);
        buffer.clear();
        buffer.put(command.getBytes());
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    @Override
    protected List<String> receive() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0) {
            return List.of();
        }

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);
        String message = new String(bytes);
        return Arrays.asList(message.split("\\n"));
    }
}
