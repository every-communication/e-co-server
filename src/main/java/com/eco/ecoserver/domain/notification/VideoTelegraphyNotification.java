package com.eco.ecoserver.domain.notification;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VIDEO_TELEGRAPHY_NOTIFICATION")
public class VideoTelegraphyNotification extends BaseNotification {
}
