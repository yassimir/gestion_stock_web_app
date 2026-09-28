package com.gharnata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facture_sequence")
@NoArgsConstructor
@AllArgsConstructor
public class FactureSequence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "dernier_numero")
    private long dernierNumero;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDernierNumero() {
        return dernierNumero;
    }

    public void setDernierNumero(long dernierNumero) {
        this.dernierNumero = dernierNumero;
    }
}
