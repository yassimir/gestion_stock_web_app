package com.gharnata.service;

import com.gharnata.entity.FactureSequence;
import com.gharnata.repository.RepFacSequ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NumeroFactureService {
    @Autowired
    private RepFacSequ repFacSequ;

    public Long genererNouveauNumero() {
        FactureSequence factureSequence = this.repFacSequ.findAll().get(0);
        long dernierNumero = factureSequence.getDernierNumero() + 1;
        factureSequence.setDernierNumero(dernierNumero);
        this.repFacSequ.save(factureSequence);
        return dernierNumero;
    }
}
