package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Bon")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
