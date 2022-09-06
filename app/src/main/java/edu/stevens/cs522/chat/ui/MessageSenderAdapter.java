package edu.stevens.cs522.chat.ui;

import edu.stevens.cs522.chat.entities.Message;

public class MessageSenderAdapter extends MessageAdapter {
    @Override
    public String getHeading(Message message) {
        return message.sender;
    }
}
