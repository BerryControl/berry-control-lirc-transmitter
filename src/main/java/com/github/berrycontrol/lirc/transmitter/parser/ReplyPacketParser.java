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

package com.github.berrycontrol.lirc.transmitter.parser;

import com.github.berrycontrol.lirc.transmitter.parser.exception.ReplyPacketParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ReplyPacketParser {
    public enum State {
        BEGIN,
        COMMAND,
        RESULT,
        DATA,
        LINE_COUNT_LEFT,
        DATA_BODY,
        SIGHUP_END,
        END
    }

    private final Map<State, Consumer<String>> stateMachine;
    private List<String> data;
    private State state;
    private boolean isFinished;
    private boolean success;
    private int linesLeft;

    public ReplyPacketParser() {
        stateMachine = Map.of(
            State.BEGIN, this::begin,
            State.COMMAND, this::command,
            State.RESULT, this::result,
            State.DATA, this::data,
            State.LINE_COUNT_LEFT, this::lineCountLeft,
            State.DATA_BODY, this::dataBody,
            State.SIGHUP_END, this::sigHupEnd,
            State.END, this::end
        );
        reset();
    }

    public List<String> parse(List<String> lines) {
        try {
            for (String line: lines) {
                if (!isFinished) {
                    feed(line);
                } else {
                    break;
                }
            }

            return success ? new ArrayList<>(data) : null;
        } finally {
            this.reset();
        }
    }

    private void feed(String line) {
        stateMachine.get(state).accept(line);
    }

    private void reset() {
        data = new ArrayList<>();
        state = State.BEGIN;
        isFinished = false;
        success = false;
        linesLeft = 0;
    }

    private void begin(String line) {
        if ("BEGIN".equals(line)) {
            state = State.COMMAND;
        } else {
            throw new ReplyPacketParserException(
                state,
                String.format("Expected a BEGIN line, but got '%s'", line));
        }
    }

    private void command(String line) {
        if ("SIGHUP".equals(line)) {
            state = State.SIGHUP_END;
        } else if (isNotEmptyString(line)) {
            state = State.RESULT;
        } else {
            throw new ReplyPacketParserException(
                state,
                String.format("Expected a command line, but got '%s'", line));
        }
    }

    private void result(String line) {
        if ("SUCCESS".equals(line) || "ERROR".equals(line)) {
            state = State.DATA;
            success = "SUCCESS".equals(line);
        } else {
            throw new ReplyPacketParserException(
                state,
                String.format("Expected a SUCCESS or ERROR line, but got '%s'", line));
        }
    }

    private void data(String line) {
        if ("END".equals(line)) {
            isFinished = true;
        } else if ("DATA".equals(line)) {
            state = State.LINE_COUNT_LEFT;
        } else {
            throw new ReplyPacketParserException(
                state,
                String.format("Expected a DATA or END line, but got '%s'", line));
        }
    }

    private void lineCountLeft(String line) {
        try {
            linesLeft = Integer.parseInt(line);
            state = linesLeft == 0 ? State.END : State.DATA_BODY;
        } catch (NumberFormatException ex) {
            throw new ReplyPacketParserException(
                state,
                String.format("Expected a remaining line count line, but got '%s'", line));
        }
    }

    private void dataBody(String line) {
        data.add(line);
        if (data.size() >= linesLeft) {
            state = State.END;
        }
    }

    private void sigHupEnd(String line) {
        if ("END".equals(line)) {
            reset();
        } else {
            throw new ReplyPacketParserException(
                state,
                String.format("Expected an END line with the received SIGHUP packet, but got '%s'", line));
        }
    }

    private void end(String line) {
        if ("END".equals(line)) {
            isFinished = true;
        } else {
            throw new ReplyPacketParserException(
                state,
                String.format("Expected an END line , but got '%s'", line));
        }
    }

    private boolean isEmptyString(String str) {
        return str == null || str.isEmpty();
    }

    private boolean isNotEmptyString(String str) {
        return !isEmptyString(str);
    }
}
