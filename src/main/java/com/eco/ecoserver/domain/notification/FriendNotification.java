package com.eco.ecoserver.domain.notification;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("FRIEND_NOTIFICATION")
public class FriendNotification extends BaseNotification {

}
