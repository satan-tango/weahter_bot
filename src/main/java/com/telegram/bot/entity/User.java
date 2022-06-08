package com.telegram.bot.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "user_name")
    private String userName;

    @OneToMany(mappedBy = "user")
    private List<UserLocation> locations;

}
