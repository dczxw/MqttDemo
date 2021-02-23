// IMessageSender.aidl
package com.sm.supere;

import com.sm.supere.model.MessageModel;

interface IMessageSender {
    void sendMessage(in MessageModel message);
}
