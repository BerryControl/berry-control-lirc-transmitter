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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class SocketIRTransmitter extends IRTransmitter {
    private final String address;
    private final int port;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    SocketIRTransmitter(String address, int port) {
        super();
        this.address = address;
        this.port = port;
    }

    @Override
    public void connect() throws IOException {
        try (Socket socket = new Socket(address, port)) {
            clientSocket = socket;
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
    }

    @Override
    public void close() throws IOException {
        if (out != null) {
            out.close();
            out = null;
        }
        if (in != null) {
            in.close();
            in = null;
        }
        if (clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }
    }

    @Override
    protected void send(String command) throws IOException {
        out.print(command + "\n");
    }

    @Override
    protected List<String> receive() throws IOException {
        List<String> lines = new ArrayList<>();
        String line;

        while ((line = in.readLine()) != null) {
            lines.add(line);
        }

        return lines;
    }
}
