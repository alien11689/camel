/**
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
package org.apache.camel.component.mail;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Unit test for batch consumer.
 */
public class MailMaxMessagesPerPollTest extends CamelTestSupport {

    @Override
    public void setUp() throws Exception {
        prepareMailbox();
        super.setUp();
    }

    @Test
    public void testBatchConsumer() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.setResultWaitTime(2000);
        mock.expectedMessageCount(3);
        mock.message(0).body().isEqualTo("Message 0");
        mock.message(1).body().isEqualTo("Message 1");
        mock.message(2).body().isEqualTo("Message 2");
        mock.expectedPropertyReceived(Exchange.BATCH_SIZE, 3);

        assertMockEndpointsSatisfied();

        mock.reset();
        mock.expectedMessageCount(2);
        mock.expectedPropertyReceived(Exchange.BATCH_SIZE, 2);
        mock.message(0).body().isEqualTo("Message 3");
        mock.message(1).body().isEqualTo("Message 4");

        assertMockEndpointsSatisfied();
    }

    private void prepareMailbox() throws Exception {
        // connect to mailbox
        Mailbox.clearAll();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        Store store = sender.getSession().getStore("pop3");
        store.connect("localhost", 25, "jones", "secret");
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        folder.expunge();

        // inserts 5 new messages
        Message[] messages = new Message[5];
        for (int i = 0; i < 5; i++) {
            messages[i] = new MimeMessage(sender.getSession());
            messages[i].setText("Message " + i);
        }
        folder.appendMessages(messages);
        folder.close(true);
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("pop3://jones@localhost?password=secret&consumer.delay=3000&maxMessagesPerPoll=3"
                    + "&delete=true").to("mock:result");
            }
        };
    }
}