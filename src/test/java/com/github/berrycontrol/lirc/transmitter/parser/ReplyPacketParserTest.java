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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReplyPacketParserTest {
    private ReplyPacketParser parser;

    @Test
    public void testSighupEnd() {
        List<String> input = List.of(
            "BEGIN",
            "SIGHUP",
            "END"
        );

        List<String> data = parser.parse(input);

        assertNull(data);
    }

    @Test
    public void testValidPacket() {
        List<String> input = List.of(
            "BEGIN",
            "LIST",
            "SUCCESS",
            "DATA",
            "3",
            "line 1",
            "line 2",
            "line 3",
            "END"
        );

        List<String> data = parser.parse(input);

        assertAll("data line",
            () -> assertEquals(data.get(0), "line 1"),
            () -> assertEquals(data.get(1), "line 2"),
            () -> assertEquals(data.get(2), "line 3")
        );
    }

    @BeforeEach
    void prepare() {
        parser = new ReplyPacketParser();
    }
}
