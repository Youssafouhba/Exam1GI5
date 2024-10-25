package org.sessionbean.server.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Getter
@Setter
public class Emprunt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Media media;

    @ManyToOne
    private Utilisateur utilisateur;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEmprunt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRetourPrevue;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRetour;

}