package com.village.portal.complaint.service;


public interface SmsService {
    /**
     * Send an SMS message to the given phone number.
     *
     * @param toPhone   recipient phone in E.164 format, e.g. +919876543210
     * @param message   message text to send
     */
    void send(String toPhone, String message);
}
