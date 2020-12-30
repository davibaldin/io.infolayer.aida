/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.infolayer.aida;

/**
 * ControlChannel is responsible for sending control messages over the service bus to internal components.
 */
public class ControlChannel {

    public static final int CMD_DATA           = 0;
    public static final int CMD_ACK_DATA       = 1;
    public static final int CMD_PUT            = 2;
    public static final int CMD_GET            = 3;
    public static final int CMD_DELETE         = 4;
    public static final int CMD_RESET_CONFIG   = 12;
    public static final int CMD_RESTART        = 90;
    public static final int CMD_SHUTDOWN       = 99;

    private int command;
    private String from;
    private String to;

    /**
     * Construct a new ControlChannel with no data.
     */
    public ControlChannel() {

    }

    /**
     * Construct a new ControlChannel with command only (anonymous mode).
     * @param command
     */
    public ControlChannel(int command) {
        this.command = command;
    }

    /**
     * Construct a new ControlChannel
     * @param command Execution signal
     * @param from Control station ID
     * @param to Destination service ID
     */
    public ControlChannel(int command, String from, String to) {
        this.command = command;
        this.from = from;
        this.to = to;
    }

    public int getCommand() {
        return command;
    }
    public void setCommand(int command) {
        this.command = command;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
    
}