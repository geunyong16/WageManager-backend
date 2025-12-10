package com.example.wagemanager.domain.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class NotificationChannels {
    private final boolean shouldSend;
    private final List<String> channels;

    public static NotificationChannels disabled() {
        return NotificationChannels.builder()
                .shouldSend(false)
                .channels(Collections.emptyList())
                .build();
    }

    public static NotificationChannels of(boolean shouldSend, List<String> channels) {
        return NotificationChannels.builder()
                .shouldSend(shouldSend)
                .channels(channels)
                .build();
    }
}
