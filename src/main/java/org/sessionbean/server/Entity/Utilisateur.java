package org.sessionbean.server.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    private String motDePasse;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String adresse;

    private String role;

    @Temporal(TemporalType.DATE)
    private Date dateInscription;

    private boolean actif;

    @OneToMany(mappedBy = "utilisateur")
    private List<Emprunt> emprunts;

    @PrePersist
    public void prePersist() {
        dateInscription = new Date();
        actif = true;
    }

}