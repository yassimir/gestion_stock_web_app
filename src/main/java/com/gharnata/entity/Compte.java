package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "compte")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double credit;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }
}
